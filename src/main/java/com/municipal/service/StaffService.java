package com.municipal.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.municipal.model.Staff;
import com.municipal.model.StaffLog;
import com.municipal.repository.StaffLogRepository;
import com.municipal.repository.StaffRepository;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    // 🔥 NEW
    @Autowired
    private StaffLogRepository staffLogRepository;

    // REGISTER (UNCHANGED)
    public Staff registerStaff(Staff staff) {

        staff.setUsername(staff.getUsername().trim());
        staff.setPassword(staff.getPassword().trim());

        if (staff.getEmployeeId() == null || staff.getEmployeeId().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (!staff.getRole().equals("AUTHORITY") && !staff.getRole().equals("TECHNICAL")) {
            throw new RuntimeException("Invalid Role");
        }

        return staffRepository.save(staff);
    }

    // LOGIN (UPDATED WITH LOGGING)
    public Staff login(String username, String password) {

        username = username.trim();
        password = password.trim();

        System.out.println("INPUT USERNAME: [" + username + "]");
        System.out.println("INPUT PASSWORD: [" + password + "]");

        Staff staff = staffRepository.findByUsernameIgnoreCase(username);

        if (staff == null) {
            System.out.println("❌ USER NOT FOUND");
            return null;
        }

        System.out.println("DB USERNAME: [" + staff.getUsername() + "]");
        System.out.println("DB PASSWORD: [" + staff.getPassword() + "]");

        if (staff.getPassword().equals(password)) {

            System.out.println("✅ LOGIN SUCCESS");

            // 🔥 SAVE LOGIN HISTORY
            StaffLog log = new StaffLog();
            log.setUsername(staff.getUsername());
            log.setRole(staff.getRole());
            log.setLoginTime(LocalDateTime.now());

            staffLogRepository.save(log);

            return staff;
        } else {
            System.out.println("❌ PASSWORD MISMATCH");
        }

        return null;
    }

    // 🔥 NEW LOGOUT METHOD
    public void logout(String username) {

        StaffLog log = staffLogRepository
                .findTopByUsernameOrderByLoginTimeDesc(username);

        if (log != null) {
            log.setLogoutTime(LocalDateTime.now());
            staffLogRepository.save(log);
        }
    }
}
