@echo off
rem Starts the newest SIQ_*.jar in the SIQ folder.
rem Do not delete jars there: older versions are kept on purpose.
set "JARDIR=%~dp0SIQ"
set "JAVA=C:\Program Files\Java\jdk-25.0.2\bin\java.exe"
set "JAR="
for /f "delims=" %%F in ('dir /b /a-d /o:d "%JARDIR%\SIQ_*.jar" 2^>nul') do set "JAR=%%F"
if not defined JAR (
    echo No SIQ_*.jar found in %JARDIR%
    goto :end
)
rem if the specific JDK is not present, fall back to whatever java is on PATH
if not exist "%JAVA%" set "JAVA=java"
echo Starting %JAR% with %JAVA%
"%JAVA%" -jar "%JARDIR%\%JAR%"
rem exit code 0: it ended normally, so the window can close; otherwise stay open to read the error
if %ERRORLEVEL%==0 exit /b 0
echo.
echo SIQ stopped with an error (exit code %ERRORLEVEL%).
:end
echo.
pause
