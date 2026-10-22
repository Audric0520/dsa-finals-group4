@echo off
rem Compiles and starts the SC Ice Cream Management System (Windows).
rem Double-click this file, or run it from a terminal in this folder:
rem   run.bat            start the program (options such as --plain are passed on)
rem   run.bat test       run the automated tests
rem   run.bat demo       replay the scripted demo session
rem The compiled classes go to a temporary folder, so nothing is added to the project.
cd /d "%~dp0"
set "BUILD=%TEMP%\sc-icecream-build"

if exist "%BUILD%" rmdir /s /q "%BUILD%"
javac -d "%BUILD%" -sourcepath src src\Main.java test\AlgorithmTest.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Make sure a JDK is installed and javac is on your PATH.
    pause
    exit /b 1
)

if /i "%~1"=="test" (
    java -cp "%BUILD%" AlgorithmTest
) else if /i "%~1"=="demo" (
    java -cp "%BUILD%" Main < test\demo-input.txt
) else (
    java -cp "%BUILD%" Main %*
)
pause
