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
  requesterId: string,
  request: LeaveRequestCreateRequest,
): Promise<LeaveRequestResponse> {
  return apiClient.post<LeaveRequestResponse>(`/api/leaves?requesterId=${requesterId}`, request);
}

export function fetchLeaveRequests(requesterId: string): Promise<LeaveRequestResponse[]> {
  return apiClient.get<LeaveRequestResponse[]>(`/api/leaves?requesterId=${requesterId}`);
}

export function cancelLeaveRequest(
  id: string,
  requesterId: string,
  version: number,
): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(
    `/api/leaves/${id}/cancel?requesterId=${requesterId}&version=${version}`,
  );
}

export function fetchPendingLeaves(managerId: string): Promise<PendingLeaveResponse[]> {
  return apiClient.get<PendingLeaveResponse[]>(`/api/leaves/pending?managerId=${managerId}`);
}

export function approveLeaveRequest(
  id: string,
  approverId: string,
  version: number,
): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(
    `/api/leaves/${id}/approve?approverId=${approverId}&version=${version}`,
  );
}

export function rejectLeaveRequest(
  id: string,
  approverId: string,
  reason: string,
  version: number,
): Promise<LeaveRequestResponse> {
  return apiClient.patch<LeaveRequestResponse>(
    `/api/leaves/${id}/reject?approverId=${approverId}`,
    { reason, version },
  );
}

export function fetchLeaveBalance(employeeId: string): Promise<LeaveBalanceResponse> {
  return apiClient.get<LeaveBalanceResponse>(`/api/leaves/balance?employeeId=${employeeId}`);
}
