"use client";

import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { DailyAttendanceResponse } from "./attendance-api";
import { formatDate, formatMinutes, formatTime } from "./format";
import { MemoCell } from "./MemoCell";

function firstClockIn(day: DailyAttendanceResponse): string {
  const record = day.records[0];
  return record ? formatTime(record.clockIn) : "--:--";
}

function lastClockOut(day: DailyAttendanceResponse): string {
  const last = day.records[day.records.length - 1];
  return last?.clockOut ? formatTime(last.clockOut) : "--:--";
}

function hasCorrected(day: DailyAttendanceResponse): boolean {
  return day.records.some((r) => r.corrected);
}

interface AttendanceTableProps {
  days: DailyAttendanceResponse[];
}

export function AttendanceTable({ days }: AttendanceTableProps) {
  if (days.length === 0) {
    return (
      <Table>
        <TableBody>
          <TableRow>
            <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
              勤怠データがありません
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>日付</TableHead>
          <TableHead>出勤</TableHead>
          <TableHead>退勤</TableHead>
          <TableHead>勤務時間</TableHead>
          <TableHead>休憩</TableHead>
          <TableHead>残業</TableHead>
          <TableHead>備考</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {days.map((day) => (
          <TableRow key={day.date}>
            <TableCell>{formatDate(day.date)}</TableCell>
            <TableCell>{firstClockIn(day)}</TableCell>
            <TableCell>{lastClockOut(day)}</TableCell>
            <TableCell>{day.workMinutes > 0 ? formatMinutes(day.workMinutes) : "-"}</TableCell>
            <TableCell>{day.breakMinutes > 0 ? formatMinutes(day.breakMinutes) : "-"}</TableCell>
            <TableCell>{day.overtimeMinutes > 0 ? formatMinutes(day.overtimeMinutes) : "-"}</TableCell>
            <TableCell>
              {day.records[0] && (
                <MemoCell
                  recordId={day.records[0].id}
                  clockInMemo={day.records[0].clockInMemo}
                  clockOutMemo={day.records[0].clockOutMemo}
                />
              )}
            </TableCell>
            <TableCell>
              {hasCorrected(day) && <Badge variant="outline">修正</Badge>}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
