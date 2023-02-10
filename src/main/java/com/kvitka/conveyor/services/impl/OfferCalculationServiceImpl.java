package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.services.OfferCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferCalculationServiceImpl implements OfferCalculationService {

    private static final int INSURANCE_RATE_VARIATION = -2;
    private static final int SALARY_CLIENT_RATE_VARIATION = -1;

    @Value("${credit.calculation-precision}")
    private int calculationPrecision;
    @Value("${credit.base-rate}")
    private BigDecimal baseRate;

    private final SecondaryCalculationService secondaryCalculationService;

    @Override
    public List<LoanOfferDTO> calculateOffers(LoanApplicationRequestDTO loanApplicationRequestDTO) {
        log.info("Offers calculation started. Argument: {}", loanApplicationRequestDTO);
        List<LoanOfferDTO> loanOffers = new ArrayList<>();
        Integer term = loanApplicationRequestDTO.getTerm();
        BigDecimal requestedAmount = loanApplicationRequestDTO.getAmount();
        BigDecimal totalAmount, monthlyPayment, rate;

        List<Boolean> booleans = Arrays.asList(true, false);
        for (boolean isInsuranceEnabled : booleans) {
            for (boolean isSalaryClient : booleans) {

                rate = baseRate
                        .add(new BigDecimal(INSURANCE_RATE_VARIATION * (isInsuranceEnabled ? 1 : -1)))
                        .add(new BigDecimal(SALARY_CLIENT_RATE_VARIATION * (isSalaryClient ? 1 : -1)));

                monthlyPayment = secondaryCalculationService.calculateMonthlyPayment(
                        requestedAmount,
                        rate.divide(new BigDecimal("1200"), calculationPrecision, RoundingMode.HALF_UP),
                        term);
                totalAmount = monthlyPayment.multiply(new BigDecimal(term));

                loanOffers.add(new LoanOfferDTO(
                        null,
                        requestedAmount,
                        totalAmount,
                        term,
                        monthlyPayment,
                        rate,
                        isInsuranceEnabled, isSalaryClient
                ));
            }
        }
        loanOffers.sort(Comparator.comparing(loanOfferDTO -> loanOfferDTO.getRate().negate()));
        log.info("Offers calculation finished. Result: {}", loanOffers);
        return loanOffers;
    }
}
