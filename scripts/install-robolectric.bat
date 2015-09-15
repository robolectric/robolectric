@echo off
set TMPPATH="%TEMP%.\install-robolectric.bat.tmp"
cd > "%TMPPATH%"
set /p PROJECT=<"%TMPPATH%"
del "%TMPPATH%"

rem Build everything
echo Installing base installation (skipping tests)...
cd "%PROJECT%"
call mvn -D skipTests clean install

rem Build older shadow packages

echo Installing shadows for API 16...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-16 clean install

echo Installing shadows for API 17...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-17 clean install

echo Installing shadows for API 18...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-18 clean install

echo Installing shadows for API 19...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-19 clean install

echo Installing shadows for API 21...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-21 clean install

echo Installing shadows for API 22s...
cd "%PROJECT%\robolectric-shadows\shadows-core"
call mvn -D skipTests -P android-22 clean install

rem Build everything with tests (tests require the shadows)
echo Installing base installation (with tests)...
cd "%PROJECT%"
call mvn -P android-latest install
