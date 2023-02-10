package com.kvitka.conveyor;

import com.kvitka.conveyor.dtos.EmploymentDTO;
import com.kvitka.conveyor.dtos.LoanApplicationRequestDTO;
import com.kvitka.conveyor.dtos.ScoringDataDTO;
import com.kvitka.conveyor.enums.EmploymentPosition;
import com.kvitka.conveyor.enums.EmploymentStatus;
import com.kvitka.conveyor.enums.Gender;
import com.kvitka.conveyor.enums.MaritalStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

@SpringBootTest
public class ConveyorApplicationTests {

    @Test
    void contextLoads() {
    }

    public static BigDecimal toDecimal(int n) {
        return new BigDecimal(n);
    }

    public static BigDecimal toDecimal(double n) {
        return new BigDecimal("" + n);
    }

    public static Function<Integer, String> testName = n -> "test" + n;

    public static ScoringDataDTO defaultScoringDataDTO() {
        return new ScoringDataDTO(
                toDecimal(200000) /* amount */,
                12, "firstName", "lastName", "middleName",
                Gender.MALE,
                LocalDate.now().minusYears(22) /* age */, "1234", "123456",
                LocalDate.now().plusYears(2), "passportIssueBranch",
                MaritalStatus.WIDOW_WIDOWER, 0,
                new EmploymentDTO(
                        EmploymentStatus.EMPLOYED,
                        "1234567890",
                        toDecimal(30000.) /* salary */,
                        EmploymentPosition.WORKER,
                        24,
                        6
                ), "12345678901234", true, true);
    }

    public static LoanApplicationRequestDTO defaultLoanApplicationRequest() {
        return new LoanApplicationRequestDTO(
                toDecimal(500_000),
                24,
                "Andrei",
                "Andreev",
                "Andreevich",
                "example@gmail.com",
                LocalDate.now().minusYears(30),
                "5735",
                "984510"
        );
    }
}
