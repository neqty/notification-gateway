@echo off
cd /d "%~dp0"
call mvn -q package
if errorlevel 1 exit /b 1
java -jar target/notification-gateway-1.0.0.jar
