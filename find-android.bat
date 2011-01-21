@echo off
for %%i in (android.bat) do set x=%%~dp$PATH:i
set x=%x:\tools=%

# not yet tested!
@echo "sdk.dir=%x%" >> local.properties
