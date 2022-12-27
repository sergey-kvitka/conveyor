package com.kvitka.conveyor.services;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;

public interface CreditCalculationService {
    CreditDTO calculateCredit(ScoringDataDTO scoringDataDTO);
}
