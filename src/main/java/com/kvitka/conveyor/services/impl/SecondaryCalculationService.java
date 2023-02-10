package com.kvitka.conveyor.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
public class SecondaryCalculationService {

    @Value("${credit.calculation-precision}")
    private int calculationPrecision;

    public int calculateCurrentAge(LocalDate birthdate) {
        log.info("Age calculation started (birthdate: {})", birthdate);
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        log.info("Age calculation finished (result: {} (years))", age);
        return age;
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal creditAmount, BigDecimal monthRate, Integer term) {
        log.info("Monthly payment calculation started (credit amount: {}, month rate: {}, term: {} (months))",
                creditAmount, monthRate, term);
        BigDecimal monthlyPayment = monthRate
                .add(monthRate
                        .divide(new BigDecimal(1)
                                .add(monthRate)
                                .pow(term)
                                .add(new BigDecimal(-1)), calculationPrecision, RoundingMode.HALF_UP))
                .multiply(creditAmount).setScale(2, RoundingMode.HALF_UP);
        log.info("Monthly payment calculation finished (result: {})", monthlyPayment);
        return monthlyPayment;
    }
}
