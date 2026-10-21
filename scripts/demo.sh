#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=== ASTRA DEMONSTRATION ==="

mvn -q test
mvn -q dependency:build-classpath   -Dmdep.outputFile=target/runtime-classpath.txt

CP="target/classes:$(cat target/runtime-classpath.txt)"

DEMO_DIR="$(mktemp -d)"
trap 'rm -rf "$DEMO_DIR"' EXIT

git -C "$DEMO_DIR" init -q
git -C "$DEMO_DIR" config user.name "ASTRA Demo"
git -C "$DEMO_DIR" config user.email "demo@localhost"

mkdir -p "$DEMO_DIR/src"

cp examples/toylang/versionA.toy "$DEMO_DIR/src/sample.toy"
git -C "$DEMO_DIR" add src/sample.toy
git -C "$DEMO_DIR" commit -qm "Version A"
OLD_REV="$(git -C "$DEMO_DIR" rev-parse HEAD)"

cp examples/toylang/versionB.toy "$DEMO_DIR/src/sample.toy"
git -C "$DEMO_DIR" add src/sample.toy
git -C "$DEMO_DIR" commit -qm "Version B"
NEW_REV="$(git -C "$DEMO_DIR" rev-parse HEAD)"

git -C "$DEMO_DIR" switch --detach -q "$NEW_REV"
git -C "$DEMO_DIR" branch -f main "$OLD_REV"
git -C "$DEMO_DIR" branch -f feature "$NEW_REV"

echo ""
echo "=== STRUCTURAL COMPARISON ==="

java -cp "$CP" astra.Main   examples/sample.ast   "$DEMO_DIR"   src/sample.toy   --trace
