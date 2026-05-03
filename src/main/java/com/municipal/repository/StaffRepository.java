package com.municipal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.municipal.model.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Case-insensitive search
    Staff findByUsernameIgnoreCase(String username);
}