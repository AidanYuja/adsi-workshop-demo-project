"use client";

import { useLeaveBalance } from "./useLeaves";

export function LeaveBalanceSummary() {
  const { data: balance, isLoading } = useLeaveBalance();

  if (isLoading) {
    return <div className="animate-pulse h-24 bg-gray-100 rounded-lg" />;
  }

  if (!balance) {
    return <div className="text-gray-500">残日数情報がありません</div>;
  }

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
      <SummaryCard label="付与日数" value={balance.grantedDays} unit="日" />
      <SummaryCard label="繰越日数" value={balance.carriedOverDays} unit="日" />
      <SummaryCard label="使用済み" value={balance.usedDays} unit="日" />
      <SummaryCard label="残日数" value={balance.remainingDays} unit="日" highlight />
    </div>
  );
}

function SummaryCard({
  label,
  value,
  unit,
  highlight = false,
}: {
  label: string;
  value: number;
  unit: string;
  highlight?: boolean;
}) {
  return (
    <div
      className={`rounded-lg border p-4 ${highlight ? "border-green-300 bg-green-50" : "bg-white"}`}
    >
      <div className="text-sm text-gray-500">{label}</div>
      <div className={`text-2xl font-bold ${highlight ? "text-green-700" : "text-gray-900"}`}>
        {value}
        <span className="text-sm font-normal text-gray-500 ml-1">{unit}</span>
      </div>
    </div>
  );
}
