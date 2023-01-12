package com.kvitka.conveyor.services;

import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.exceptions.PreScoreException;

import java.util.List;

public interface OfferCalculationService {
    List<LoanOfferDTO> calculateOffers(LoanApplicationRequestDTO loanApplicationRequestDTO);

    void preScore(LoanApplicationRequestDTO loanApplicationRequestDTO) throws PreScoreException;
}
