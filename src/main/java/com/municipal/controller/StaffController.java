package com.municipal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.municipal.model.Staff;
import com.municipal.service.StaffService;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    // 🔹 REGISTER (UPDATED WITH SECRET CHECK)
    @PostMapping("/register")
    public Staff register(@RequestBody Staff staff) {

        // 🔐 Get secret from request
        String userSecret = staff.getSecretCode();

        // 🔐 Get actual secret from ENV
        String ADMIN_SECRET = System.getenv("STAFF_SECRET");

        // ❌ Invalid secret → block
        if (ADMIN_SECRET == null || userSecret == null || !ADMIN_SECRET.equals(userSecret.trim())) {
            throw new RuntimeException("Invalid Secret Code");
        }

        // ❌ Duplicate username → block
        if (staffService.existsByUsername(staff.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // ✅ Continue original logic
        return staffService.registerStaff(staff);
    }

    // 🔹 LOGIN (UNCHANGED)
    @PostMapping("/login")
    public String login(@RequestBody Staff staff) {

        Staff logged = staffService.login(
                staff.getUsername(),
                staff.getPassword()
        );

        if (logged != null) {
            return "Login Success! Role: " + logged.getRole();
        } else {
            throw new RuntimeException("Invalid Login");
        }
    }

    // 🔥 LOGOUT (UNCHANGED)
    @PostMapping("/logout")
    public String logout(@RequestBody Staff staff) {

        staffService.logout(staff.getUsername());

        return "Logout successful";
    }
}
