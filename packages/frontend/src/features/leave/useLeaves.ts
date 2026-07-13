"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "@/components/Toast";
import { useAuth } from "@/features/auth/useAuth";
import {
  type LeaveRequestCreateRequest,
  approveLeaveRequest,
  cancelLeaveRequest,
  createLeaveRequest,
  fetchLeaveBalance,
  fetchLeaveRequests,
  fetchPendingLeaves,
  rejectLeaveRequest,
} from "./leave-api";

const LEAVES_KEY = ["leaves"] as const;
const PENDING_KEY = ["leaves", "pending"] as const;
const BALANCE_KEY = ["leaves", "balance"] as const;

export function useLeaveRequests() {
  const { user } = useAuth();

  return useQuery({
    queryKey: [...LEAVES_KEY, user?.id],
    queryFn: () => fetchLeaveRequests(user!.id),
    enabled: !!user?.id,
  });
}

export function useLeaveBalance() {
  const { user } = useAuth();

  return useQuery({
    queryKey: [...BALANCE_KEY, user?.id],
    queryFn: () => fetchLeaveBalance(user!.id),
    enabled: !!user?.id,
  });
}

export function useCreateLeaveRequest() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: LeaveRequestCreateRequest) => createLeaveRequest(user!.id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: LEAVES_KEY });
      queryClient.invalidateQueries({ queryKey: BALANCE_KEY });
      toast.success("有給申請を送信しました");
    },
    onError: () => {
      toast.error("有給申請の送信に失敗しました");
    },
  });
}

export function useCancelLeaveRequest() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      cancelLeaveRequest(id, user!.id, version),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: LEAVES_KEY });
      queryClient.invalidateQueries({ queryKey: BALANCE_KEY });
      toast.success("有給申請を取り下げました");
    },
    onError: () => {
      toast.error("取り下げに失敗しました");
    },
  });
}

export function usePendingLeaves() {
  const { user } = useAuth();

  return useQuery({
    queryKey: [...PENDING_KEY, user?.id],
    queryFn: () => fetchPendingLeaves(user!.id),
    enabled: !!user?.isManager,
  });
}

export function useApproveLeaveRequest() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      approveLeaveRequest(id, user!.id, version),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PENDING_KEY });
      queryClient.invalidateQueries({ queryKey: LEAVES_KEY });
      queryClient.invalidateQueries({ queryKey: BALANCE_KEY });
      toast.success("有給申請を承認しました");
    },
    onError: () => {
      toast.error("承認に失敗しました");
    },
  });
}

export function useRejectLeaveRequest() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, reason, version }: { id: string; reason: string; version: number }) =>
      rejectLeaveRequest(id, user!.id, reason, version),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PENDING_KEY });
      queryClient.invalidateQueries({ queryKey: LEAVES_KEY });
      toast.success("有給申請を却下しました");
    },
    onError: () => {
      toast.error("却下に失敗しました");
    },
  });
}
