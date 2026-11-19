#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
sh build.sh
java --module-path bin --class-path 'lib/*' --module TeamProjectManagerFeedback/teamproject.common.Main
