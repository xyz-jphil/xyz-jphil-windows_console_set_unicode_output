@echo off
echo Running all Unicode tests in separate cmd windows...
echo.

echo Starting Test 1 - Baseline...
start "Test1 Baseline" cmd /k "java Test1_Baseline.java && echo. && echo Press any key to close... && pause > nul && exit"

timeout /t 2 > nul

echo Starting Test 2 - UTF8 Reset...
start "Test2 UTF8Reset" cmd /k "java Test2_UTF8Reset.java && echo. && echo Press any key to close... && pause > nul && exit"

timeout /t 2 > nul

echo Starting Test 3 - CP65001 Reset...
start "Test3 CP65001Reset" cmd /k "java Test3_CP65001Reset.java && echo. && echo Press any key to close... && pause > nul && exit"

timeout /t 2 > nul

echo Starting Test 4 - System Property...
start "Test4 SystemProperty" cmd /k "java Test4_SystemProperty.java && echo. && echo Press any key to close... && pause > nul && exit"

echo.
echo All tests launched in separate windows. Check each window for results.
pause