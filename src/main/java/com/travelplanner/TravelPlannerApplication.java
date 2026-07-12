package com.travelplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point cua ung dung.
 *
 * Kien truc: Modular Monolith - moi module nghiep vu (itinerary, budget,
 * collaboration, booking) duoc to chuc thanh package rieng, co ranh gioi
 * ro rang (domain / dto / repository / service / controller). Cach nay
 * giup de bao tri o giai doan dau, va van co the tach thanh microservices
 * sau nay neu can, vi cac module da duoc thiet ke it phu thuoc cheo nhau.
 */
@SpringBootApplication
@EnableJpaAuditing // cho phep tu dong set createdAt/updatedAt trong BaseEntity
public class TravelPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelPlannerApplication.class, args);
    }
}
