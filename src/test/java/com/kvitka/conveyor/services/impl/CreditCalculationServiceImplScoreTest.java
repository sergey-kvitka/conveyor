package com.kvitka.conveyor.services.impl;

import com.kvitka.conveyor.dtos.EmploymentDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.enums.EmploymentPosition;
import com.kvitka.conveyor.enums.EmploymentStatus;
import com.kvitka.conveyor.enums.Gender;
import com.kvitka.conveyor.enums.MaritalStatus;
import com.kvitka.conveyor.exceptions.ScoreException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.kvitka.conveyor.ConveyorApplicationTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
class CreditCalculationServiceImplScoreTest {

    @Autowired
    CreditCalculationServiceImpl creditCalculationService;

    static Map<String, ScoringDataDTO> unacceptableScoringData = new HashMap<>();

    static {
        int n = 1;
        ScoringDataDTO usd1 = defaultScoringDataDTO();
        usd1.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        unacceptableScoringData.put(testName.apply(n++), usd1);

        int maxRatio = 20;
        ScoringDataDTO usd2 = defaultScoringDataDTO();
        usd2.getEmployment().setSalary(toDecimal(20000.));
        usd2.setAmount(toDecimal(20000 * (maxRatio + 1)));
        unacceptableScoringData.put(testName.apply(n++), usd2);

        ScoringDataDTO usd3 = defaultScoringDataDTO();
        usd3.getEmployment().setSalary(toDecimal(30000.));
        usd3.setAmount(toDecimal(30000 * maxRatio + 1));
        unacceptableScoringData.put(testName.apply(n++), usd3);

        ScoringDataDTO usd4 = defaultScoringDataDTO();
        usd4.setBirthdate(LocalDate.now().minusYears(20).plusDays(1));
        unacceptableScoringData.put(testName.apply(n++), usd4);

        ScoringDataDTO usd5 = defaultScoringDataDTO();
        usd5.setBirthdate(LocalDate.now().minusYears(61));
        unacceptableScoringData.put(testName.apply(n++), usd5);

        ScoringDataDTO usd6 = defaultScoringDataDTO();
        usd6.getEmployment().setWorkExperienceTotal(11);
        unacceptableScoringData.put(testName.apply(n++), usd6);

        ScoringDataDTO usd7 = defaultScoringDataDTO();
        usd7.getEmployment().setWorkExperienceTotal(5);
        unacceptableScoringData.put(testName.apply(n++), usd7);

        ScoringDataDTO usd8 = defaultScoringDataDTO();
        usd8.getEmployment().setWorkExperienceCurrent(2);
        unacceptableScoringData.put(testName.apply(n++), usd8);

        ScoringDataDTO usd9 = defaultScoringDataDTO();
        usd9.getEmployment().setWorkExperienceCurrent(1);
        unacceptableScoringData.put(testName.apply(n++), usd9);

        ScoringDataDTO usd10 = defaultScoringDataDTO();
        usd10.getEmployment().setSalary(toDecimal(12000));
        usd10.setAmount(toDecimal(240001));
        unacceptableScoringData.put(testName.apply(n), usd10);
    }

    static Map<String, ScoringDataDTO> scoringDataToCheckScore = new HashMap<>();

    static {
        int n = 1;
        ScoringDataDTO csd1 = defaultScoringDataDTO();
        csd1.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER); /* +3 */
        csd1.getEmployment().setPosition(EmploymentPosition.OWNER); /* 0 */
        csd1.setMaritalStatus(MaritalStatus.SINGLE); /* +1 */
        csd1.setDependentAmount(2); /* +1 */
        csd1.setGender(Gender.NON_BINARY); /* +3 */
        scoringDataToCheckScore.put(testName.apply(n++), csd1);

        ScoringDataDTO csd2 = defaultScoringDataDTO();
        csd2.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER); /* -4 */
        csd2.setMaritalStatus(MaritalStatus.MARRIED); /* -3 */
        csd2.setGender(Gender.MALE);
        csd2.setBirthdate(LocalDate.now().minusYears(40)); /* -3 */
        scoringDataToCheckScore.put(testName.apply(n++), csd2);

        ScoringDataDTO csd3 = defaultScoringDataDTO();
        csd3.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED); /* +1 */
        csd3.getEmployment().setPosition(EmploymentPosition.MID_MANAGER); /* -2 */
        csd3.setDependentAmount(0); /* 0 */
        csd3.setMaritalStatus(MaritalStatus.SINGLE); /* +1 */
        csd3.setGender(Gender.FEMALE);
        csd3.setBirthdate(LocalDate.now().minusYears(32)); /* 0 */
        scoringDataToCheckScore.put(testName.apply(n++), csd3);

        ScoringDataDTO csd4 = defaultScoringDataDTO();
        csd4.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER); /* +3 */
        csd4.getEmployment().setPosition(EmploymentPosition.OWNER); /* 0 */
        csd4.setDependentAmount(3); /* +1 */
        csd4.setMaritalStatus(MaritalStatus.SINGLE); /* +1 */
        csd4.setGender(Gender.MALE);
        csd4.setBirthdate(LocalDate.now().minusYears(40)); /* -3 */
        scoringDataToCheckScore.put(testName.apply(n++), csd4);

        ScoringDataDTO csd5 = defaultScoringDataDTO();
        csd5.getEmployment().setEmploymentStatus(EmploymentStatus.EMPLOYED); /* 0 */
        csd5.getEmployment().setPosition(EmploymentPosition.MID_MANAGER); /* -2 */
        csd5.setDependentAmount(0); /* 0 */
        csd5.setMaritalStatus(MaritalStatus.MARRIED); /* -3 */
        csd5.setGender(Gender.MALE);
        csd5.setBirthdate(LocalDate.now().minusYears(58)); /* 0 */
        scoringDataToCheckScore.put(testName.apply(n), csd5);
    }

    @Test
    void testDefaultScore() {
        assertEquals(0, creditCalculationService.score(defaultScoringDataDTO()));
    }

    @Test
    void testScore1CheckThrowing() {
        log.info("testScore1: ScoreException should be thrown with message about unacceptable employment status");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(1)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore2CheckThrowing() {
        log.info("testScore2: ScoreException should be thrown with message about unacceptable 'credit amount':'salary' ratio");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(2)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore3CheckThrowing() {
        log.info("testScore3: ScoreException should be thrown with message about unacceptable 'credit amount':'salary' ratio");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(3)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore4CheckThrowing() {
        log.info("testScore4: ScoreException should be thrown with message about unacceptable age");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(4)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore5CheckThrowing() {
        log.info("testScore5: ScoreException should be thrown with message about unacceptable age");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(5)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore6CheckThrowing() {
        log.info("testScore6: ScoreException should be thrown with message about unacceptable total experience");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(6)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore7CheckThrowing() {
        log.info("testScore7: ScoreException should be thrown with message about unacceptable total experience");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(7)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore8CheckThrowing() {
        log.info("testScore8: ScoreException should be thrown with message about unacceptable current experience");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(8)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore9CheckThrowing() {
        log.info("testScore9: ScoreException should be thrown with message about unacceptable current experience");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(9)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore10CheckThrowing() {
        log.info("testScore10: ScoreException should be thrown with message about unacceptable 'credit amount':'salary' ratio");
        assertThrows(ScoreException.class, () -> {
            try {
                creditCalculationService.score(unacceptableScoringData.get(testName.apply(10)));
            } catch (ScoreException e) {
                log.info("{} caught. Message: \"{}\"", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testScore1CheckScore() {
        assertEquals(8, creditCalculationService.score(scoringDataToCheckScore.get(testName.apply(1))));
    }

    @Test
    void testScore2CheckScore() {
        assertEquals(-10, creditCalculationService.score(scoringDataToCheckScore.get(testName.apply(2))));
    }

    @Test
    void testScore3CheckScore() {
        assertEquals(0, creditCalculationService.score(scoringDataToCheckScore.get(testName.apply(3))));
    }

    @Test
    void testScore4CheckScore() {
        assertEquals(2, creditCalculationService.score(scoringDataToCheckScore.get(testName.apply(4))));
    }

    @Test
    void testScore5CheckScore() {
        assertEquals(-5, creditCalculationService.score(scoringDataToCheckScore.get(testName.apply(5))));
    }
}
