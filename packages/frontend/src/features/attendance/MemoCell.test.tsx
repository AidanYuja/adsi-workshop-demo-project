import { fireEvent, render, within } from "@testing-library/react";
import { type Mock, beforeEach, describe, expect, it, vi } from "vitest";
import { MemoCell } from "./MemoCell";
import { useUpdateMemo } from "./useAttendance";

vi.mock("./useAttendance", () => ({
  useUpdateMemo: vi.fn(),
}));

const mockMutate = vi.fn();

function setupMock(isPending = false) {
  (useUpdateMemo as Mock).mockReturnValue({
    mutate: mockMutate,
    isPending,
  });
}

describe("MemoCell", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupMock();
  });

  it("メモがない場合は「-」を表示する", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo={null} clockOutMemo={null} />,
    );
    expect(within(container).getByText("-")).toBeInTheDocument();
  });

  it("出勤メモを[出勤]プレフィックス付きで表示する", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo="遅刻" clockOutMemo={null} />,
    );
    expect(container.textContent).toContain("[出勤] 遅刻");
  });

  it("退勤メモを[退勤]プレフィックス付きで表示する", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo={null} clockOutMemo="早退" />,
    );
    expect(container.textContent).toContain("[退勤] 早退");
  });

  it("クリックで編集モードに切り替わる", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo="メモ" clockOutMemo={null} />,
    );
    const view = within(container);

    fireEvent.click(view.getByTitle("クリックして編集"));

    expect(view.getByText("出勤メモ")).toBeInTheDocument();
    expect(view.getByText("退勤メモ")).toBeInTheDocument();
    expect(view.getByRole("button", { name: "保存" })).toBeInTheDocument();
    expect(view.getByRole("button", { name: "キャンセル" })).toBeInTheDocument();
  });

  it("キャンセルで編集モードが閉じる", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo="メモ" clockOutMemo={null} />,
    );
    const view = within(container);

    fireEvent.click(view.getByTitle("クリックして編集"));
    fireEvent.click(view.getByRole("button", { name: "キャンセル" }));

    expect(view.getByTitle("クリックして編集")).toBeInTheDocument();
    expect(view.queryByRole("button", { name: "保存" })).not.toBeInTheDocument();
  });

  it("保存ボタンでmutateが呼ばれる", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo="元メモ" clockOutMemo={null} />,
    );
    const view = within(container);

    fireEvent.click(view.getByTitle("クリックして編集"));

    const textareas = view.getAllByRole("textbox");
    fireEvent.change(textareas[0], { target: { value: "新メモ" } });

    fireEvent.click(view.getByRole("button", { name: "保存" }));

    expect(mockMutate).toHaveBeenCalledWith(
      { recordId: "r1", body: { clockInMemo: "新メモ", clockOutMemo: null } },
      expect.any(Object),
    );
  });

  it("1000文字を超える入力はカットされる", () => {
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo={null} clockOutMemo={null} />,
    );
    const view = within(container);

    fireEvent.click(view.getByTitle("クリックして編集"));

    const textareas = view.getAllByRole("textbox");
    const longText = "あ".repeat(1100);
    fireEvent.change(textareas[0], { target: { value: longText } });

    expect((textareas[0] as HTMLTextAreaElement).value).toHaveLength(1000);
  });

  it("保存中はボタンが無効化される", () => {
    setupMock(true);
    const { container } = render(
      <MemoCell recordId="r1" clockInMemo="メモ" clockOutMemo={null} />,
    );
    const view = within(container);

    fireEvent.click(view.getByTitle("クリックして編集"));

    expect(view.getByRole("button", { name: "保存" })).toBeDisabled();
  });
});
