package com.ordering.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    // This is perfect for PageController - general navigation
    @GetMapping("/menu")
    public String menu() {
        return "menu"; 
    }

    // Redirect the root path to login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}