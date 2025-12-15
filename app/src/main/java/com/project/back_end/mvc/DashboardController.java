package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller for role-based dashboards.
 * Returns Thymeleaf view names (not JSON).
 */
@Controller
public class DashboardController {

    private final Service sharedService;

    /**
     * Autowire the shared service that provides token validation logic.
     */
    @Autowired
    public DashboardController(Service sharedService) {
        this.sharedService = sharedService;
    }

    /**
     * Admin Dashboard:
     * GET /adminDashboard/{token}
     * - Validates token for "admin" role.
     * - If valid => returns "admin/adminDashboard" Thymeleaf view.
     * - If invalid => redirects to root (login/home).
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token, Model model) {
        boolean valid = sharedService.validateTokenForRole(token, "admin");
        if (!valid) {
            return "redirect:/";
        }
        // Optional: add attributes for Thymeleaf template
        model.addAttribute("role", "admin");
        model.addAttribute("username", sharedService.extractUsername(token));
        return "admin/adminDashboard";
    }

    /**
     * Doctor Dashboard:
     * GET /doctorDashboard/{token}
     * - Validates token for "doctor" role.
     * - If valid => returns "doctor/doctorDashboard" Thymeleaf view.
     * - If invalid => redirects to root (login/home).
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token, Model model) {
        boolean valid = sharedService.validateTokenForRole(token, "doctor");
        if (!valid) {
            return "redirect:/";
        }
               // Optional: add attributes for Thymeleaf template
        model.addAttribute("role", "doctor");
        model.addAttribute("username", sharedService.extractUsername(token));
        model.addAttribute("doctorId", sharedService.extractPrincipalId(token)); // if applicable
        return "doctor/doctorDashboard";
    }
}