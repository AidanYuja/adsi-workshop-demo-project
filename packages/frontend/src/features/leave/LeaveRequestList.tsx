"use client";

import { StatusBadge } from "@/components/StatusBadge";
import { useCancelLeaveRequest, useLeaveRequests } from "./useLeaves";
import type { LeaveRequestResponse, LeaveStatus, LeaveType } from "./leave-api";

const LEAVE_STATUS_CONFIG: Record<
  LeaveStatus,
  { label: string; variant: "default" | "secondary" | "destructive" | "outline" }
> = {
  PENDING: { label: "申請中", variant: "secondary" },
  APPROVED: { label: "承認済み", variant: "default" },
  REJECTED: { label: "却下", variant: "destructive" },
  CANCELLED: { label: "取り下げ", variant: "outline" },
};

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  FULL_DAY: "全日",
  AM_HALF: "午前半休",
  PM_HALF: "午後半休",
};

export function LeaveRequestList() {
  const { data: requests, isLoading } = useLeaveRequests();
  const cancelMutation = useCancelLeaveRequest();

  if (isLoading) {
    return <div className="animate-pulse h-40 bg-gray-100 rounded-lg" />;
  }

  if (!requests || requests.length === 0) {
    return <div className="text-center text-gray-500 py-8">有給申請の履歴がありません</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              期間
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              種別
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              日数
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              理由
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              ステータス
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
              操作
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {requests.map((req: LeaveRequestResponse) => (
            <tr key={req.id}>
              <td className="px-4 py-3 text-sm whitespace-nowrap">
                {req.startDate === req.endDate
                  ? req.startDate
                  : `${req.startDate} 〜 ${req.endDate}`}
              </td>
              <td className="px-4 py-3 text-sm">{LEAVE_TYPE_LABELS[req.leaveType]}</td>
              <td className="px-4 py-3 text-sm">{req.totalDays}日</td>
              <td className="px-4 py-3 text-sm max-w-[200px] truncate">{req.reason}</td>
              <td className="px-4 py-3 text-sm">
                <StatusBadge status={req.status} configMap={LEAVE_STATUS_CONFIG} />
              </td>
              <td className="px-4 py-3 text-sm">
                {req.status === "PENDING" && (
                  <button
                    onClick={() => cancelMutation.mutate({ id: req.id, version: 0 })}
                    disabled={cancelMutation.isPending}
                    className="text-red-600 hover:text-red-800 text-sm font-medium"
                  >
                    取り下げ
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
