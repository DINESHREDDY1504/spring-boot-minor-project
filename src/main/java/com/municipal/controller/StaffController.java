package com.municipal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.municipal.model.Staff;
import com.municipal.service.StaffService;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin
public class StaffController {

    @Autowired
    private StaffService staffService;

    // 🔹 REGISTER (UNCHANGED)
    @PostMapping("/register")
    public Staff register(@RequestBody Staff staff) {
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

    // 🔥 NEW: LOGOUT API
    @PostMapping("/logout")
    public String logout(@RequestBody Staff staff) {

        staffService.logout(staff.getUsername());

        return "Logout successful";
    }
}
