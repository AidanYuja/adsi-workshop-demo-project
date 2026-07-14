# Unit 07: 有給休暇申請

有給休暇の申請・承認・取り下げ、有給付与・残日数管理、勤怠履歴への反映。

## 依存関係

- 依存先: Unit 00（共通基盤）, Unit 02（社員 — 申請者・承認者・入社日から付与計算）, Unit 03（認証）, Unit 04（打刻 — 勤怠履歴への有給表示）
- 依存元: Unit 06（集計 — 有給日数の集計項目追加）
- **Unit 05（修正）とは並列実装可能**

## ユーザーストーリー

- **LEAVE-01**: 社員として、有給休暇を申請したい（対象期間・種別・理由を入力）
- **LEAVE-02**: 社員として、自分の有給申請の状態を確認したい
- **LEAVE-03**: 社員として、承認前の有給申請を取り下げたい
- **LEAVE-04**: 上長として、自部署メンバーの有給申請を一覧で確認したい
- **LEAVE-05**: 上長として、自部署メンバーの有給申請を承認・却下したい
- **LEAVE-06**: 承認された有給が勤怠履歴に「有給休暇」として表示される
- **LEAVE-07**: 社員として、有給残日数を確認したい（当年度付与 + 前年度繰越 − 使用済み）
- **LEAVE-08**: 承認操作と取り下げ操作の競合は楽観ロックで制御する

## テーブル

### paid_leave_balances（有給残高）

```sql
CREATE TABLE paid_leave_balances (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    fiscal_year INT NOT NULL,
    granted_days DECIMAL(4,1) NOT NULL,
    carried_over_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (employee_id, fiscal_year)
);

CREATE INDEX idx_paid_leave_balances_employee ON paid_leave_balances(employee_id);
```

### paid_leave_requests（有給申請）

```sql
CREATE TABLE paid_leave_requests (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES employees(id),
    approver_id UUID REFERENCES employees(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_type VARCHAR(20) NOT NULL CHECK (leave_type IN ('FULL_DAY', 'AM_HALF', 'PM_HALF')),
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    total_days DECIMAL(4,1) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_paid_leave_requests_requester ON paid_leave_requests(requester_id);
CREATE INDEX idx_paid_leave_requests_status ON paid_leave_requests(status);
CREATE INDEX idx_paid_leave_requests_dates ON paid_leave_requests(start_date, end_date);
```

Flyway: `V6__create_paid_leave_tables.sql`

## API

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| POST | `/api/leaves` | 有給申請 | 全ロール |
| GET | `/api/leaves` | 自分の申請一覧 | 全ロール |
| PATCH | `/api/leaves/{id}/cancel` | 申請取り下げ | 全ロール（自分の申請のみ） |
| GET | `/api/leaves/pending` | 承認待ち一覧（部署メンバー） | 上長 |
| PATCH | `/api/leaves/{id}/approve` | 承認 | 上長 |
| PATCH | `/api/leaves/{id}/reject` | 却下 | 上長 |
| GET | `/api/leaves/balance` | 有給残日数 | 全ロール |

## 画面

| パス | ページ | コンポーネント |
|------|--------|--------------|
| `/leaves` | 有給休暇トップ | `LeaveBalanceSummary`, `LeaveRequestList`, `LeaveRequestForm` |
| `/leaves/approvals` | 有給承認（上長） | `PendingLeaveList`, `LeaveApprovalActions` |

## Backend 実装順序（TDD）

1. Flyway マイグレーション `V6__create_paid_leave_tables.sql`
2. `LeaveType` Enum（FULL_DAY, AM_HALF, PM_HALF）
3. `LeaveStatus` Enum（PENDING, APPROVED, REJECTED, CANCELLED）
4. `PaidLeaveBalance` Entity
5. `PaidLeaveRequest` Entity
6. `PaidLeaveGrantCalculator` ドメインサービス（労基法準拠の付与日数計算）
7. `PaidLeaveBalanceRepository` テスト → 実装
8. `PaidLeaveRequestRepository` テスト → 実装
9. `PaidLeaveService` テスト → interface → 実装
10. `PaidLeaveController` テスト → 実装
11. 統合テスト

## Backend ファイル

```
packages/backend/src/
├── main/java/com/example/attendance/leave/
│   ├── controller/PaidLeaveController.java
│   ├── dto/
│   │   ├── LeaveRequestCreateRequest.java     (record)
│   │   ├── LeaveRequestResponse.java          (record)
│   │   ├── LeaveBalanceResponse.java          (record)
│   │   └── PendingLeaveResponse.java          (record)
│   ├── entity/
│   │   ├── PaidLeaveBalance.java
│   │   ├── PaidLeaveRequest.java
│   │   ├── LeaveType.java                     (enum)
│   │   └── LeaveStatus.java                   (enum)
│   ├── domain/
│   │   └── PaidLeaveGrantCalculator.java      (付与日数計算)
│   ├── repository/
│   │   ├── PaidLeaveBalanceRepository.java
│   │   └── PaidLeaveRequestRepository.java
│   └── service/
│       ├── PaidLeaveService.java              (interface)
│       └── PaidLeaveServiceImpl.java
├── main/resources/db/migration/
│   └── V6__create_paid_leave_tables.sql
└── test/java/com/example/attendance/leave/
    ├── controller/PaidLeaveControllerTest.java
    ├── domain/PaidLeaveGrantCalculatorTest.java
    ├── repository/
    │   ├── PaidLeaveBalanceRepositoryTest.java
    │   └── PaidLeaveRequestRepositoryTest.java
    └── service/PaidLeaveServiceTest.java
```

## Frontend ファイル

```
packages/frontend/src/features/leave/
├── LeaveBalanceSummary.tsx
├── LeaveRequestForm.tsx
├── LeaveRequestList.tsx
├── PendingLeaveList.tsx
├── LeaveApprovalActions.tsx
├── useLeaves.ts
└── leave-api.ts

packages/frontend/src/app/(authenticated)/
└── leaves/
    ├── page.tsx
    └── approvals/page.tsx
```

## テストケース

### Backend

| テスト | 種類 | 内容 |
|--------|------|------|
| GrantCalculator: 入社6ヶ月（10日） | Unit | 勤続0.5年で10日付与 |
| GrantCalculator: 入社1.5年（11日） | Unit | 勤続1.5年で11日付与 |
| GrantCalculator: 入社6.5年以上（20日） | Unit | 上限20日 |
| GrantCalculator: 入社6ヶ月未満（0日） | Unit | 付与なし |
| Repository: 社員×年度で残高検索 | DataJpaTest | employee_id + fiscal_year |
| Repository: 申請者で検索 | DataJpaTest | requester_id で一覧取得 |
| Repository: ステータスで検索 | DataJpaTest | PENDING で絞り込み |
| Repository: 日付範囲の重複チェック | DataJpaTest | 期間重複する承認済み申請を検索 |
| Service: 有給申請（正常） | Unit | 残日数内で申請作成 |
| Service: 有給申請（残日数不足） | Unit | 400 例外 |
| Service: 有給申請（重複日程） | Unit | 409 例外 |
| Service: 有給申請（半休） | Unit | 0.5日 × 平日数を消化 |
| Service: 有給申請（連続日・土日除外） | Unit | 平日のみカウント |
| Service: 取り下げ（正常） | Unit | status=CANCELLED |
| Service: 取り下げ（承認済み） | Unit | 400 例外（承認後は取り下げ不可） |
| Service: 承認 | Unit | status=APPROVED、used_days 加算 |
| Service: 承認（承認者が部署の上長） | Unit | 正常完了 |
| Service: 承認（承認者が上長でない） | Unit | 403 例外 |
| Service: 上長の自己承認 | Unit | 自分自身が承認者 |
| Service: 却下 | Unit | status=REJECTED |
| Service: 楽観ロックエラー | Unit | 承認と取り下げの競合 |
| Service: 残日数取得 | Unit | granted + carried_over - used |
| Service: 繰越計算 | Unit | 前年度未使用分が翌年に繰越 |
| Controller: POST /api/leaves | WebMvcTest | 201 |
| Controller: GET /api/leaves | WebMvcTest | 200 + 申請一覧 |
| Controller: PATCH cancel | WebMvcTest | 200 |
| Controller: GET /api/leaves/pending（上長） | WebMvcTest | 200 |
| Controller: GET /api/leaves/pending（一般） | WebMvcTest | 403 |
| Controller: PATCH approve | WebMvcTest | 200 |
| Controller: PATCH reject | WebMvcTest | 200 |
| Controller: GET /api/leaves/balance | WebMvcTest | 200 + 残日数 |

### Frontend

| テスト | 種類 | 内容 |
|--------|------|------|
| LeaveBalanceSummary: 残日数表示 | Component | 付与・繰越・使用済み・残を表示 |
| LeaveRequestForm: 日付範囲選択 | Component | 開始日・終了日を選択 |
| LeaveRequestForm: 種別選択 | Component | 全日/午前半休/午後半休 |
| LeaveRequestForm: バリデーション | Component | 残日数超過でエラー表示 |
| LeaveRequestForm: 申請送信 | Component | API 呼び出し → 一覧更新 |
| LeaveRequestList: ステータス表示 | Component | 各申請にバッジ表示 |
| LeaveRequestList: 取り下げボタン | Component | PENDING のみ取り下げ可能 |
| PendingLeaveList: 承認・却下 | Component | ボタンクリック → ステータス変化 |

## ビジネスルール

### 付与

- 労基法準拠: 入社日 + 6ヶ月が最初の付与、以降1年ごと
- 付与日数: 6ヶ月=10日, 1.5年=11日, 2.5年=12日, 3.5年=14日, 4.5年=16日, 5.5年=18日, 6.5年+=20日
- 繰越: 前年度未使用分のみ（2年前の分は消滅）
- 消化順序: 繰越分（古い方）から先に消化

### 申請

- 1申請で開始日〜終了日の範囲指定（連続休暇）
- 種別: 全日(1.0日)・午前半休(0.5日)・午後半休(0.5日)
- 土日は有給消化対象外（平日のみカウント）
- 過去日への事後申請可能
- 残日数を超える申請はエラー
- 既に有給が承認されている日への重複申請はエラー

### 承認フロー

勤怠修正と同じ:

| 申請者 | 承認者 |
| --- | --- |
| 一般社員 | 所属部署の上長 |
| 上長 | 自己承認 |
| 管理者（上長でない） | 所属部署の上長 |
| 管理者（上長を兼任） | 自己承認 |

### 勤怠履歴への反映

- 全日有給: 打刻不要。勤怠履歴に「有給休暇」として表示
- 半休: 出勤側の半日分は打刻が必要

## Unit 06 への影響

月次集計に以下の変更が必要:

- 集計項目に「有給取得日数」を追加
- 欠勤日数 = 営業日（平日）− 出勤日数 − 有給取得日数

## 完了条件

- [ ] 有給申請（全日・半休・連続日）ができる
- [ ] 残日数のバリデーションが機能する
- [ ] 土日を除外した平日のみカウントが正しい
- [ ] 承認待ち一覧が上長に表示される
- [ ] 承認すると used_days が加算される
- [ ] 却下が正常に動作する
- [ ] 取り下げ（PENDING のみ）が動作する
- [ ] 楽観ロックエラーが適切に処理される
- [ ] 有給残日数（付与 + 繰越 − 使用）が正しく計算される
- [ ] 労基法準拠の付与日数計算が正しい
- [ ] 繰越（前年度未使用分、2年で消滅）が正しい
- [ ] 勤怠履歴に「有給休暇」が表示される
- [ ] サイドバーに「有給休暇」メニューが追加されている
- [ ] Backend テストカバレッジ 80% 以上（leave パッケージ）
