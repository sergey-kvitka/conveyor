package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.LoanOfferDTO;
import com.kvitka.conveyor.exceptions.PreScoreException;
import com.kvitka.conveyor.services.OfferCalculationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OfferCalculationServiceImpl implements OfferCalculationService {

    @Value("${credit.calculation-precision}")
    private int calculationPrecision;
    @Value("${credit.base-rate}")
    private BigDecimal baseRate;

    private final SecondaryCalculationService secondaryCalculationService;

    public OfferCalculationServiceImpl(SecondaryCalculationService secondaryCalculationService) {
        this.secondaryCalculationService = secondaryCalculationService;
    }

    @Override
    public List<LoanOfferDTO> calculateOffers(LoanApplicationRequestDTO loanApplicationRequestDTO) {
        preScore(loanApplicationRequestDTO);
        List<LoanOfferDTO> loanOffers = new ArrayList<>();
        Integer term = loanApplicationRequestDTO.getTerm();
        BigDecimal requestedAmount = loanApplicationRequestDTO.getAmount();
        BigDecimal totalAmount;
        BigDecimal rate;

        List<Boolean> booleans = Arrays.asList(true, false);
        for (boolean isInsuranceEnabled : booleans) {
            if (isInsuranceEnabled) {
                totalAmount = requestedAmount.multiply(new BigDecimal("1.1"));
                rate = baseRate.subtract(new BigDecimal(5));
            } else {
                totalAmount = requestedAmount.add(BigDecimal.ZERO);
                rate = baseRate.add(new BigDecimal(2));
            }
            for (boolean isSalaryClient : booleans) {
                rate = rate.add(new BigDecimal(
                        isSalaryClient ? -1 : 1
                ));
                loanOffers.add(new LoanOfferDTO(
                        1L, /* ? applicationId */ //TODO
                        requestedAmount,
                        totalAmount,
                        term,
                        secondaryCalculationService.calculateMonthlyPayment(
                                totalAmount,
                                rate.divide(new BigDecimal("1200"), calculationPrecision, RoundingMode.HALF_UP),
                                term),
                        rate,
                        isInsuranceEnabled, isSalaryClient
                ));
            }
        }
        loanOffers.sort(Comparator.comparing(LoanOfferDTO::getRate));
        return loanOffers;
    }

    @Override
    public void preScore(LoanApplicationRequestDTO loanApplicationRequestDTO) throws PreScoreException {
        Pattern onlyLettersAndLength = Pattern.compile("^[a-z]{2,30}", Pattern.CASE_INSENSITIVE);
        String onlyLettersAndLengthMismatchMessage =
                "must only contain latin letters and its length must be in range from 2 to 30 characters";
        validateNamePattern(loanApplicationRequestDTO.getFirstName(), "first name",
                onlyLettersAndLength, onlyLettersAndLengthMismatchMessage);
        validateNamePattern(loanApplicationRequestDTO.getLastName(), "last name",
                onlyLettersAndLength, onlyLettersAndLengthMismatchMessage);
        String middleName = loanApplicationRequestDTO.getMiddleName();
        if (middleName != null && !middleName.isEmpty()) {
            validateNamePattern(loanApplicationRequestDTO.getMiddleName(), "middle name",
                    onlyLettersAndLength, onlyLettersAndLengthMismatchMessage + " (if it's present)");
        }

        int minTerm = 6;
        if (loanApplicationRequestDTO.getTerm() < minTerm) {
            throw new PreScoreException(String.format("Term must not be less than %d months.", minTerm));
        }

        int minAge = 18;
        if (secondaryCalculationService.calculateCurrentAge(loanApplicationRequestDTO.getBirthdate()) < minAge) {
            throw new PreScoreException(String.format("The minimum age is %d years.", minAge));
        }

        String email = loanApplicationRequestDTO.getEmail();
        String emailRegex = "[\\w\\.]{2,50}@[\\w\\.]{2,20}";
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            throw new PreScoreException(String.format("Incorrect email format (%s).", email));
        }

        String passportRegexToFormat = "[0-9]{%d}";
        int passportSeriesLength = 4;
        if (!Pattern.compile(String.format(passportRegexToFormat, passportSeriesLength))
                .matcher(loanApplicationRequestDTO.getPassportSeries()).matches()) {
            throw new PreScoreException(String.format("Passport series must contain %d digits.", passportSeriesLength));
        }
        int passportNumberLength = 6;
        if (!Pattern.compile(String.format(passportRegexToFormat, passportNumberLength))
                .matcher(loanApplicationRequestDTO.getPassportNumber()).matches()) {
            throw new PreScoreException(String.format("Passport number must contain %d digits.", passportNumberLength));
        }
    }

    private void validateNamePattern(String value, String varName,
                                     Pattern pattern, String mismatchMessage) throws PreScoreException {
        varName = varName.trim();
        String varNameCapitalized = value.length() < 1 ? varName :
                varName.substring(0, 1).toUpperCase() + varName.substring(1);

        if (!pattern.matcher(value).matches()) throw new PreScoreException(String.format("%s %s.",
                varNameCapitalized, mismatchMessage.trim()));
    }
}
