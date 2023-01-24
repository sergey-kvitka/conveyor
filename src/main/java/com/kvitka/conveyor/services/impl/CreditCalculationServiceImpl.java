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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CreditCalculationServiceImpl implements CreditCalculationService {

    private static final int AMOUNT_SALARY_MAX_RATIO = 20;
    private static final int MIN_AGE = 20;
    private static final int MAX_AGE = 60;
    private static final int MIN_TOTAL_EXPERIENCE = 12;
    private static final int MIN_CURRENT_EXPERIENCE = 3;

    private static final int MIN_FEMALE_PREFERENTIAL_AGE = 35;
    private static final int MIN_MALE_PREFERENTIAL_AGE = 30;
    private static final int MAX_MALE_PREFERENTIAL_AGE = 55;

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
        log.info("Credit calculation started. Arguments: {} and base rate is {}", scoringDataDTO, baseRate);
        BigDecimal rate = baseRate.add(new BigDecimal(score(scoringDataDTO)));
        Integer term = scoringDataDTO.getTerm();
        BigDecimal monthRateValue = rate.divide(
                new BigDecimal(1200), calculationPrecision, RoundingMode.HALF_UP);
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal monthlyPayment = secondaryCalculationService.calculateMonthlyPayment(amount, monthRateValue, term);
        LocalDate date = LocalDate.now();
        BigDecimal currentRemainingDebt = amount;
        BigDecimal interestPayment, debtPayment;
        List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();
        PaymentScheduleElement paymentScheduleElement = new PaymentScheduleElement();
        for (int i = 1; i <= term; i++) {
            interestPayment = currentRemainingDebt.multiply(monthRateValue).setScale(2, RoundingMode.HALF_UP);
            debtPayment = monthlyPayment.subtract(interestPayment);
            currentRemainingDebt = currentRemainingDebt.subtract(debtPayment);
            paymentScheduleElement = new PaymentScheduleElement(i, date, monthlyPayment, interestPayment, debtPayment,
                    currentRemainingDebt);
            paymentSchedule.add(paymentScheduleElement);
            date = date.plusMonths(1);
        }
        BigDecimal remainingDebt = paymentScheduleElement.getRemainingDebt();
        paymentScheduleElement.setRemainingDebt(new BigDecimal(0));

        CreditDTO creditDTO = new CreditDTO(
                amount,
                term,
                monthlyPayment,
                rate,
                monthlyPayment.multiply(new BigDecimal(String.valueOf(term))).subtract(remainingDebt),
                scoringDataDTO.getIsInsuranceEnabled(),
                scoringDataDTO.getIsSalaryClient(),
                paymentSchedule);
        log.info("Credit calculation finished. Result: {}", creditDTO);
        return creditDTO;
    }

    @Override
    public int score(ScoringDataDTO scoringDataDTO) throws ScoreException {
        log.info("Scoring started. Data to score: {}", scoringDataDTO);

        EmploymentDTO employment = scoringDataDTO.getEmployment();
        EmploymentStatus employmentStatus = employment.getEmploymentStatus();
        log.info("Scoring... checking employment status (value: {})", employmentStatus);
        if (employmentStatus == EmploymentStatus.UNEMPLOYED) {
            throw new ScoreException(String.format("Credit denied (Employment status \"%s\" is unacceptable).",
                    employmentStatus.name()));
        }

        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal salary = employment.getSalary();
        log.info("Scoring... checking 'credit amount':'salary' ratio (amount: {}, salary: {})", amount, salary);
        if (amount.divide(salary, 12, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal(AMOUNT_SALARY_MAX_RATIO)) > 0) {
            throw new ScoreException(String.format(
                    "Credit denied ('Credit amount':'salary' ratio is more than %d" +
                            "; actual salary: %f, actual credit amount: %f).", AMOUNT_SALARY_MAX_RATIO,
                    salary.doubleValue(), amount.doubleValue()));
        }

        LocalDate birthdate = scoringDataDTO.getBirthdate();
        int age = secondaryCalculationService.calculateCurrentAge(birthdate);
        log.info("Scoring... checking age (birthdate: {}, age: {})", birthdate, age);
        if (age < MIN_AGE || MAX_AGE < age) {
            throw new ScoreException(String.format(
                    "Credit denied (Age must be in range from %d to %d; actual age: %d).", MIN_AGE, MAX_AGE, age));
        }

        Integer experienceTotal = employment.getWorkExperienceTotal();
        log.info("Scoring... checking total experience (value: {} (months))", experienceTotal);
        if (experienceTotal < MIN_TOTAL_EXPERIENCE) {
            throw new ScoreException(String.format(
                    "Credit denied (Total work experience must not be less than %d months" +
                            "; actual total work experience: %d months).", MIN_TOTAL_EXPERIENCE, experienceTotal));
        }

        Integer experienceCurrent = employment.getWorkExperienceCurrent();
        log.info("Scoring... checking current experience (value: {} (months))", experienceCurrent);
        if (experienceCurrent < MIN_CURRENT_EXPERIENCE) {
            throw new ScoreException(String.format(
                    "Credit denied (Current work experience must not be less than %d months; " +
                            "actual current work experience: %d months).", MIN_CURRENT_EXPERIENCE, experienceCurrent));
        }

        log.info("Scoring: All checks passed! Calculating rate");

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
        if (gender == Gender.FEMALE && age >= MIN_FEMALE_PREFERENTIAL_AGE) rate -= 3;
        else if (gender == Gender.MALE && age >=MIN_MALE_PREFERENTIAL_AGE && age <=MAX_MALE_PREFERENTIAL_AGE) rate -= 3;
        else if (gender == Gender.NON_BINARY) rate += 3;

        log.info("Scoring finished. Calculated rate: {}", rate);
        return rate;
    }
}
