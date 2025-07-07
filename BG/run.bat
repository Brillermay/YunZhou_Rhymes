@echo off
REM ===== Spring Boot Build & Run Script =====
REM Created: 2025-07-05 23:54 
REM Project: E:\ComputerDesignCompetition\YunZhou_Rhymes\BG 
 
:: Step 1: Clean previous build
echo [INFO] Cleaning target directory...
call mvnw.cmd  clean -Dmaven.clean.failOnError=false  
 
:: Step 2: Package application 
echo [INFO] Building executable JAR (tests skipped)...
call mvnw.cmd  package -DskipTests 
 
if %errorlevel% neq 0 (
    echo [ERROR] Build failed! Check errors above 
    pause
    exit /b %errorlevel%
)
 
:: Step 3: Verify JAR existence 
set JAR_PATH=target\BG-0.0.1-SNAPSHOT.jar 
if not exist "%JAR_PATH%" (
    echo [ERROR] JAR not found: %JAR_PATH%
    dir /b target
    pause 
    exit /b 1
)
 
:: Step 4: Run application (in current console)
echo [INFO] Starting Spring Boot application...
echo ======== APPLICATION LOGS ========
java -Dlogging.file.name=a.log -jar E:\ComputerDesignCompetition\YunZhou_Rhymes\BG\target\BG-0.0.1-SNAPSHOT.jar > console.log 2>&1
:: Post-execution handling
if %errorlevel% neq 0 (
    echo [WARN] Application exited with code %errorlevel%
) else (
    echo [INFO] Application stopped normally
)
 
echo ==================================
echo [INFO] Execution completed 
pause 