package com.municipal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.municipal.model.StaffLog;

public interface StaffLogRepository extends JpaRepository<StaffLog, Long> {

    StaffLog findTopByUsernameOrderByLoginTimeDesc(String username);
}
