package com.municipal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.municipal.model.Staff;
import com.municipal.service.StaffService;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    // ✅ Get secret from application.properties (linked to ENV)
    @Value("${staff.registration.secret}")
    private String ADMIN_SECRET;

    // 🔹 REGISTER (UPDATED WITH SECRET CHECK)
    @PostMapping("/register")
    // @PostMapping("/register")
    public String register(@RequestBody Staff staff) {

        String userSecret = staff.getSecretCode();

        if (ADMIN_SECRET == null || userSecret == null || !ADMIN_SECRET.equals(userSecret.trim())) {
            throw new RuntimeException("Invalid Secret Code");
        }

        if (staffService.existsByUsername(staff.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        staffService.registerStaff(staff);

        return "Staff Registered Successfully";
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
