@echo off
rem Compiles and starts the SC Ice Cream Management System (Windows).
rem Double-click this file, or run "run.bat" from a terminal in this folder.
cd /d "%~dp0"

if exist out rmdir /s /q out
javac -d out src\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Make sure a JDK is installed and javac is on your PATH.
    pause
    exit /b 1
)

java -cp out Main
pause
