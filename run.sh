#!/usr/bin/env sh
COMPILER="$(dirname "$0")/build/install/CZero/bin/CZero"
$COMPILER --source="$1" --output="$2" --target=X86
