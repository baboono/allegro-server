package com.apwglobal.allegro.server.scheduler;

import com.apwglobal.allegro.server.service.IDealService;
import com.apwglobal.allegro.server.service.IPostBuyFormsService;
import com.apwglobal.nice.domain.Deal;
import com.apwglobal.nice.service.IAllegroNiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rx.Observable;

import java.util.List;
import java.util.Optional;

@Service
public class PostBuyFormsScheduler {

    @Autowired
    private IAllegroNiceApi allegro;

    @Autowired
    private IPostBuyFormsService postBuyFormsService;

    @Autowired
    private IDealService dealService;

    @Scheduled(fixedDelay = 2 * 60000)
    @Transactional
    public void syncPostBuyForms() {

        Optional<Long> transactionId = postBuyFormsService.findLastTransactionId();
        List<Deal> deals = transactionId
                .map(dealService::getDealsAfter)
                .orElse(dealService.getDealsAfter(0));

        allegro
                .login()
                .getPostBuyForms(Observable.from(deals))
                .forEach(postBuyFormsService::savePostBuyForm);
    }

}
