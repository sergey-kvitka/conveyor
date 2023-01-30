package com.kvitka.conveyor.services;

import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;

import java.util.List;

public interface OfferCalculationService {
    List<LoanOfferDTO> calculateOffers(LoanApplicationRequestDTO loanApplicationRequestDTO);
}
