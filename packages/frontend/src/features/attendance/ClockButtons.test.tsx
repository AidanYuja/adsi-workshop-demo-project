import { render, within } from "@testing-library/react";
import { createElement } from "react";
import { type Mock, beforeEach, describe, expect, it, vi } from "vitest";
import { ClockButtons } from "./ClockButtons";
import { useClockIn, useClockOut, useTodayStatus } from "./useAttendance";

vi.mock("./useAttendance", () => ({
  useTodayStatus: vi.fn(),
  useClockIn: vi.fn(),
  useClockOut: vi.fn(),
}));

vi.mock("@/components/ui/skeleton", () => ({
  Skeleton: (props: Record<string, unknown>) =>
    createElement("div", { "data-testid": "skeleton", ...props }),
}));

const mockClockInMutate = vi.fn();
const mockClockOutMutate = vi.fn();

function setupMocks(
  status: "NOT_CLOCKED_IN" | "CLOCKED_IN" | "CLOCKED_OUT",
  records: Array<{ clockIn: string; clockOut: string | null }> = [],
) {
  (useTodayStatus as Mock).mockReturnValue({
    data: { status, records },
    isLoading: false,
  });
  (useClockIn as Mock).mockReturnValue({
    mutate: mockClockInMutate,
    isPending: false,
  });
  (useClockOut as Mock).mockReturnValue({
    mutate: mockClockOutMutate,
    isPending: false,
  });
}

describe("ClockButtons", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("勤務中(CLOCKED_IN)のとき出勤ボタンが無効化される", () => {
    setupMocks("CLOCKED_IN", [
      { clockIn: "2026-07-13T09:00:00Z", clockOut: null },
    ]);

    const { container } = render(<ClockButtons />);
    const view = within(container);

    const clockInButton = view.getByRole("button", { name: /出勤/ });
    expect(clockInButton).toBeDisabled();
  });

  it("勤務中(CLOCKED_IN)のとき退勤ボタンが有効である", () => {
    setupMocks("CLOCKED_IN", [
      { clockIn: "2026-07-13T09:00:00Z", clockOut: null },
    ]);

    const { container } = render(<ClockButtons />);
    const view = within(container);

    const clockOutButton = view.getByRole("button", { name: /退勤/ });
    expect(clockOutButton).toBeEnabled();
  });

  it("退勤済み(CLOCKED_OUT)のとき再出勤ボタンが有効である", () => {
    setupMocks("CLOCKED_OUT", [
      { clockIn: "2026-07-13T09:00:00Z", clockOut: "2026-07-13T18:00:00Z" },
    ]);

    const { container } = render(<ClockButtons />);
    const view = within(container);

    const clockInButton = view.getByRole("button", { name: /出勤/ });
    expect(clockInButton).toBeEnabled();
  });
});
