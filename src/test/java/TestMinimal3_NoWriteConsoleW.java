// Standalone Java code (not part of main project) - replaces bash/python/batch scripts with IDE-friendly, maintainable code using JDK 11/21/25 enhancements. To know why, refer to Cay Horstmann's JavaOne 2025 talk "Java for Small Coding Tasks" (https://youtu.be/04wFgshWMdA)

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.foreign.ValueLayout.*;

public class TestMinimal3_NoWriteConsoleW {
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
    
    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            System.out.println("=== MINIMAL TEST 3: Full setup but NO WriteConsoleW ===");
            
            var setConsoleOutputCP = KERNEL32.find("SetConsoleOutputCP")
                .map(addr -> LINKER.downcallHandle(addr, 
                    FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT)))
                .orElseThrow();
                
            var setConsoleCP = KERNEL32.find("SetConsoleCP")
                .map(addr -> LINKER.downcallHandle(addr, 
                    FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT)))
                .orElseThrow();
                
            var outputResult = (boolean) setConsoleOutputCP.invoke(65001);
            var inputResult = (boolean) setConsoleCP.invoke(65001);
            System.out.println("Results - Output: " + outputResult + ", Input: " + inputResult);
            
            // Reset System.out
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            
            // Test ONLY with System.out (no WriteConsoleW)
            System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}