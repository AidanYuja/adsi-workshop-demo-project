package com.example.attendance.leave.service;

import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.domain.PaidLeaveGrantCalculator;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;
import com.example.attendance.leave.entity.LeaveStatus;
import com.example.attendance.leave.entity.PaidLeaveBalance;
import com.example.attendance.leave.entity.PaidLeaveRequest;
import com.example.attendance.leave.repository.PaidLeaveBalanceRepository;
import com.example.attendance.leave.repository.PaidLeaveRequestRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaidLeaveServiceImpl implements PaidLeaveService {

    private final PaidLeaveRequestRepository requestRepository;
    private final PaidLeaveBalanceRepository balanceRepository;
    private final EmployeeRepository employeeRepository;
    private final PaidLeaveGrantCalculator grantCalculator;
    private final Clock clock;

    public PaidLeaveServiceImpl(
            PaidLeaveRequestRepository requestRepository,
            PaidLeaveBalanceRepository balanceRepository,
            EmployeeRepository employeeRepository,
            PaidLeaveGrantCalculator grantCalculator,
            Clock clock) {
        this.requestRepository = requestRepository;
        this.balanceRepository = balanceRepository;
        this.employeeRepository = employeeRepository;
        this.grantCalculator = grantCalculator;
        this.clock = clock;
    }

    @Override
    public LeaveRequestResponse create(UUID requesterId, LeaveRequestCreateRequest request) {
        var employee = findEmployee(requesterId);
        int fiscalYear = request.startDate().getYear();
        var balance = findOrCreateBalance(employee, fiscalYear);

        BigDecimal totalDays = calculateTotalDays(request);

        if (balance.getRemainingDays().compareTo(totalDays) < 0) {
            throw new IllegalStateException("有給残日数が不足しています");
        }

        var overlapping = requestRepository.findOverlapping(
            requesterId, request.startDate(), request.endDate(), LeaveStatus.APPROVED);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("指定期間に既に承認済みの有給が重複しています");
        }

        var leaveRequest = PaidLeaveRequest.builder()
            .id(UuidCreator.getTimeOrderedEpoch())
            .requester(employee)
            .startDate(request.startDate())
            .endDate(request.endDate())
            .leaveType(request.leaveType())
            .reason(request.reason())
            .status(LeaveStatus.PENDING)
            .totalDays(totalDays)
            .build();

        var saved = requestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findByRequester(UUID requesterId) {
        return requestRepository.findByRequesterIdOrderByStartDateDesc(requesterId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public LeaveRequestResponse cancel(UUID leaveRequestId, UUID requesterId, Long version) {
        var leaveRequest = findRequest(leaveRequestId);

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("申請中の有給のみ取り下げ可能です");
        }
        if (!leaveRequest.getRequester().getId().equals(requesterId)) {
            throw new SecurityException("自分の申請のみ取り下げ可能です");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        var saved = requestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingLeaveResponse> findPending(UUID managerId) {
        var manager = findEmployee(managerId);
        var departmentId = manager.getDepartment().getId();

        return requestRepository.findPendingByDepartment(departmentId)
            .stream()
            .map(this::toPendingResponse)
            .toList();
    }

    @Override
    public LeaveRequestResponse approve(UUID leaveRequestId, UUID approverId, Long version) {
        var leaveRequest = findRequest(leaveRequestId);
        var approver = findEmployee(approverId);

        validateApprover(approver, leaveRequest);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprover(approver);

        int fiscalYear = leaveRequest.getStartDate().getYear();
        var balance = findOrCreateBalance(leaveRequest.getRequester(), fiscalYear);
        balance.setUsedDays(balance.getUsedDays().add(leaveRequest.getTotalDays()));
        balanceRepository.save(balance);

        var saved = requestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    public LeaveRequestResponse reject(UUID leaveRequestId, UUID approverId, String reason, Long version) {
        var leaveRequest = findRequest(leaveRequestId);
        var approver = findEmployee(approverId);

        validateApprover(approver, leaveRequest);

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprover(approver);

        var saved = requestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(UUID employeeId) {
        var employee = findEmployee(employeeId);
        int fiscalYear = LocalDate.now(clock).getYear();
        var balance = findOrCreateBalance(employee, fiscalYear);

        return new LeaveBalanceResponse(
            balance.getFiscalYear(),
            balance.getGrantedDays(),
            balance.getCarriedOverDays(),
            balance.getUsedDays(),
            balance.getRemainingDays()
        );
    }

    private BigDecimal calculateTotalDays(LeaveRequestCreateRequest request) {
        long weekdays = grantCalculator.countWeekdays(request.startDate(), request.endDate());
        return request.leaveType().getDaysPerUnit().multiply(new BigDecimal(weekdays));
    }

    private void validateApprover(Employee approver, PaidLeaveRequest leaveRequest) {
        var requester = leaveRequest.getRequester();
        boolean isSelfApproval = approver.getId().equals(requester.getId()) && approver.isManager();
        boolean isDepartmentManager = approver.isManager()
            && approver.getDepartment().getId().equals(requester.getDepartment().getId());

        if (!isSelfApproval && !isDepartmentManager) {
            throw new SecurityException("承認権限がありません");
        }
    }

    private PaidLeaveBalance findOrCreateBalance(Employee employee, int fiscalYear) {
        return balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), fiscalYear)
            .orElseGet(() -> {
                var referenceDate = LocalDate.now(clock);
                var granted = grantCalculator.calculate(employee.getHireDate(), referenceDate);
                var balance = PaidLeaveBalance.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .employee(employee)
                    .fiscalYear(fiscalYear)
                    .grantedDays(granted)
                    .carriedOverDays(BigDecimal.ZERO)
                    .usedDays(BigDecimal.ZERO)
                    .build();
                return balanceRepository.save(balance);
            });
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません: " + id));
    }

    private PaidLeaveRequest findRequest(UUID id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("有給申請が見つかりません: " + id));
    }

    private LeaveRequestResponse toResponse(PaidLeaveRequest request) {
        return new LeaveRequestResponse(
            request.getId(),
            request.getRequester().getId(),
            request.getRequester().getName(),
            request.getStartDate(),
            request.getEndDate(),
            request.getLeaveType(),
            request.getReason(),
            request.getStatus(),
            request.getTotalDays(),
            request.getApprover() != null ? request.getApprover().getName() : null,
            request.getCreatedAt()
        );
    }

    private PendingLeaveResponse toPendingResponse(PaidLeaveRequest request) {
        return new PendingLeaveResponse(
            request.getId(),
            request.getRequester().getId(),
            request.getRequester().getName(),
            request.getStartDate(),
            request.getEndDate(),
            request.getLeaveType(),
            request.getReason(),
            request.getTotalDays(),
            request.getCreatedAt(),
            request.getVersion()
        );
    }
}
