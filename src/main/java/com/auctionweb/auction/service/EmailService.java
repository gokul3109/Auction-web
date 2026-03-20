package com.auctionweb.auction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your Auction Web password");
        message.setText(
            "Hi,\n\n" +
            "We received a request to reset your password.\n\n" +
            "Click the link below to reset it (valid for 15 minutes):\n" +
            resetLink + "\n\n" +
            "If you didn't request this, you can safely ignore this email.\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }

    public void sendOutbidEmail(String toEmail, String auctionTitle, String auctionUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("You've been outbid on \"" + auctionTitle + "\"");
        message.setText(
            "Hi,\n\n" +
            "Someone placed a higher bid on \"" + auctionTitle + "\".\n\n" +
            "Bid again to stay in the lead:\n" +
            auctionUrl + "\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }

    public void sendAuctionWonEmail(String toEmail, String auctionTitle, String amount, String auctionUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Congratulations! You won \"" + auctionTitle + "\"");
        message.setText(
            "Hi,\n\n" +
            "Congratulations! You won the auction for \"" + auctionTitle + "\" with a bid of $" + amount + ".\n\n" +
            "View your auction:\n" +
            auctionUrl + "\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }

    public void sendAuctionSoldEmail(String toEmail, String auctionTitle, String amount, String auctionUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your auction \"" + auctionTitle + "\" has sold");
        message.setText(
            "Hi,\n\n" +
            "Your auction \"" + auctionTitle + "\" has ended and sold for $" + amount + ".\n\n" +
            "View the auction:\n" +
            auctionUrl + "\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }

    public void sendBidReceivedEmail(String toEmail, String auctionTitle, String amount, String auctionUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("New bid on your auction \"" + auctionTitle + "\"");
        message.setText(
            "Hi,\n\n" +
            "Someone placed a bid of $" + amount + " on your auction \"" + auctionTitle + "\".\n\n" +
            "View your auction:\n" +
            auctionUrl + "\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }

    public void sendAuctionNoBidsEmail(String toEmail, String auctionTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your auction \"" + auctionTitle + "\" ended with no bids");
        message.setText(
            "Hi,\n\n" +
            "Your auction \"" + auctionTitle + "\" ended but received no bids.\n\n" +
            "Consider relisting it with a lower starting price.\n\n" +
            "— Auction Web Team"
        );
        mailSender.send(message);
    }
}
