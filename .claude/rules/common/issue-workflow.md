---
description: GitHub Issue を起点・記録ハブとする開発運用。要求仕様・設計・実装計画を Issue にコメントして永続化する。機能追加・バグ修正の着手時に適用する。
---

# Issue 運用ルール

> 全体像は [development-process.md](./development-process.md)。工程ごとの手順は各スキル（`requirements` / `design` / `tdd-implementation`）を参照。

## 基本方針

**GitHub Issue を機能追加・バグ修正の「起点」かつ「記録ハブ」にする。**

- 作業は Issue から始める（テンプレート: `.github/ISSUE_TEMPLATE/`）
- 各工程の成果物が確定するたびに **Issue にコメントして永続化する**
- 「人間のレビュー・承認の場」＝ Issue、「確定仕様の置き場」＝ `docs/`
- コミット / PR は Issue 番号を参照する（`#123`, `Closes #123`）

## 工程ごとの永続化

| 工程 | Issue へのコメント内容 | 確定版の保存先 |
|------|----------------------|---------------|
| 要求仕様 | ユーザーストーリー / 受け入れ基準 / [Answer] で確定した仕様 | `docs/requirements/` or `docs/units/unit_*.md` |
| 設計 | ドメイン / API / DB 設計の要点と決定理由 | `docs/design/` |
| 作業分割 | Unit 分割・依存・Phase | `docs/units/` |
| 実装計画 (Plan) | 変更ファイル・テストケース・実装順序 | （コメントで承認を得る） |

- `docs/` に確定版を置き、**Issue コメントには要約＋保存先へのリンク**を残す。
- 未確定（未決）の論点がある限り、その仕様に依存する実装に進まない。

## Q&A（[Question] / [Answer]）はローカルで行う

`[Question]` / `[Answer]` の**壁打ち・反復はローカルで行う**（Issue コメントは編集しづらいため）。

| 用途 | 場所 |
|------|------|
| `[Q]/[A]` の壁打ち・反復 | ローカル `docs/working/requirements/`・`docs/working/design/` |
| 確定した決定事項の記録 | Issue コメント（要約）＋ `docs/requirements/`・`docs/design/` |

- Issue には **確定した決定だけ**を要約して残す（生の `[Q]/[A]` 往復は載せない）。
- 未決の論点は Issue に「未決」として列挙し、ローカルで詰める。

## 承認ゲート

機能追加テンプレの「レビューゲート」チェックボックスを、各工程の承認時に人間が更新する。

```
要求仕様レビュー → 設計レビュー → 実装計画 (Plan) 承認 → テスト観点レビュー
```

全ゲート通過が実装完了の条件。

## gh コマンド例

```bash
# Issue 作成（テンプレは Web UI 推奨。CLI では本文を渡す）
gh issue create --title "[Feature] 月次勤怠 CSV 出力" --label feature

# 工程成果物を永続化（コメント）
gh issue comment 123 --body-file docs/requirements/monthly-report.md

# コミットで参照
git commit -m "feat: 月次 CSV 出力を追加 (#123)"

# PR で自動クローズ
gh pr create --title "feat: 月次 CSV 出力 (#123)" --body "Closes #123"
```

## 参照

- `.github/ISSUE_TEMPLATE/feature_request.yml` / `bug_report.yml`
- `.claude/rules/common/development-process.md`
- `.claude/rules/common/git-workflow.md`
