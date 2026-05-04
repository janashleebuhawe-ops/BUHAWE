package com.ordering.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordering.system.entity.Item;
import com.ordering.system.repository.ItemRepository;

import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    @GetMapping
    public String listItems(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Item> items;
        // Only show items where active is TRUE
        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByActiveTrueAndNameContainingIgnoreCase(search);
        } else {
            items = itemRepository.findByActiveTrue();
        }
        model.addAttribute("items", items);
        model.addAttribute("newItem", new Item());
        return "item-management";
    }

    @GetMapping("/archive")
    public String showArchive(Model model) {
        // Show only cancelled items
        model.addAttribute("items", itemRepository.findByActiveFalse());
        return "item-archive";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes ra) {
        item.setActive(true); // New items are active by default
        itemRepository.save(item);
        ra.addFlashAttribute("message", "Item successfully added!");
        return "redirect:/items";
    }

    @GetMapping("/delete/{id}")
    public String cancelProduct(@PathVariable Long id, RedirectAttributes ra) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setActive(false); // Switch to inactive
            itemRepository.save(item);
            ra.addFlashAttribute("message", "Product has been cancelled/hidden.");
        }
        return "redirect:/items";
    }

    @GetMapping("/restore/{id}")
    public String restoreProduct(@PathVariable Long id, RedirectAttributes ra) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setActive(true); // Switch back to active
            itemRepository.save(item);
            ra.addFlashAttribute("message", "Product reactivated successfully!");
        }
        return "redirect:/items";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
        model.addAttribute("item", item);
        return "edit-item";
    }

    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable Long id, @ModelAttribute Item item, RedirectAttributes ra) {
        item.setId(id);
        item.setActive(true); 
        itemRepository.save(item);
        ra.addFlashAttribute("message", "Item updated successfully!");
        return "redirect:/items";
    }
    @PostMapping("/update")
    public String updateItem(@ModelAttribute("item") Item item, RedirectAttributes ra) {
        // This will save all fields, including the new description
        itemRepository.save(item);
        ra.addFlashAttribute("message", "Product updated successfully!");
        return "redirect:/items";
    }
}