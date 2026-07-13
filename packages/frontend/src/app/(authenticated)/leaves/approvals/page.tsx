"use client";

import { PendingLeaveList } from "@/features/leave/PendingLeaveList";

export default function LeaveApprovalsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">有給承認待ち</h1>
      <PendingLeaveList />
    </div>
  );
}
