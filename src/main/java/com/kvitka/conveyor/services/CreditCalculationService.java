package com.kvitka.conveyor.services;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.exceptions.ScoreException;

public interface CreditCalculationService {
    CreditDTO calculateCredit(ScoringDataDTO scoringDataDTO);

    int score(ScoringDataDTO scoringDataDTO) throws ScoreException;
}
