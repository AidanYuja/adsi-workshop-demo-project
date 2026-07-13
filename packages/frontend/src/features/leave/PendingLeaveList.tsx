"use client";

import { type FormEvent, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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

  const handleReject = (e: FormEvent) => {
    e.preventDefault();
    if (!rejectTarget || !rejectReason.trim()) return;
    rejectMutation.mutate(
      { id: rejectTarget.id, reason: rejectReason.trim(), version: rejectTarget.version },
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
                <Button
                  size="sm"
                  onClick={() => approveMutation.mutate({ id: req.id, version: req.version })}
                  disabled={approveMutation.isPending || rejectMutation.isPending}
                >
                  {approveMutation.isPending ? "承認中..." : "承認"}
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => setRejectTarget(req)}
                  disabled={approveMutation.isPending || rejectMutation.isPending}
                >
                  却下
                </Button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <Dialog
        open={!!rejectTarget}
        onOpenChange={(open) => {
          if (!open) {
            setRejectTarget(null);
            setRejectReason("");
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>有給申請を却下</DialogTitle>
            <DialogDescription>
              {rejectTarget ? `${rejectTarget.requesterName}さんの有給申請を却下します。` : ""}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleReject}>
            <div className="space-y-2 py-2">
              <label htmlFor="rejectReason" className="text-sm font-medium">
                却下理由
              </label>
              <textarea
                id="rejectReason"
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                required
                maxLength={500}
                rows={3}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
                placeholder="却下理由を入力してください"
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setRejectTarget(null)}>
                キャンセル
              </Button>
              <Button
                type="submit"
                variant="destructive"
                disabled={!rejectReason.trim() || rejectMutation.isPending}
              >
                {rejectMutation.isPending ? "処理中..." : "却下する"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
