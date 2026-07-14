package com.example.attendance.leave.service;

import com.example.attendance.department.entity.Department;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.domain.PaidLeaveGrantCalculator;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.entity.LeaveStatus;
import com.example.attendance.leave.entity.LeaveType;
import com.example.attendance.leave.entity.PaidLeaveBalance;
import com.example.attendance.leave.entity.PaidLeaveRequest;
import com.example.attendance.leave.repository.PaidLeaveBalanceRepository;
import com.example.attendance.leave.repository.PaidLeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaidLeaveServiceTest {

    @Mock
    private PaidLeaveRequestRepository requestRepository;
    @Mock
    private PaidLeaveBalanceRepository balanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private PaidLeaveGrantCalculator grantCalculator;
    private PaidLeaveServiceImpl service;
    private Clock clock;

    private Employee employee;
    private Employee manager;
    private Department department;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-07-13T00:00:00Z"), ZoneId.of("Asia/Tokyo"));
        grantCalculator = new PaidLeaveGrantCalculator();
        service = new PaidLeaveServiceImpl(
            requestRepository, balanceRepository, employeeRepository, grantCalculator, clock);

        department = Department.builder()
            .id(UUID.randomUUID())
            .name("開発部")
            .build();

        manager = Employee.builder()
            .id(UUID.randomUUID())
            .name("上長太郎")
            .email("manager@example.com")
            .department(department)
            .role(Role.EMPLOYEE)
            .isManager(true)
            .hireDate(LocalDate.of(2020, 4, 1))
            .build();

        employee = Employee.builder()
            .id(UUID.randomUUID())
            .name("田中太郎")
            .email("tanaka@example.com")
            .department(department)
            .role(Role.EMPLOYEE)
            .isManager(false)
            .hireDate(LocalDate.of(2024, 4, 1))
            .build();
    }

    @Test
    @DisplayName("有給申請: 正常（全日・1日）")
    void create_fullDay_success() {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15),
            LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY,
            "私用のため"
        );
        var balance = createBalance(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO);

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.findOverlapping(employee.getId(), request.startDate(), request.endDate(), LeaveStatus.APPROVED))
            .thenReturn(List.of());
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(employee.getId(), request);

        assertThat(result.totalDays()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
    }

    @Test
    @DisplayName("有給申請: 半休（0.5日）")
    void create_halfDay_success() {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15),
            LocalDate.of(2026, 7, 15),
            LeaveType.AM_HALF,
            "通院のため"
        );
        var balance = createBalance(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO);

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.findOverlapping(employee.getId(), request.startDate(), request.endDate(), LeaveStatus.APPROVED))
            .thenReturn(List.of());
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(employee.getId(), request);

        assertThat(result.totalDays()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("有給申請: 連続日（土日除外）")
    void create_multiDay_excludesWeekends() {
        // 2026-07-06(Mon) 〜 2026-07-12(Sun) = 平日5日
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 6),
            LocalDate.of(2026, 7, 12),
            LeaveType.FULL_DAY,
            "夏季休暇"
        );
        var balance = createBalance(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO);

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.findOverlapping(employee.getId(), request.startDate(), request.endDate(), LeaveStatus.APPROVED))
            .thenReturn(List.of());
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(employee.getId(), request);

        assertThat(result.totalDays()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("有給申請: 残日数不足で400エラー")
    void create_insufficientBalance_throwsException() {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15),
            LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY,
            "私用のため"
        );
        var balance = createBalance(new BigDecimal("1"), BigDecimal.ZERO, new BigDecimal("1"));

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> service.create(employee.getId(), request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("残日数");
    }

    @Test
    @DisplayName("有給申請: 日程重複で409エラー")
    void create_overlapping_throwsException() {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15),
            LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY,
            "私用のため"
        );
        var balance = createBalance(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO);
        var existing = PaidLeaveRequest.builder().id(UUID.randomUUID()).build();

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.findOverlapping(employee.getId(), request.startDate(), request.endDate(), LeaveStatus.APPROVED))
            .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.create(employee.getId(), request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("重複");
    }

    @Test
    @DisplayName("取り下げ: PENDING状態なら成功")
    void cancel_pending_success() {
        var leaveRequest = createPendingRequest();

        when(requestRepository.findById(leaveRequest.getId())).thenReturn(Optional.of(leaveRequest));
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancel(leaveRequest.getId(), employee.getId(), 0L);

        assertThat(result.status()).isEqualTo(LeaveStatus.CANCELLED);
    }

    @Test
    @DisplayName("取り下げ: 承認済みなら400エラー")
    void cancel_approved_throwsException() {
        var leaveRequest = createPendingRequest();
        leaveRequest.setStatus(LeaveStatus.APPROVED);

        when(requestRepository.findById(leaveRequest.getId())).thenReturn(Optional.of(leaveRequest));

        assertThatThrownBy(() -> service.cancel(leaveRequest.getId(), employee.getId(), 0L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("承認: 上長が自部署メンバーの申請を承認")
    void approve_byManager_success() {
        var leaveRequest = createPendingRequest();
        var balance = createBalance(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO);

        when(requestRepository.findById(leaveRequest.getId())).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any(PaidLeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approve(leaveRequest.getId(), manager.getId(), 0L);

        assertThat(result.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("1.0"));
    }

    @Test
    @DisplayName("承認: 上長でない社員が承認しようとすると403エラー")
    void approve_byNonManager_throwsException() {
        var leaveRequest = createPendingRequest();
        var nonManager = Employee.builder()
            .id(UUID.randomUUID())
            .name("一般社員")
            .department(department)
            .isManager(false)
            .build();

        when(requestRepository.findById(leaveRequest.getId())).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(nonManager.getId())).thenReturn(Optional.of(nonManager));

        assertThatThrownBy(() -> service.approve(leaveRequest.getId(), nonManager.getId(), 0L))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("承認: 上長が自分の申請を自己承認")
    void approve_selfApproval_byManager() {
        var managerRequest = PaidLeaveRequest.builder()
            .id(UUID.randomUUID())
            .requester(manager)
            .startDate(LocalDate.of(2026, 7, 15))
            .endDate(LocalDate.of(2026, 7, 15))
            .leaveType(LeaveType.FULL_DAY)
            .reason("私用")
            .status(LeaveStatus.PENDING)
            .totalDays(new BigDecimal("1.0"))
            .version(0L)
            .build();
        var balance = createBalance(new BigDecimal("20"), BigDecimal.ZERO, BigDecimal.ZERO);
        balance.setEmployee(manager);

        when(requestRepository.findById(managerRequest.getId())).thenReturn(Optional.of(managerRequest));
        when(employeeRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(manager.getId(), 2026))
            .thenReturn(Optional.of(balance));
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any(PaidLeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approve(managerRequest.getId(), manager.getId(), 0L);

        assertThat(result.status()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    @DisplayName("却下: 正常")
    void reject_success() {
        var leaveRequest = createPendingRequest();

        when(requestRepository.findById(leaveRequest.getId())).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(requestRepository.save(any(PaidLeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.reject(leaveRequest.getId(), manager.getId(), "業務都合のため", 0L);

        assertThat(result.status()).isEqualTo(LeaveStatus.REJECTED);
    }

    @Test
    @DisplayName("残日数取得: granted + carriedOver - used")
    void getBalance_calculatesCorrectly() {
        var balance = createBalance(new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("3"));

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(balanceRepository.findByEmployeeIdAndFiscalYear(employee.getId(), 2026))
            .thenReturn(Optional.of(balance));

        var result = service.getBalance(employee.getId());

        assertThat(result.grantedDays()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(result.carriedOverDays()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(result.usedDays()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(result.remainingDays()).isEqualByComparingTo(new BigDecimal("12"));
    }

    private PaidLeaveBalance createBalance(BigDecimal granted, BigDecimal carried, BigDecimal used) {
        return PaidLeaveBalance.builder()
            .id(UUID.randomUUID())
            .employee(employee)
            .fiscalYear(2026)
            .grantedDays(granted)
            .carriedOverDays(carried)
            .usedDays(used)
            .version(0L)
            .build();
    }

    private PaidLeaveRequest createPendingRequest() {
        return PaidLeaveRequest.builder()
            .id(UUID.randomUUID())
            .requester(employee)
            .startDate(LocalDate.of(2026, 7, 15))
            .endDate(LocalDate.of(2026, 7, 15))
            .leaveType(LeaveType.FULL_DAY)
            .reason("私用のため")
            .status(LeaveStatus.PENDING)
            .totalDays(new BigDecimal("1.0"))
            .version(0L)
            .build();
    }
}
