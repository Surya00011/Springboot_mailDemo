package com.mail.mail_demoapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/home")
    public String home() {
        System.out.println("home from main");
        return "Welcome to homePage";
    }
}
