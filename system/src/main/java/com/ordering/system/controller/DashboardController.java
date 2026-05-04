package com.ordering.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordering.system.entity.Orders;
import com.ordering.system.repository.ItemRepository;
import com.ordering.system.repository.LaborRepository;
import com.ordering.system.repository.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private LaborRepository laborRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        List<Orders> allOrders = orderRepository.findAll();

        // --- 1. KPI DATA ---
        double totalSales = allOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().isAfter(twentyFourHoursAgo))
                .mapToDouble(o -> o.getTotalAmount())
                .sum();

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("orderCount", allOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().isAfter(twentyFourHoursAgo))
                .count());

        // --- 2. REVENUE TREND (Last 7 Days) ---
        Map<String, Double> last7DaysSales = new LinkedHashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String label = date.format(dayFormatter);
            double dayTotal = allOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().equals(date))
                .mapToDouble(o -> o.getTotalAmount())
                .sum();
            last7DaysSales.put(label, dayTotal);
        }
        model.addAttribute("chartLabels", new ArrayList<>(last7DaysSales.keySet()));
        model.addAttribute("chartData", new ArrayList<>(last7DaysSales.values()));

        // --- 3. PAYMENT METHODS ---
        long cash = allOrders.stream()
                .filter(o -> "CASH".equalsIgnoreCase(o.getPaymentMethod())).count();
        long gcash = allOrders.stream()
                .filter(o -> "GCASH".equalsIgnoreCase(o.getPaymentMethod())).count();
        long card = allOrders.stream()
                .filter(o -> "CARD".equalsIgnoreCase(o.getPaymentMethod())).count();

        model.addAttribute("cashCount", cash);
        model.addAttribute("gcashCount", gcash);
        model.addAttribute("cardCount", card);

        // --- 4. TOP SELLING ITEMS (FIXED - sums actual quantity, not occurrences) ---
        Map<String, Long> topItems = allOrders.stream()
                .filter(o -> o.getOrderItems() != null)
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        oi -> oi.getItem().getName(),
                        Collectors.summingLong(oi -> oi.getQuantity()) // ✅ FIXED
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        model.addAttribute("topItems", topItems);

        // --- 5. LABOR ---
        model.addAttribute("activeLaborCount", laborRepository.count());
        model.addAttribute("lowStock", itemRepository.findAll().stream()
                .filter(i -> i.getStock() < 10).count());

        // --- 6. RECENT ORDERS ---
        model.addAttribute("recentOrders", allOrders.stream()
                .filter(o -> o.getOrderDate() != null)
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(7)
                .collect(Collectors.toList()));

        return "dashboard";
    }
}