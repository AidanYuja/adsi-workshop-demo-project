package com.example.attendance.leave.repository;

import com.example.attendance.leave.entity.PaidLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaidLeaveBalanceRepository extends JpaRepository<PaidLeaveBalance, UUID> {

    Optional<PaidLeaveBalance> findByEmployeeIdAndFiscalYear(UUID employeeId, int fiscalYear);
}
