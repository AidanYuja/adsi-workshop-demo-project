"use client";

import { useState } from "react";
import { ConfirmDialog } from "@/components/ConfirmDialog";
import { useApproveLeaveRequest, usePendingLeaves, useRejectLeaveRequest } from "./useLeaves";
import type { LeaveType, PendingLeaveResponse } from "./leave-api";

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  FULL_DAY: "全日",
  AM_HALF: "午前半休",
  PM_HALF: "午後半休",
};

export function PendingLeaveList() {
  const { data: pending, isLoading } = usePendingLeaves();
  const approveMutation = useApproveLeaveRequest();
  const rejectMutation = useRejectLeaveRequest();
  const [rejectTarget, setRejectTarget] = useState<PendingLeaveResponse | null>(null);
  const [rejectReason, setRejectReason] = useState("");

  if (isLoading) {
    return <div className="animate-pulse h-40 bg-gray-100 rounded-lg" />;
  }

  if (!pending || pending.length === 0) {
    return <div className="text-center text-gray-500 py-8">承認待ちの有給申請はありません</div>;
  }

  const handleReject = () => {
    if (!rejectTarget) return;
    rejectMutation.mutate(
      { id: rejectTarget.id, reason: rejectReason, version: rejectTarget.version },
      {
        onSuccess: () => {
          setRejectTarget(null);
          setRejectReason("");
        },
      },
    );
  };

  return (
    <>
      <div className="space-y-4">
        {pending.map((req: PendingLeaveResponse) => (
          <div key={req.id} className="border rounded-lg p-4 bg-white shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <div className="font-medium">{req.requesterName}</div>
                <div className="text-sm text-gray-500">
                  {req.startDate === req.endDate
                    ? req.startDate
                    : `${req.startDate} 〜 ${req.endDate}`}
                  {" / "}
                  {LEAVE_TYPE_LABELS[req.leaveType]}
                  {" / "}
                  {req.totalDays}日
                </div>
                <div className="text-sm text-gray-600 mt-1">{req.reason}</div>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() =>
                    approveMutation.mutate({
                      id: req.id,
                      version: req.version,
                    })
                  }
                  disabled={approveMutation.isPending}
                  className="rounded-md bg-green-600 px-3 py-1.5 text-sm text-white hover:bg-green-700 disabled:opacity-50"
                >
                  承認
                </button>
                <button
                  onClick={() => setRejectTarget(req)}
                  className="rounded-md bg-red-600 px-3 py-1.5 text-sm text-white hover:bg-red-700"
                >
                  却下
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <ConfirmDialog
        open={!!rejectTarget}
        onOpenChange={(open) => {
          if (!open) {
            setRejectTarget(null);
            setRejectReason("");
          }
        }}
        title="有給申請を却下"
        description={
          rejectTarget ? `${rejectTarget.requesterName}さんの有給申請を却下しますか？` : ""
        }
        onConfirm={handleReject}
        confirmLabel="却下する"
        variant="destructive"
        isLoading={rejectMutation.isPending}
      />
    </>
  );
}
