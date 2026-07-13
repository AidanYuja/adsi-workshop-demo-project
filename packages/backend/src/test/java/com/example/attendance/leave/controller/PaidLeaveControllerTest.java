package com.example.attendance.leave.controller;

import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.leave.dto.PendingLeaveResponse;
import com.example.attendance.leave.entity.LeaveStatus;
import com.example.attendance.leave.entity.LeaveType;
import com.example.attendance.leave.service.PaidLeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

    private final UUID requesterId = UUID.randomUUID();
    private final UUID leaveId = UUID.randomUUID();

    @Test
    @WithMockUser
    @DisplayName("POST /api/leaves: 有給申請を作成できる")
    void create_returns201() throws Exception {
        var request = new LeaveRequestCreateRequest(
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用のため");
        var response = createResponse(LeaveStatus.PENDING);

        when(paidLeaveService.create(eq(requesterId), any())).thenReturn(response);

        mockMvc.perform(post("/api/leaves")
                .param("requesterId", requesterId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalDays").value(1.0));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/leaves: 自分の申請一覧を取得できる")
    void findByRequester_returns200() throws Exception {
        var response = createResponse(LeaveStatus.PENDING);
        when(paidLeaveService.findByRequester(requesterId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves")
                .param("requesterId", requesterId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/leaves/{id}/cancel: 取り下げできる")
    void cancel_returns200() throws Exception {
        var response = createResponse(LeaveStatus.CANCELLED);
        when(paidLeaveService.cancel(eq(leaveId), eq(requesterId), eq(0L))).thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/cancel", leaveId)
                .param("requesterId", requesterId.toString())
                .param("version", "0")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/leaves/pending: 承認待ち一覧を取得できる")
    void findPending_returns200() throws Exception {
        var pending = new PendingLeaveResponse(
            leaveId, requesterId, "田中太郎",
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用", new BigDecimal("1.0"),
            Instant.now(), 0L);
        var managerId = UUID.randomUUID();
        when(paidLeaveService.findPending(managerId)).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/leaves/pending")
                .param("managerId", managerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].requesterName").value("田中太郎"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/leaves/{id}/approve: 承認できる")
    void approve_returns200() throws Exception {
        var response = createResponse(LeaveStatus.APPROVED);
        var approverId = UUID.randomUUID();
        when(paidLeaveService.approve(eq(leaveId), eq(approverId), eq(0L))).thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/approve", leaveId)
                .param("approverId", approverId.toString())
                .param("version", "0")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/leaves/{id}/reject: 却下できる")
    void reject_returns200() throws Exception {
        var response = createResponse(LeaveStatus.REJECTED);
        var approverId = UUID.randomUUID();
        when(paidLeaveService.reject(eq(leaveId), eq(approverId), eq("業務都合"), eq(0L)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/leaves/{id}/reject", leaveId)
                .param("approverId", approverId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"業務都合\",\"version\":0}")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/leaves/balance: 残日数を取得できる")
    void getBalance_returns200() throws Exception {
        var balance = new LeaveBalanceResponse(
            2026, new BigDecimal("10"), new BigDecimal("5"),
            new BigDecimal("3"), new BigDecimal("12"));
        when(paidLeaveService.getBalance(requesterId)).thenReturn(balance);

        mockMvc.perform(get("/api/leaves/balance")
                .param("employeeId", requesterId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingDays").value(12));
    }

    @Test
    @DisplayName("未認証ユーザーは401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/leaves")
                .param("requesterId", requesterId.toString()))
            .andExpect(status().isUnauthorized());
    }

    private LeaveRequestResponse createResponse(LeaveStatus status) {
        return new LeaveRequestResponse(
            leaveId, requesterId, "田中太郎",
            LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15),
            LeaveType.FULL_DAY, "私用のため", status,
            new BigDecimal("1.0"), null, Instant.now());
    }
}
