#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== adsi-demo-project: 依存関係インストール ==="
echo ""

# Backend: Gradle の依存解決
echo "--- [1/3] Backend (Gradle) ---"
cd "$PROJECT_DIR/packages/backend"
./gradlew dependencies --write-locks 2>/dev/null || ./gradlew dependencies
echo "Backend dependencies: OK"
echo ""

# Frontend: npm install
echo "--- [2/3] Frontend (npm) ---"
cd "$PROJECT_DIR/packages/frontend"
npm install
echo "Frontend dependencies: OK"
echo ""

# Infra: npm install
echo "--- [3/3] Infra (npm) ---"
cd "$PROJECT_DIR/packages/infra"
npm install
echo "Infra dependencies: OK"
echo ""

echo "=========================================="
echo "  全ての依存関係のインストールが完了しました"
echo "=========================================="
