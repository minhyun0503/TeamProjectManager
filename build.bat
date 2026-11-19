@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
(for /r src %%f in (*.java) do @echo "%%f") > .sources.txt
javac --release 17 -encoding UTF-8 -d bin @.sources.txt
if errorlevel 1 exit /b 1
if not exist bin\teamproject\common mkdir bin\teamproject\common
copy /y src\teamproject\common\schema.sql bin\teamproject\common\schema.sql > nul
