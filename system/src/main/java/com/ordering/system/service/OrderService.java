package com.ordering.system.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordering.system.entity.Item;
import com.ordering.system.entity.OrderItem;
import com.ordering.system.entity.Orders;
import com.ordering.system.repository.ItemRepository;
import com.ordering.system.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Orders placeOrder(String customerName,
                             List<Long> itemIds,
                             List<Integer> quantities,
                             String paymentMethod,
                             Double amountPaid) {

        // 1. Create parent order
        Orders order = new Orders();
        order.setCustomerName(customerName);
        order.setStatus("PENDING");
        order.setPaymentMethod(paymentMethod);

        double grandTotal = 0;

        // 2. Process each item
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            Integer qty = quantities.get(i);

            if (qty == null || qty <= 0) continue;

            Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

            // Check stock
            if (item.getStock() < qty) {
                throw new RuntimeException("Not enough stock for: " + item.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(qty);
            order.addOrderItem(orderItem);

            grandTotal += item.getPrice() * qty;

            // Deduct stock
            item.setStock(item.getStock() - qty);
            itemRepository.save(item);
        }

        order.setTotalAmount(grandTotal);

        // 3. Handle payment
        if ("CASH".equals(paymentMethod)) {
            if (amountPaid == null || amountPaid < grandTotal) {
                throw new RuntimeException(
                    "Amount paid is less than total! Total is ₱" + String.format("%.2f", grandTotal));
            }
            order.setAmountPaid(amountPaid);
            order.setChangeAmount(amountPaid - grandTotal);
        } else {
            // GCash / Card — exact amount, no change
            order.setAmountPaid(grandTotal);
            order.setChangeAmount(0.0);
        }

        // 4. Save and return
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Orders order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // Return stock
        for (OrderItem detail : order.getOrderItems()) {
            Item item = detail.getItem();
            item.setStock(item.getStock() + detail.getQuantity());
            itemRepository.save(item);
        }

        orderRepository.delete(order);
    }

    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }
}
