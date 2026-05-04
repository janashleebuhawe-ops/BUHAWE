package com.ordering.system.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ordering.system.entity.Labor;
import com.ordering.system.entity.User;
import com.ordering.system.repository.LaborRepository;
import com.ordering.system.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LaborRepository laborRepository;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new User());
        return "user-management";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes ra) {
        try {
            userService.saveUser(user);
            ra.addFlashAttribute("message", "Identity provisioned. Verification email sent.");
        } catch (RuntimeException e) {
            if ("INVALID_INBOX".equals(e.getMessage())) {
                ra.addFlashAttribute("error", "The email account provided does not exist in the real world.");
            } else {
                ra.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            }
        }
        return "redirect:/users";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token,
                              RedirectAttributes ra,
                              HttpServletRequest request) {

        // 1. Verify token and get user
        User user = userService.verifyToken(token);

        if (user != null) {
            // 2. Fix role prefix
            String roleName = user.getRole();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            // 3. Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority(roleName))
                );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 4. Save security context to session
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.getContext());

            return "redirect:/menu";

        } else {
            ra.addFlashAttribute("error", "Invalid or already used verification link.");
            return "redirect:/login";
        }
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Labor linkedLabor = laborRepository.findBySystemAccountId(id);
            if (linkedLabor != null) {
                laborRepository.delete(linkedLabor);
            }
            userService.deleteById(id);
            ra.addFlashAttribute("message", "Identity and associated metrics revoked.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "System Error: Could not break identity tether.");
        }
        return "redirect:/users";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(required = false) String password,
                             RedirectAttributes ra) {
        userService.updateUser(user, password);
        ra.addFlashAttribute("message", "Credentials updated.");
        return "redirect:/users";
    }
}