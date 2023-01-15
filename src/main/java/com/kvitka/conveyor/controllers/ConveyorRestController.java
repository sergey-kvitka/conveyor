package com.kvitka.conveyor.controllers;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.services.impl.CreditCalculationServiceImpl;
import com.kvitka.conveyor.services.impl.OfferCalculationServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("conveyor")
public class ConveyorRestController {

    private final OfferCalculationServiceImpl offerCalculationService;
    private final CreditCalculationServiceImpl creditCalculationService;

    public ConveyorRestController(OfferCalculationServiceImpl offerCalculationService,
                                  CreditCalculationServiceImpl creditCalculationService) {
        this.offerCalculationService = offerCalculationService;
        this.creditCalculationService = creditCalculationService;
    }

    @PostMapping("offers")
    public List<LoanOfferDTO> calculateOffers(@RequestBody LoanApplicationRequestDTO loanApplicationRequestDTO) {
        return offerCalculationService.calculateOffers(loanApplicationRequestDTO);
    }

    @PostMapping("calculation")
    public CreditDTO calculateCredit(@RequestBody ScoringDataDTO scoringDataDTO) {
        return creditCalculationService.calculateCredit(scoringDataDTO);
    }
}
