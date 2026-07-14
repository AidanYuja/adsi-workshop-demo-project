package com.example.attendance.leave.repository;

import com.example.attendance.leave.entity.LeaveStatus;
import com.example.attendance.leave.entity.PaidLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaidLeaveRequestRepository extends JpaRepository<PaidLeaveRequest, UUID> {

    @Query("SELECT r FROM PaidLeaveRequest r JOIN FETCH r.requester LEFT JOIN FETCH r.approver WHERE r.requester.id = :requesterId ORDER BY r.startDate DESC")
    List<PaidLeaveRequest> findByRequesterIdOrderByStartDateDesc(@Param("requesterId") UUID requesterId);

    @Query("SELECT r FROM PaidLeaveRequest r JOIN FETCH r.requester LEFT JOIN FETCH r.approver WHERE r.requester.id = :requesterId AND r.status = :status ORDER BY r.startDate DESC")
    List<PaidLeaveRequest> findByRequesterIdAndStatusOrderByStartDateDesc(@Param("requesterId") UUID requesterId, @Param("status") LeaveStatus status);

    @Query("SELECT r FROM PaidLeaveRequest r JOIN FETCH r.requester WHERE r.requester.department.id = :departmentId AND r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<PaidLeaveRequest> findPendingByDepartment(@Param("departmentId") UUID departmentId);

    @Query("SELECT r FROM PaidLeaveRequest r WHERE r.requester.id = :employeeId AND r.status = :status " +
           "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<PaidLeaveRequest> findOverlapping(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") LeaveStatus status);
}
