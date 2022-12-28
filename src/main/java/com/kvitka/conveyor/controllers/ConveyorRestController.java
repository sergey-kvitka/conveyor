package com.kvitka.conveyor.controllers;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("conveyor")
public class ConveyorRestController {
    @PostMapping("offers")
    public List<LoanOfferDTO> calculateOffers(@RequestBody LoanApplicationRequestDTO loanApplicationRequestDTO) {
        //TODO
        return null;
    }

    @PostMapping("calculation")
    public CreditDTO calculateCredit(@RequestBody ScoringDataDTO scoringDataDTO) {
        //TODO
        return null;
    }
}
