package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.CreditDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.enums.EmploymentPosition;
import com.kvitka.conveyor.enums.EmploymentStatus;
import com.kvitka.conveyor.enums.Gender;
import com.kvitka.conveyor.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kvitka.conveyor.ConveyorApplicationTests.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class CreditCalculationServiceImplCalculateCreditTest {

    @Value("${credit.allowable-error}")
    private Double allowableError;
    @Value("${credit.calculation-precision}")
    private Integer calculationPrecision;

    @Autowired
    CreditCalculationServiceImpl creditCalculationService;

    @Data
    @AllArgsConstructor
    static class CreditCalculationData {
        private BigDecimal baseRate;
        private BigDecimal monthlyPayment;
        private BigDecimal psk;
    }

    Map<String, ScoringDataDTO> scoringDataDTOs = new HashMap<>();
    Map<String, CreditCalculationData> creditCalculationDataMap = new HashMap<>();

    {
        int n = 0;
        ScoringDataDTO sd1 = defaultScoringDataDTO();
        sd1.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        sd1.getEmployment().setPosition(EmploymentPosition.OWNER);
        sd1.setMaritalStatus(MaritalStatus.SINGLE);
        sd1.setDependentAmount(2);
        sd1.setGender(Gender.NON_BINARY); // * score rate is 8
        sd1.getEmployment().setSalary(toDecimal(100_000));
        sd1.setAmount(toDecimal(1_000_000));
        sd1.setTerm(12);
        scoringDataDTOs.put(testName.apply(++n), sd1);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(20),
                toDecimal(96505.99),
                toDecimal(1_158_071.86)
        ));

        ScoringDataDTO sd2 = defaultScoringDataDTO();
        sd2.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        sd2.getEmployment().setPosition(EmploymentPosition.OWNER);
        sd2.setMaritalStatus(MaritalStatus.SINGLE);
        sd2.setDependentAmount(2);
        sd2.setGender(Gender.NON_BINARY); // * score rate is 8
        sd2.getEmployment().setSalary(toDecimal(100_000));
        sd2.setAmount(toDecimal(1_500_000));
        sd2.setTerm(36);
        scoringDataDTOs.put(testName.apply(++n), sd2);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(7),
                toDecimal(51997.99),
                toDecimal(1_871_927.74)
        ));

        ScoringDataDTO sd3 = defaultScoringDataDTO();
        sd3.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);
        sd3.setMaritalStatus(MaritalStatus.MARRIED);
        sd3.setGender(Gender.MALE);
        sd3.setBirthdate(LocalDate.now().minusYears(40)); // * score rate is -10
        sd3.getEmployment().setSalary(toDecimal(500_000));
        sd3.setAmount(toDecimal(4_000_000));
        sd3.setTerm(48);
        scoringDataDTOs.put(testName.apply(++n), sd3);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(15),
                toDecimal(92117.17),
                toDecimal(4_421_624.37)
        ));

        ScoringDataDTO sd4 = defaultScoringDataDTO();
        sd4.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER);
        sd4.setMaritalStatus(MaritalStatus.MARRIED);
        sd4.setGender(Gender.MALE);
        sd4.setBirthdate(LocalDate.now().minusYears(40)); // * score rate is -10
        sd4.getEmployment().setSalary(toDecimal(500_000));
        sd4.setAmount(toDecimal(2_345_678));
        sd4.setTerm(33);
        scoringDataDTOs.put(testName.apply(++n), sd4);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(20),
                toDecimal(81596.12),
                toDecimal(2_692_671.90)
        ));

        ScoringDataDTO sd5 = defaultScoringDataDTO();
        sd5.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        sd5.getEmployment().setPosition(EmploymentPosition.MID_MANAGER);
        sd5.setDependentAmount(0);
        sd5.setMaritalStatus(MaritalStatus.SINGLE);
        sd5.setGender(Gender.FEMALE);
        sd5.setBirthdate(LocalDate.now().minusYears(32)); // * score rate is 0
        sd5.getEmployment().setSalary(toDecimal(500_000));
        sd5.setAmount(toDecimal(3_000_000));
        sd5.setTerm(60);
        scoringDataDTOs.put(testName.apply(++n), sd5);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(10),
                toDecimal(63741.13),
                toDecimal(3_824_468.05)
        ));

        ScoringDataDTO sd6 = defaultScoringDataDTO();
        sd6.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        sd6.getEmployment().setPosition(EmploymentPosition.MID_MANAGER);
        sd6.setDependentAmount(0);
        sd6.setMaritalStatus(MaritalStatus.SINGLE);
        sd6.setGender(Gender.FEMALE);
        sd6.setBirthdate(LocalDate.now().minusYears(32)); // * score rate is 0
        sd6.getEmployment().setSalary(toDecimal(500_000));
        sd6.setAmount(toDecimal(800_000));
        sd6.setTerm(20);
        scoringDataDTOs.put(testName.apply(++n), sd6);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(8),
                toDecimal(42858.90),
                toDecimal(857_177.95)
        ));

        ScoringDataDTO sd7 = defaultScoringDataDTO();
        sd7.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        sd7.getEmployment().setPosition(EmploymentPosition.OWNER);
        sd7.setDependentAmount(3);
        sd7.setMaritalStatus(MaritalStatus.SINGLE);
        sd7.setGender(Gender.MALE);
        sd7.setBirthdate(LocalDate.now().minusYears(40)); // * score rate is 2
        sd7.getEmployment().setSalary(toDecimal(500_000));
        sd7.setAmount(toDecimal(4_000_000));
        sd7.setTerm(45);
        scoringDataDTOs.put(testName.apply(++n), sd7);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(10),
                toDecimal(110_820.18),
                toDecimal(4_986_908.20)
        ));

        ScoringDataDTO sd8 = defaultScoringDataDTO();
        sd8.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);
        sd8.getEmployment().setPosition(EmploymentPosition.OWNER);
        sd8.setDependentAmount(3);
        sd8.setMaritalStatus(MaritalStatus.SINGLE);
        sd8.setGender(Gender.MALE);
        sd8.setBirthdate(LocalDate.now().minusYears(40)); // * score rate is 2
        sd8.getEmployment().setSalary(toDecimal(500_000));
        sd8.setAmount(toDecimal(2_245_000));
        sd8.setTerm(17);
        scoringDataDTOs.put(testName.apply(++n), sd8);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(13),
                toDecimal(147_407.23),
                toDecimal(2_505_922.83)
        ));

        ScoringDataDTO sd9 = defaultScoringDataDTO();
        sd9.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        sd9.getEmployment().setPosition(EmploymentPosition.MID_MANAGER);
        sd9.setDependentAmount(0);
        sd9.setMaritalStatus(MaritalStatus.MARRIED);
        sd9.setGender(Gender.MALE);
        sd9.setBirthdate(LocalDate.now().minusYears(58)); // * score rate is -5
        sd9.getEmployment().setSalary(toDecimal(500_000));
        sd9.setAmount(toDecimal(5_000_000));
        sd9.setTerm(40);
        scoringDataDTOs.put(testName.apply(++n), sd9);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(10),
                toDecimal(135_965.52),
                toDecimal(5_438_620.82)
        ));

        ScoringDataDTO sd10 = defaultScoringDataDTO();
        sd10.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED);
        sd10.getEmployment().setPosition(EmploymentPosition.MID_MANAGER);
        sd10.setDependentAmount(0);
        sd10.setMaritalStatus(MaritalStatus.MARRIED);
        sd10.setGender(Gender.MALE);
        sd10.setBirthdate(LocalDate.now().minusYears(58)); // * score rate is -5
        sd10.getEmployment().setSalary(toDecimal(500_000));
        sd10.setAmount(toDecimal(5_555_555));
        sd10.setTerm(55);
        scoringDataDTOs.put(testName.apply(++n), sd10);
        creditCalculationDataMap.put(testName.apply(n), new CreditCalculationData(
                toDecimal(15),
                toDecimal(126_333.38),
                toDecimal(6_948_335.90)
        ));
    }

    double[] calculateError(BigDecimal calculatedMonthlyPayment, BigDecimal targetMonthlyPayment,
                            BigDecimal calculatedPsk, BigDecimal targetPsk) {
        log.info("double[] calculateError: monthly payment data to compare: {} and {}",
                calculatedMonthlyPayment.toPlainString(), targetMonthlyPayment.toPlainString());
        log.info("double[] calculateError: psk data to compare: {} and {}",
                calculatedPsk.toPlainString(), targetPsk.toPlainString());
        double monthlyPaymentError = Math.abs(calculatedMonthlyPayment
                .divide(targetMonthlyPayment, calculationPrecision, RoundingMode.HALF_UP)
                .multiply(toDecimal(100))
                .subtract(toDecimal(100)).doubleValue());
        double pskError = Math.abs(calculatedPsk
                .divide(targetPsk, calculationPrecision, RoundingMode.HALF_UP)
                .multiply(toDecimal(100))
                .subtract(toDecimal(100)).doubleValue());

        return new double[]{monthlyPaymentError, pskError};
    }

    @Test
    void testErrorCalculation() {
        double[] calculateErrors = calculateError(
                toDecimal(1 * (100 - allowableError) + 0.0001), toDecimal(100),
                toDecimal(0.5 * (100 + allowableError) - 0.0005), toDecimal(50));
        log.info("testErrorCalculation: these numbers must be less than {}: {}",
                allowableError, Arrays.toString(calculateErrors));
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision1() {
        int testNumber = 1;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision2() {
        int testNumber = 2;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision3() {
        int testNumber = 3;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision4() {
        int testNumber = 4;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision5() {
        int testNumber = 5;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision6() {
        int testNumber = 6;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision7() {
        int testNumber = 7;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision8() {
        int testNumber = 8;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision9() {
        int testNumber = 9;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }

    @Test
    void testCreditCalculationPrecision10() {
        int testNumber = 10;
        CreditCalculationData creditCalculationData = creditCalculationDataMap.get(testName.apply(testNumber));
        log.info("testCreditCalculationPrecision{}: Credit Calculation Data is: {}", testNumber, creditCalculationData);
        log.info("testCreditCalculationPrecision{}: calculating creditDTO", testNumber);
        CreditDTO creditDTO = creditCalculationService.calculateCredit(
                scoringDataDTOs.get(testName.apply(testNumber)),
                creditCalculationData.getBaseRate());
        log.info("testCreditCalculationPrecision{}: Credit DTO is: {}", testNumber, creditDTO);
        log.info("testCreditCalculationPrecision{}: calculating errors", testNumber);
        double[] calculateErrors = calculateError(creditDTO.getMonthlyPayment(), creditCalculationData.getMonthlyPayment(),
                creditDTO.getPsk(), creditCalculationData.getPsk());
        log.info("testCreditCalculationPrecision{}: calculated errors ({}) must be both less than {}",
                testNumber, Arrays.toString(calculateErrors), allowableError);
        assertTrue(calculateErrors[0] < allowableError);
        assertTrue(calculateErrors[1] < allowableError);
    }
}
