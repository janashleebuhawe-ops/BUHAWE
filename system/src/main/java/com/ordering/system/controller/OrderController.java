package com.ordering.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordering.system.entity.Item;
import com.ordering.system.entity.Orders;
import com.ordering.system.repository.ItemRepository;
import com.ordering.system.repository.OrderRepository;
import com.ordering.system.service.OrderService;

import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/orders")
    public String showOrderPage(Model model) {
        List<Item> items = itemRepository.findByActiveTrue();
        List<Orders> orders = orderService.getAllOrders();
        System.out.println("DEBUG: Found " + items.size() + " active items for the POS.");
        model.addAttribute("items", items);
        model.addAttribute("orders", orders);
        return "order-management";
    }

    @PostMapping("/orders/add")
    public String addOrder(
            @RequestParam String customerName,
            @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
            @RequestParam(value = "quantities", required = false) List<Integer> quantities,
            @RequestParam(defaultValue = "CASH") String paymentMethod,
            @RequestParam(required = false) Double amountPaid,
            RedirectAttributes ra) {

        if (itemIds == null || itemIds.isEmpty() || quantities == null) {
            ra.addFlashAttribute("error", "Your cart is empty!");
            return "redirect:/orders";
        }

        try {
            Orders order = orderService.placeOrder(
                customerName, itemIds, quantities, paymentMethod, amountPaid);

            // Mark as COMPLETED immediately since payment is done at the terminal
            order.setStatus("COMPLETED");
            orderRepository.save(order);

            // Pass receipt data for modal
            ra.addFlashAttribute("receiptOrderId", order.getId());
            ra.addFlashAttribute("receiptCustomer", order.getCustomerName());
            ra.addFlashAttribute("receiptTotal", order.getTotalAmount());
            ra.addFlashAttribute("receiptPayment", order.getPaymentMethod());
            ra.addFlashAttribute("receiptAmountPaid", order.getAmountPaid());
            ra.addFlashAttribute("receiptChange", order.getChangeAmount());
            ra.addFlashAttribute("success", "Order placed successfully!");

        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders";
    }

    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(id);
            ra.addFlashAttribute("success", "Order cancelled and stock returned!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error cancelling order: " + e.getMessage());
        }
        return "redirect:/orders";
    }
}