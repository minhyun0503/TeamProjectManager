#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p bin
find src -name '*.java' > .sources.txt
javac --release 17 -encoding UTF-8 -d bin @.sources.txt
mkdir -p bin/teamproject/common
cp src/teamproject/common/schema.sql bin/teamproject/common/schema.sql
