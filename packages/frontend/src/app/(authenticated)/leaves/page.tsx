"use client";

import { useState } from "react";
import { LeaveBalanceSummary } from "@/features/leave/LeaveBalanceSummary";
import { LeaveRequestForm } from "@/features/leave/LeaveRequestForm";
import { LeaveRequestList } from "@/features/leave/LeaveRequestList";

export default function LeavesPage() {
  const [showForm, setShowForm] = useState(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">有給休暇</h1>
        <button
          type="button"
          onClick={() => setShowForm(!showForm)}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white font-medium hover:bg-blue-700"
        >
          {showForm ? "閉じる" : "新規申請"}
        </button>
      </div>

      <LeaveBalanceSummary />

      {showForm && (
        <div className="border rounded-lg p-6 bg-white shadow-sm">
          <h2 className="text-lg font-semibold mb-4">有給休暇申請</h2>
          <LeaveRequestForm onSuccess={() => setShowForm(false)} />
        </div>
      )}

      <div>
        <h2 className="text-lg font-semibold mb-4">申請履歴</h2>
        <LeaveRequestList />
      </div>
    </div>
  );
}
