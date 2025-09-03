@echo off
echo Running all Unicode tests in isolated processes and capturing output...
echo.

echo Running Test 1 - Baseline (isolated process)...
cmd /c "java Test1_Baseline.java" > test1_output.txt 2>&1

echo Running Test 2 - UTF8 Reset (isolated process)...
cmd /c "java Test2_UTF8Reset.java" > test2_output.txt 2>&1

echo Running Test 3 - CP65001 Reset (isolated process)...
cmd /c "java Test3_CP65001Reset.java" > test3_output.txt 2>&1

echo Running Test 4 - System Property (isolated process)...
cmd /c "java Test4_SystemProperty.java" > test4_output.txt 2>&1

echo.
echo All tests completed. Output files created:
echo   test1_output.txt - Baseline test
echo   test2_output.txt - UTF8 Reset test  
echo   test3_output.txt - CP65001 Reset test
echo   test4_output.txt - System Property test
echo.
pause