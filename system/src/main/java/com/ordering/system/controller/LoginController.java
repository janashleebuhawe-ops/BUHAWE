package com.ordering.system.controller;

import com.ordering.system.entity.User;
import com.ordering.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ✅ Added this import

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("newUser", new User());
        return "registration";
    }

    // ✅ Place the code here
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("newUser") User user, RedirectAttributes ra) {
        try {
            userService.saveUser(user); 
            // If successful, send them to login with a success message
            ra.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
            return "redirect:/login?success";
        } catch (RuntimeException e) {
            // If the email is fake or taken, send them back to the register page with the error
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}