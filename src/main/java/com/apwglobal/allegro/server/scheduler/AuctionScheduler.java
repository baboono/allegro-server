package com.apwglobal.allegro.server.scheduler;

import com.apwglobal.allegro.server.service.IAuctionService;
import com.apwglobal.nice.domain.Auction;
import com.apwglobal.nice.domain.AuctionStatus;
import com.apwglobal.nice.domain.AuctionStatusType;
import com.apwglobal.nice.service.IAllegroNiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.List;

import static com.apwglobal.nice.domain.AuctionStatusType.*;
import static java.util.stream.Collectors.toList;

@Service
public class AuctionScheduler {

    @Autowired
    private IAllegroNiceApi allegro;

    @Autowired
    private IAuctionService auctionService;

    @Scheduled(fixedDelay=9 * 60000)
    @Transactional
    public void syncAuctions() {
        doAuctions(
                a -> !exists(a),
                auctionService::saveAuction);
    }

    @Scheduled(fixedDelay=11 * 60000)
    @Transactional
    public void updateAuctions() {
        doAuctions(
                this::exists,
                auctionService::updateAuction);
    }

    private boolean exists(Auction a) {
        return auctionService.getAuctionById(a.getId()).isPresent();
    }

    private void doAuctions(Func1<Auction, Boolean> predicate, Action1<Auction> consumer) {
        allegro
                .login()
                .getAuctions()
                .filter(predicate)
                .forEach(consumer);
    }



    @Scheduled(fixedDelay=1 * 60000)
    @Transactional
    public void closeAuctions() {
        List<Long> inDb  = auctionService.getAuctionStatusesByStatus(OPEN)
                .stream()
                .map(AuctionStatus::getItemId)
                .collect(toList());

        if (inDb.isEmpty()) {
            return;
        }

        List<Long> inAllegro = allegro
                .login()
                .getAuctions()
                .map(Auction::getId)
                .toList()
                .toBlocking()
                .single();

        inDb
                .stream()
                .filter(a -> !inAllegro.contains(a))
                .forEach(auctionService::closeAuction);
    }

}
