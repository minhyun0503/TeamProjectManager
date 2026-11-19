#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
sh build.sh
mkdir -p test-bin
javac -encoding UTF-8 -cp bin -d test-bin tests/FeedbackRegressionTest.java
java -Djava.awt.headless=true -cp 'bin:test-bin:lib/h2-2.4.240.jar' FeedbackRegressionTest
