@echo off
del local.properties
for %%i in (android.bat) do set x=%%~dp$PATH:i
set x=%x:\tools=%
set x=%x:\=/%

rem not yet tested!
@echo sdk.dir=%x%>> local.properties