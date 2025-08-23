@echo off
setlocal EnableDelayedExpansion

rem --- ユーザー入力 ---
set /p MAX_HEAP=Enter the maximum heap size in GB (e.g., 8):
set /p INIT_HEAP=Enter the initial heap size in GB (e.g., 2):

rem --- 空白トリム ---
for /f %%A in ("!MAX_HEAP!") do set MAX_HEAP=%%~A
for /f %%A in ("!INIT_HEAP!") do set INIT_HEAP=%%~A

rem --- 数値チェック（set /a で計算試行）---
set /a dummy=%MAX_HEAP% >nul 2>&1
if errorlevel 1 (
    echo Please enter a valid numeric value for the maximum heap size.
    pause
    exit /b
)

set /a dummy=%INIT_HEAP% >nul 2>&1
if errorlevel 1 (
    echo Please enter a valid numeric value for the initial heap size.
    pause
    exit /b
)

rem --- 初期ヒープサイズが最大を超えていないかチェック ---
set /a INIT_GT_MAX=%INIT_HEAP% - %MAX_HEAP%
if %INIT_GT_MAX% GTR 0 (
    echo The initial heap size must not exceed the maximum heap size.
    pause
    exit /b
)

rem --- SoftMaxHeapSizeをXmsとXmxの中間に設定 ---
set /a SOFT_MAX=(%MAX_HEAP% + %INIT_HEAP%) / 2

rem --- JVMオプション組み立て ---
rem set JVM_OPTS=-XX:+UseZGC -XX:+UnlockExperimentalVMOptions -XX:SoftMaxHeapSize=%SOFT_MAX%G -Xmx%MAX_HEAP%G -Xms%INIT_HEAP%G
set JVM_OPTS=-XX:+UseZGC -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4 -XX:MaxGCPauseMillis=50 -XX:+UnlockExperimentalVMOptions -XX:SoftMaxHeapSize=%SOFT_MAX%G -Xmx%MAX_HEAP%G -Xms%INIT_HEAP%G
rem set JVM_OPTS=-XX:+UseG1GC -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4 -XX:MaxGCPauseMillis=50 -XX:SoftMaxHeapSize=%SOFT_MAX%G -Xmx%MAX_HEAP%G -Xms%INIT_HEAP%G

rem --- Java実行 ---
set JAVA_HOME=jre
echo "%JAVA_HOME%\bin\java.exe" %JVM_OPTS% -jar RainMIDI.jar
"%JAVA_HOME%\bin\java.exe" %JVM_OPTS% -jar RainMIDI.jar

pause
endlocal
