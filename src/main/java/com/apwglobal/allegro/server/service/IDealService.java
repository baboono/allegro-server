package com.apwglobal.allegro.server.service;

import com.apwglobal.nice.domain.Deal;

import java.util.List;
import java.util.Optional;

public interface IDealService {

    Optional<Long> findLastRowId();
    List<Deal> getLastDeals(int limit);
    List<Deal> getDealsAfter(long transactionId);
    void saveDeal(Deal deal);

}
