"use client";

import { useState } from "react";
import { useUpdateMemo } from "./useAttendance";

interface MemoCellProps {
  recordId: string;
  clockInMemo: string | null;
  clockOutMemo: string | null;
}

const MEMO_MAX_LENGTH = 1000;

export function MemoCell({ recordId, clockInMemo, clockOutMemo }: MemoCellProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [editClockInMemo, setEditClockInMemo] = useState(clockInMemo ?? "");
  const [editClockOutMemo, setEditClockOutMemo] = useState(clockOutMemo ?? "");
  const updateMemoMutation = useUpdateMemo();

  const displayParts: string[] = [];
  if (clockInMemo) displayParts.push(`[出勤] ${clockInMemo}`);
  if (clockOutMemo) displayParts.push(`[退勤] ${clockOutMemo}`);

  if (isEditing) {
    const handleSave = () => {
      updateMemoMutation.mutate(
        {
          recordId,
          body: {
            clockInMemo: editClockInMemo.trim() || null,
            clockOutMemo: editClockOutMemo.trim() || null,
          },
        },
        {
          onSuccess: () => setIsEditing(false),
        },
      );
    };

    return (
      <div className="space-y-1">
        <label className="block">
          <span className="text-xs text-muted-foreground">出勤メモ</span>
          <textarea
            value={editClockInMemo}
            onChange={(e) => setEditClockInMemo(e.target.value.slice(0, MEMO_MAX_LENGTH))}
            rows={2}
            className="w-full rounded-md border border-input bg-background px-2 py-1 text-xs resize-none focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          />
        </label>
        <label className="block">
          <span className="text-xs text-muted-foreground">退勤メモ</span>
          <textarea
            value={editClockOutMemo}
            onChange={(e) => setEditClockOutMemo(e.target.value.slice(0, MEMO_MAX_LENGTH))}
            rows={2}
            className="w-full rounded-md border border-input bg-background px-2 py-1 text-xs resize-none focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          />
        </label>
        <div className="flex gap-1">
          <button
            type="button"
            onClick={handleSave}
            disabled={updateMemoMutation.isPending}
            className="rounded bg-primary px-2 py-0.5 text-xs text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
          >
            保存
          </button>
          <button
            type="button"
            onClick={() => {
              setEditClockInMemo(clockInMemo ?? "");
              setEditClockOutMemo(clockOutMemo ?? "");
              setIsEditing(false);
            }}
            className="rounded border px-2 py-0.5 text-xs hover:bg-accent"
          >
            キャンセル
          </button>
        </div>
      </div>
    );
  }

  return (
    <button
      type="button"
      onClick={() => setIsEditing(true)}
      className="text-left text-xs whitespace-pre-wrap hover:bg-accent/50 rounded px-1 py-0.5 min-w-[60px] min-h-[24px]"
      title="クリックして編集"
    >
      {displayParts.length > 0 ? (
        displayParts.join("\n")
      ) : (
        <span className="text-muted-foreground">-</span>
      )}
    </button>
  );
}
