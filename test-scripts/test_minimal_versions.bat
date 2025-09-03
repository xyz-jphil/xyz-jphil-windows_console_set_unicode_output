@echo off
echo Testing minimal versions to find what's really needed...
echo.

echo Starting Minimal Test 1 - Only SetConsoleOutputCP + System.setOut
start "Minimal1-OutputCP" cmd /k "echo === MINIMAL 1: Only Output CP === && java --enable-native-access=ALL-UNNAMED TestMinimal1_OnlyOutputCP.java && echo. && pause > nul"

timeout /t 3 > nul

echo Starting Minimal Test 2 - Only native CP change, no System.setOut
start "Minimal2-NoSystemOut" cmd /k "echo === MINIMAL 2: No System.setOut === && java --enable-native-access=ALL-UNNAMED TestMinimal2_NoSystemOut.java && echo. && pause > nul"

timeout /t 3 > nul

echo Starting Minimal Test 3 - Full setup but no WriteConsoleW
start "Minimal3-NoWriteConsoleW" cmd /k "echo === MINIMAL 3: No WriteConsoleW === && java --enable-native-access=ALL-UNNAMED TestMinimal3_NoWriteConsoleW.java && echo. && pause > nul"

echo.
echo Check each window and report which shows proper Unicode:
echo   - Minimal1-OutputCP: Only output CP + System.setOut
echo   - Minimal2-NoSystemOut: Only native CP change
echo   - Minimal3-NoWriteConsoleW: Full setup but no direct WriteConsoleW
echo.
pause