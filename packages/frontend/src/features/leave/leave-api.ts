import { apiClient } from "@/lib/api-client";

export type LeaveType = "FULL_DAY" | "AM_HALF" | "PM_HALF";
export type LeaveStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export interface LeaveRequestResponse {
  id: string;
  requesterId: string;
  requesterName: string;
  startDate: string;
  endDate: string;
  leaveType: LeaveType;
  reason: string;
  status: LeaveStatus;
  totalDays: number;
  approverName: string | null;
  version: number;
  createdAt: string;
}

export interface PendingLeaveResponse {
  id: string;
  requesterId: string;
  requesterName: string;
  startDate: string;
  endDate: string;
  leaveType: LeaveType;
  reason: string;
  totalDays: number;
  createdAt: string;
  version: number;
}

export interface LeaveBalanceResponse {
  fiscalYear: number;
  grantedDays: number;
  carriedOverDays: number;
  usedDays: number;
  remainingDays: number;
}

export interface LeaveRequestCreateRequest {
  startDate: string;
  endDate: string;
  leaveType: LeaveType;
  reason: string;
}

export function createLeaveRequest(
  request: LeaveRequestCreateRequest,
): Promise<LeaveRequestResponse> {
  return apiClient.post<LeaveRequestResponse>("/api/leaves", request);
}

export function fetchLeaveRequests(): Promise<LeaveRequestResponse[]> {
  return apiClient.get<LeaveRequestResponse[]>("/api/leaves");
}

export function cancelLeaveRequest(id: string, version: number): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(`/api/leaves/${id}/cancel?version=${version}`);
}

export function fetchPendingLeaves(): Promise<PendingLeaveResponse[]> {
  return apiClient.get<PendingLeaveResponse[]>("/api/leaves/pending");
}

export function approveLeaveRequest(id: string, version: number): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(`/api/leaves/${id}/approve?version=${version}`);
}

export function rejectLeaveRequest(
  id: string,
  reason: string,
  version: number,
): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(`/api/leaves/${id}/reject`, { reason, version });
}

export function fetchLeaveBalance(): Promise<LeaveBalanceResponse> {
  return apiClient.get<LeaveBalanceResponse>("/api/leaves/balance");
}
