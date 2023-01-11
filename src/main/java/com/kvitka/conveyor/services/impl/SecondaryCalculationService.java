package com.kvitka.conveyor.services.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SecondaryCalculationService {

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
}
