-- 有給残高テーブル
CREATE TABLE paid_leave_balances (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    fiscal_year INT NOT NULL,
    granted_days DECIMAL(4,1) NOT NULL,
    carried_over_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (employee_id, fiscal_year)
);

CREATE INDEX idx_paid_leave_balances_employee ON paid_leave_balances(employee_id);

-- 有給申請テーブル
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_paid_leave_requests_requester ON paid_leave_requests(requester_id);
CREATE INDEX idx_paid_leave_requests_status ON paid_leave_requests(status);
CREATE INDEX idx_paid_leave_requests_dates ON paid_leave_requests(start_date, end_date);
