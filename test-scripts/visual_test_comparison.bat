@echo off
echo Opening all tests in separate windows for visual comparison...
echo.

echo Starting Test 1 - Baseline (should show broken Unicode)
start "Test1-Baseline" cmd /k "echo === ISOLATED PROCESS TEST 1 - BASELINE === && java Test1_Baseline.java && echo. && echo Press any key to close this window... && pause > nul"

timeout /t 3 > nul

echo Starting Test 2 - Simple UTF8 Reset (should show fixed Unicode?)
start "Test2-UTF8Reset" cmd /k "echo === ISOLATED PROCESS TEST 2 - UTF8 RESET === && java Test2_UTF8Reset.java && echo. && echo Press any key to close this window... && pause > nul"

timeout /t 3 > nul

echo Starting Test 3 - CP65001 Reset
start "Test3-CP65001" cmd /k "echo === ISOLATED PROCESS TEST 3 - CP65001 === && java Test3_CP65001Reset.java && echo. && echo Press any key to close this window... && pause > nul"

timeout /t 3 > nul

echo Starting Test 4 - System Property + Reset
start "Test4-Property" cmd /k "echo === ISOLATED PROCESS TEST 4 - PROPERTY === && java Test4_SystemProperty.java && echo. && echo Press any key to close this window... && pause > nul"

timeout /t 3 > nul

echo Starting FULL FFM Implementation (SetConsoleCodePage.java)
start "FFM-SetConsoleCP" cmd /k "echo === ISOLATED PROCESS - FULL FFM IMPLEMENTATION === && jbang SetConsoleCodePage.java && echo. && echo Press any key to close this window... && pause > nul"

echo.
echo All 5 tests are now running in separate cmd windows.
echo Please check each window and report which ones show Unicode correctly:
echo   - Test1-Baseline: Should show broken Unicode (question marks, etc)
echo   - Test2-UTF8Reset: Simple System.setOut reset - does it work?
echo   - Test3-CP65001: CP65001 charset approach
echo   - Test4-Property: file.encoding property + reset
echo   - FFM-SetConsoleCP: Full native FFM implementation
echo.
pause