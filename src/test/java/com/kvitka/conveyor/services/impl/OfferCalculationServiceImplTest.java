package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kvitka.conveyor.ConveyorApplicationTests.defaultLoanApplicationRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class OfferCalculationServiceImplTest {

    @Autowired
    OfferCalculationServiceImpl offerCalculationService;

    @Test
    @Order(1)
    @DisplayName("Offers calculation test")
    void calculateOffers() {
        LoanApplicationRequestDTO defaultLoanApplicationRequest = defaultLoanApplicationRequest();
        List<LoanOfferDTO> loanOffers = offerCalculationService.calculateOffers(defaultLoanApplicationRequest);

        List<List<Boolean>> twoBoolCombinations = List.of(
                List.of(true, true), List.of(true, false),
                List.of(false, true), List.of(false, false));
        int loanOfferAmount = 4;

        log.info("[Offers calculation test]: checking amount of offers (must be {})", loanOfferAmount);
        assertEquals(loanOfferAmount, loanOffers.size());

        log.info("[Offers calculation test]: checking values of fields 'isInsuranceEnabled' and 'isSalaryClient'");
        assertTrue(loanOffers.stream()
                .allMatch(loanOffer -> twoBoolCombinations.contains(
                        List.of(loanOffer.getIsInsuranceEnabled(),
                                loanOffer.getIsSalaryClient()))));

        log.info("[Offers calculation test]: checking that all offers have equal term " +
                "(from loanApplicationRequest)");
        assertTrue(loanOffers.stream()
                .map(LoanOfferDTO::getTerm)
                .allMatch(term -> Objects.equals(term,
                        defaultLoanApplicationRequest.getTerm())));

        log.info("[Offers calculation test]: checking that all offers have equal requested amount " +
                "(from loanApplicationRequest)");
        assertTrue(loanOffers.stream()
                .map(LoanOfferDTO::getRequestedAmount)
                .allMatch(amount -> Objects.equals(amount,
                        defaultLoanApplicationRequest.getAmount())));

        List<BigDecimal> actualRates = loanOffers.stream()
                .map(LoanOfferDTO::getRate)
                .collect(Collectors.toList());
        List<BigDecimal> sortedRates = loanOffers.stream()
                .map(LoanOfferDTO::getRate)
                .sorted(Comparator.comparing(BigDecimal::negate))
                .collect(Collectors.toList());

        log.info("[Offers calculation test]: checking that offers have the correct order (sorted by rate desc)");
        assertEquals(actualRates, sortedRates);
    }
}
