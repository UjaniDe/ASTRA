#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

clear

printf '\n'
printf '========================================\n'
printf '       ASTRA - FINAL REVIEW DEMO        \n'
printf '========================================\n\n'

./bin/astra --version

printf '\n'
printf 'Available commands:\n\n'
./bin/astra --help

printf '\n'
printf '========================================\n'
printf '        LIVE STRUCTURAL ANALYSIS        \n'
printf '========================================\n'

./bin/astra demo

printf '\n'
printf '========================================\n'
printf '        DEMONSTRATION COMPLETE          \n'
printf '========================================\n'
