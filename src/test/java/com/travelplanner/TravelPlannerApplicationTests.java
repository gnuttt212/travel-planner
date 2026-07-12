package com.travelplanner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TravelPlannerApplicationTests {

    @Test
    void contextLoads() {
        // Test nay chi de dam bao Spring context (tat ca @Bean, @Configuration)
        // khoi tao thanh cong, khong co loi wiring. Day la test "smoke test"
        // nen co ngay tu dau du an.
    }
}
