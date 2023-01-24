package com.kvitka.conveyor.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Service
public class SecondaryCalculationService {

    @Value("${credit.calculation-precision}")
    private int calculationPrecision;

    public int calculateCurrentAge(LocalDate birthdate) {
        log.info("Age calculation started (birthdate: {})", birthdate);
        LocalDate now = LocalDate.now();
        int birthdateMonth = birthdate.getMonth().getValue();
        int monthNow = now.getMonth().getValue();

        if (birthdate.getEra().getValue() == 0) {
            log.warn("Age calculation failed: date.getEra() invalid value");
            return -1;
        }
        int age = now.getYear() - birthdate.getYear() - ((
                (monthNow < birthdateMonth
                        || (birthdateMonth == monthNow && birthdate.getDayOfMonth() > now.getDayOfMonth())) ? 1 : 0
        ));
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
