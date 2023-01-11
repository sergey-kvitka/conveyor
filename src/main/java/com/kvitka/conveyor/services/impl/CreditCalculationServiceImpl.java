package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.EmploymentDTO;
import com.kvitka.conveyor.dtos.PaymentScheduleElement;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.enums.EmploymentPosition;
import com.kvitka.conveyor.enums.EmploymentStatus;
import com.kvitka.conveyor.enums.Gender;
import com.kvitka.conveyor.enums.MaritalStatus;
import com.kvitka.conveyor.exceptions.ScoreException;
import com.kvitka.conveyor.services.CreditCalculationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditCalculationServiceImpl implements CreditCalculationService {

    @Value("${credit.base-rate}")
    private BigDecimal baseRateFromProperties;
    @Value("${credit.calculation-precision}")
    private int calculationPrecision;

    private final SecondaryCalculationService secondaryCalculationService;

    public CreditCalculationServiceImpl(SecondaryCalculationService secondaryCalculationService) {
        this.secondaryCalculationService = secondaryCalculationService;
    }

    @Override
    public CreditDTO calculateCredit(ScoringDataDTO scoringDataDTO) throws ScoreException {
        return calculateCredit(scoringDataDTO, baseRateFromProperties);
    }

    public CreditDTO calculateCredit(ScoringDataDTO scoringDataDTO, BigDecimal baseRate) throws ScoreException {
        BigDecimal rate = baseRate.add(new BigDecimal(score(scoringDataDTO)));
        Integer term = scoringDataDTO.getTerm();
        BigDecimal monthRateValue = rate.divide(
                new BigDecimal(term * 100), calculationPrecision, RoundingMode.HALF_UP);
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal monthlyPayment = monthRateValue
                .add(monthRateValue
                        .divide(new BigDecimal(1)
                                .add(monthRateValue)
                                .pow(term)
                                .add(new BigDecimal(-1)), calculationPrecision, RoundingMode.HALF_UP))
                .multiply(amount).setScale(2, RoundingMode.HALF_UP);

        LocalDate date = LocalDate.now();
        BigDecimal currentRemainingDebt = amount;
        BigDecimal interestPayment, debtPayment;
        List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();
        for (int i = 1; i <= term; i++) {
            interestPayment = currentRemainingDebt.multiply(monthRateValue).setScale(2, RoundingMode.HALF_UP);
            debtPayment = monthlyPayment.subtract(interestPayment);
            currentRemainingDebt = currentRemainingDebt.subtract(debtPayment);
            paymentSchedule.add(new PaymentScheduleElement(i, date,
                    monthlyPayment, interestPayment, debtPayment, currentRemainingDebt));
            date = date.plusMonths(1);
        }
        return new CreditDTO(
                amount,
                term,
                monthlyPayment,
                rate,
                monthlyPayment.multiply(new BigDecimal(String.valueOf(term))),
                scoringDataDTO.getIsInsuranceEnabled(),
                scoringDataDTO.getIsSalaryClient(),
                paymentSchedule);
    }

    @Override
    public int score(ScoringDataDTO scoringDataDTO) throws ScoreException {
        EmploymentDTO employment = scoringDataDTO.getEmployment();
        EmploymentStatus employmentStatus = employment.getEmploymentStatus();
        if (employmentStatus == EmploymentStatus.UNEMPLOYED) {
            throw new ScoreException(String.format("Credit denied (Employment status \"%s\" is unacceptable).",
                    employmentStatus.name()));
        }
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal salary = employment.getSalary();
        int amountSalaryMaxRatio = 20;
        if (amount.divide(salary, 12, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal(amountSalaryMaxRatio)) > 0) {
            throw new ScoreException(String.format(
                    "Credit denied ('Credit amount':'salary' ratio is more than %d" +
                            "; actual salary: %f, actual credit amount: %f).", amountSalaryMaxRatio,
                    salary.doubleValue(), amount.doubleValue()));
        }
        int age = secondaryCalculationService.calculateCurrentAge(scoringDataDTO.getBirthdate());
        int minAge = 20;
        int maxAge = 60;
        if (age < minAge || maxAge < age) {
            throw new ScoreException(String.format(
                    "Credit denied (Age must be in range from %d to %d; actual age: %d).", minAge, maxAge, age));
        }
        Integer experienceTotal = employment.getWorkExperienceTotal();
        int minTotalExperience = 12;
        if (experienceTotal < minTotalExperience) {
            throw new ScoreException(String.format(
                    "Credit denied (Total work experience must not be less than %d months" +
                            "; actual total work experience: %d months).", minTotalExperience, experienceTotal));
        }
        Integer experienceCurrent = employment.getWorkExperienceCurrent();
        int minCurrentExperience = 3;
        if (experienceCurrent < minCurrentExperience) {
            throw new ScoreException(String.format(
                    "Credit denied (Current work experience must not be less than %d months" +
                            "; actual current work experience: %d months).", minCurrentExperience, experienceCurrent));
        }

        int rate = 0;
        if (employmentStatus == EmploymentStatus.SELF_EMPLOYED) rate += 1;
        else if (employmentStatus == EmploymentStatus.BUSINESS_OWNER) rate += 3;

        EmploymentPosition employmentPosition = employment.getPosition();
        if (employmentPosition == EmploymentPosition.MID_MANAGER) rate -= 2;
        else if (employmentPosition == EmploymentPosition.TOP_MANAGER) rate -= 4;

        MaritalStatus maritalStatus = scoringDataDTO.getMaritalStatus();
        if (maritalStatus == MaritalStatus.MARRIED) rate -= 3;
        else if (maritalStatus == MaritalStatus.SINGLE) rate += 1;

        if (scoringDataDTO.getDependentAmount() > 1) rate += 1;

        Gender gender = scoringDataDTO.getGender();
        int minFemalePreferentialAge = 35;
        int minMalePreferentialAge = 30;
        int maxMalePreferentialAge = 55;
        if (gender == Gender.FEMALE && age >= minFemalePreferentialAge) rate -= 3;
        else if (gender == Gender.MALE && age >= minMalePreferentialAge && age <= maxMalePreferentialAge) rate -= 3;
        else if (gender == Gender.NON_BINARY) rate += 3;

        return rate;
    }
}
