package com.kvitka.conveyor.controllers;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.services.impl.CreditCalculationServiceImpl;
import com.kvitka.conveyor.services.impl.OfferCalculationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
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
        log.info("[@PostMapping(offers)] calculateOffers method called. Argument: {}", loanApplicationRequestDTO);
        List<LoanOfferDTO> loanOffers = offerCalculationService.calculateOffers(loanApplicationRequestDTO);
        log.info("[@PostMapping(offers)] calculateOffers method returns value: {}", loanOffers);
        return loanOffers;
    }

    @PostMapping("calculation")
    public CreditDTO calculateCredit(@RequestBody ScoringDataDTO scoringDataDTO) {
        log.info("[@PostMapping(calculation)] calculateCredit method called. Argument: {}", scoringDataDTO);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(scoringDataDTO);
        log.info("[@PostMapping(calculation)] calculateCredit method returns value: {}", creditDTO);
        return creditDTO;
    }
}
