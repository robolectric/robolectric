@echo off
set TMPPATH="%TEMP%.\install-robolectric.bat.tmp"
cd > "%TMPPATH%"
set /p PROJECT=<"%TMPPATH%"
del "%TMPPATH%"

rem Build everything
echo Installing base installation (skipping tests)...
cd "%PROJECT%"
call mvn clean install -DskipTests

rem Build older shadow packages

echo Installing shadows for API 16...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn clean velocity:velocity install -Pandroid-16

echo Installing shadows for API 17...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn clean velocity:velocity install -Pandroid-17

echo Installing shadows for API 18...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn clean velocity:velocity install -Pandroid-18

echo Installing shadows for API 19...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn clean velocity:velocity install -Pandroid-19

rem Build everything with tests (tests require the shadows)
echo Installing base installation (with tests)...
cd "%PROJECT%"
call mvn javadoc:javadoc install
