package com.example.attendance.leave.controller;

import com.example.attendance.common.config.security.EmployeeUserDetails;
import com.example.attendance.employee.entity.Role;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;
import com.example.attendance.leave.entity.LeaveStatus;
import com.example.attendance.leave.entity.LeaveType;
import com.example.attendance.leave.service.PaidLeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaidLeaveController.class)
class PaidLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaidLeaveService paidLeaveService;

    private final UUID employeeId = UUID.randomUUID();
    private final UUID leaveId = UUID.randomUUID();
    private EmployeeUserDetails userDetails;

    @BeforeEach
    void setUp() {
        var info = new EmployeeUserDetails.EmployeeInfo(
            employeeId, "田中太郎", UUID.randomUUID(), "開発部", Role.EMPLOYEE, true);
        userDetails = new EmployeeUserDetails(
            "tanaka@example.com", "password", true,
            List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), info);
    }

    @Test
    @DisplayName("POST /api/leaves: 有給申請を作成できる")
    void create_returns201() throws Exception {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用のため");
        var response = createResponse(LeaveStatus.PENDING);

        when(paidLeaveService.create(eq(employeeId), any())).thenReturn(response);

        mockMvc.perform(post("/api/leaves")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalDays").value(1.0));
    }

    @Test
    @DisplayName("GET /api/leaves: 自分の申請一覧を取得できる")
    void findByRequester_returns200() throws Exception {
        var response = createResponse(LeaveStatus.PENDING);
        when(paidLeaveService.findByRequester(employeeId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves").with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("PATCH /api/leaves/{id}/cancel: 取り下げできる")
    void cancel_returns200() throws Exception {
        var response = createResponse(LeaveStatus.CANCELLED);
        when(paidLeaveService.cancel(eq(leaveId), eq(employeeId), eq(0L))).thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/cancel", leaveId)
                .with(user(userDetails))
                .param("version", "0")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /api/leaves/pending: 承認待ち一覧を取得できる")
    void findPending_returns200() throws Exception {
        var pending = new PendingLeaveResponse(
            leaveId, UUID.randomUUID(), "田中太郎",
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用", new BigDecimal("1.0"),
            Instant.now(), 0L);
        when(paidLeaveService.findPending(employeeId)).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/leaves/pending").with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].requesterName").value("田中太郎"));
    }

    @Test
    @DisplayName("PATCH /api/leaves/{id}/approve: 承認できる")
    void approve_returns200() throws Exception {
        var response = createResponse(LeaveStatus.APPROVED);
        when(paidLeaveService.approve(eq(leaveId), eq(employeeId), eq(0L))).thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/approve", leaveId)
                .with(user(userDetails))
                .param("version", "0")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PATCH /api/leaves/{id}/reject: 却下できる")
    void reject_returns200() throws Exception {
        var response = createResponse(LeaveStatus.REJECTED);
        when(paidLeaveService.reject(eq(leaveId), eq(employeeId), eq("業務都合"), eq(0L)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/reject", leaveId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"業務都合\",\"version\":0}")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /api/leaves/balance: 残日数を取得できる")
    void getBalance_returns200() throws Exception {
        var balance = new LeaveBalanceResponse(
            2026, new BigDecimal("10"), new BigDecimal("5"),
            new BigDecimal("3"), new BigDecimal("12"));
        when(paidLeaveService.getBalance(employeeId)).thenReturn(balance);

        mockMvc.perform(get("/api/leaves/balance").with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingDays").value(12));
    }

    @Test
    @DisplayName("未認証ユーザーは401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/leaves"))
            .andExpect(status().isUnauthorized());
    }

    private LeaveRequestResponse createResponse(LeaveStatus status) {
        return new LeaveRequestResponse(
            leaveId, employeeId, "田中太郎",
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用のため", status,
            new BigDecimal("1.0"), null, 0L, Instant.now());
    }
}
