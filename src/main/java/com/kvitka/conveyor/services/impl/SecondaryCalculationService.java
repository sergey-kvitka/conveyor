package com.kvitka.conveyor.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class SecondaryCalculationService {

    @Value("${credit.calculation-precision}")
    private int calculationPrecision;

    public int calculateCurrentAge(LocalDate birthdate) {
        LocalDate now = LocalDate.now();
        int birthdateMonth = birthdate.getMonth().getValue();
        int monthNow = now.getMonth().getValue();

        if (birthdate.getEra().getValue() == 0) return -1;
        return now.getYear() - birthdate.getYear() - ((
                (monthNow < birthdateMonth
                        || (birthdateMonth == monthNow && birthdate.getDayOfMonth() > now.getDayOfMonth())) ? 1 : 0
        ));
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal creditAmount, BigDecimal monthRate, Integer term) {
        return monthRate
                .add(monthRate
                        .divide(new BigDecimal(1)
                                .add(monthRate)
                                .pow(term)
                                .add(new BigDecimal(-1)), calculationPrecision, RoundingMode.HALF_UP))
                .multiply(creditAmount).setScale(2, RoundingMode.HALF_UP);
    }
}
