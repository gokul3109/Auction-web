package com.auctionweb.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuctionwebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionwebApplication.class, args);
    }

}
