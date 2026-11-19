@echo off
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b 1
if not exist test-bin mkdir test-bin
javac --release 17 -encoding UTF-8 -cp bin -d test-bin tests\FeedbackRegressionTest.java
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "bin;test-bin;lib/h2-2.4.240.jar" FeedbackRegressionTest
