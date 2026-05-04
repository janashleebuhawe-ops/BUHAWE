package com.ordering.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordering.system.entity.Labor;
import com.ordering.system.entity.User;
import com.ordering.system.repository.LaborRepository;
import com.ordering.system.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/labor")
@RequiredArgsConstructor
public class LaborController {

    private final LaborRepository laborRepository;
    private final UserService userService;

    @GetMapping
    public String listLabor(Model model) {
        List<Labor> allLabor = laborRepository.findAll().stream()
                .sorted(Comparator.comparing(Labor::getId).reversed())
                .collect(Collectors.toList());

        for (Labor l : allLabor) {
            if (l.getActiveHours() == null) {
                l.setActiveHours(0.0);
            }
        }

        model.addAttribute("labors", allLabor);

        List<Long> linkedUserIds = allLabor.stream()
                .filter(l -> l.getSystemAccount() != null)
                .map(l -> l.getSystemAccount().getId())
                .collect(Collectors.toList());

        List<User> availableUsers = userService.getAllUsers().stream()
                .filter(u -> !linkedUserIds.contains(u.getId()))
                .collect(Collectors.toList());

        model.addAttribute("availableUsers", availableUsers);
        return "labor-management";
    }

    @PostMapping("/activate")
    public String activateWorker(@RequestParam Long userId,
                                 @RequestParam String position,
                                 @RequestParam Double dailyWage,
                                 RedirectAttributes ra) {
        User user = userService.findById(userId);
        if (user != null) {
            Labor labor = new Labor();
            labor.setSystemAccount(user);
            labor.setName(user.getUsername());
            labor.setPosition(position);
            labor.setDailyWage(dailyWage);
            labor.setActive(false); // inactive until shift is started via Edit
            labor.setActiveHours(0.0);
            labor.setShiftStartedAt(null);
            laborRepository.save(labor);
            ra.addFlashAttribute("message", "Worker profile created for " + user.getUsername());
        }
        return "redirect:/labor";
    }

    @PostMapping("/update")
    public String updateLabor(@ModelAttribute Labor labor, RedirectAttributes ra) {
        Labor existing = laborRepository.findById(labor.getId()).orElse(null);
        if (existing != null) {
            existing.setPosition(labor.getPosition());
            existing.setDailyWage(labor.getDailyWage());
            existing.setActiveHours(labor.getActiveHours() != null ? labor.getActiveHours() : 0.0);

            boolean wasActive = existing.isActive();
            boolean nowActive = labor.isActive();

            if (!wasActive && nowActive) {
                // Became active — start the shift timer
                existing.setActive(true);
                existing.setShiftStartedAt(Instant.now());

            } else if (wasActive && !nowActive) {
                // Became inactive — accumulate elapsed hours and clear timer
                if (existing.getShiftStartedAt() != null) {
                    Duration duration = Duration.between(existing.getShiftStartedAt(), Instant.now());
                    double hoursWorked = duration.toSeconds() / 3600.0;
                    existing.setActiveHours(existing.getActiveHours() + hoursWorked);
                }
                existing.setActive(false);
                existing.setShiftStartedAt(null);

            } else {
                // No status change, just update fields
                existing.setActive(nowActive);
            }

            laborRepository.save(existing);
        }
        ra.addFlashAttribute("message", "Metrics updated successfully.");
        return "redirect:/labor";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        Labor labor = laborRepository.findById(id).orElse(null);
        if (labor != null) {
            if (!labor.isActive()) {
                // Activate — start shift timer
                labor.setActive(true);
                labor.setShiftStartedAt(Instant.now());
                ra.addFlashAttribute("message", labor.getName() + " is now Active.");
            } else {
                // Deactivate — accumulate hours and clear timer
                if (labor.getShiftStartedAt() != null) {
                    Duration duration = Duration.between(labor.getShiftStartedAt(), Instant.now());
                    double hoursWorked = duration.toSeconds() / 3600.0;
                    labor.setActiveHours(labor.getActiveHours() + hoursWorked);
                }
                labor.setActive(false);
                labor.setShiftStartedAt(null);
                ra.addFlashAttribute("message", labor.getName() + " shift ended.");
            }
            laborRepository.save(labor);
        }
        return "redirect:/labor";
    }
}