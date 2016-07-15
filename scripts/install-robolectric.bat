@echo off
set TMPPATH="%TEMP%.\install-robolectric.bat.tmp"
cd > "%TMPPATH%"
set /p PROJECT=<"%TMPPATH%"
del "%TMPPATH%"

rem Build everything
echo Installing base installation (skipping tests)...
cd "%PROJECT%"
call gradlew clean assemble install compileTest --info --stacktrace

rem Build everything with tests (tests require the shadows)
echo Installing base installation (with tests)...
cd "%PROJECT%"
call gradlew --continue test --info --stacktrace
