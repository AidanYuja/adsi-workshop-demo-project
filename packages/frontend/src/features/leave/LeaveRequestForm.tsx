"use client";

import { useState } from "react";
import { useCreateLeaveRequest } from "./useLeaves";
import type { LeaveType } from "./leave-api";

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  FULL_DAY: "全日",
  AM_HALF: "午前半休",
  PM_HALF: "午後半休",
};

export function LeaveRequestForm({ onSuccess }: { onSuccess?: () => void }) {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [leaveType, setLeaveType] = useState<LeaveType>("FULL_DAY");
  const [reason, setReason] = useState("");

  const createMutation = useCreateLeaveRequest();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(
      { startDate, endDate: endDate || startDate, leaveType, reason },
      { onSuccess },
    );
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label className="block text-sm font-medium text-gray-700">開始日</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">終了日</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            min={startDate}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
          <p className="text-xs text-gray-500 mt-1">空欄の場合は開始日と同日</p>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">種別</label>
        <select
          value={leaveType}
          onChange={(e) => setLeaveType(e.target.value as LeaveType)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
        >
          {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">理由</label>
        <textarea
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
          maxLength={500}
          rows={3}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          placeholder="有給取得の理由を入力してください"
        />
      </div>

      <button
        type="submit"
        disabled={createMutation.isPending || !startDate || !reason}
        className="w-full rounded-md bg-blue-600 px-4 py-2 text-white font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {createMutation.isPending ? "申請中..." : "有給を申請する"}
      </button>
    </form>
  );
}
