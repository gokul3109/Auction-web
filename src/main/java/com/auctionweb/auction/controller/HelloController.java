package com.auctionweb.auction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Auction Web API! Server is running successfully.";
    }

    @GetMapping("/api/health")
    public String health() {
        return "{\"status\": \"Server is healthy\"}";
    }

}
