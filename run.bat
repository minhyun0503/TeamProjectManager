@echo off
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b 1
java --module-path bin --class-path "lib/*" --module TeamProjectManagerFeedback/teamproject.common.Main
