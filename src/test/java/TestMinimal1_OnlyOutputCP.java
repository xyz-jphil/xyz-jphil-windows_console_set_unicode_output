// Standalone Java code (not part of main project) - replaces bash/python/batch scripts with IDE-friendly, maintainable code using JDK 11/21/25 enhancements. To know why, refer to Cay Horstmann's JavaOne 2025 talk "Java for Small Coding Tasks" (https://youtu.be/04wFgshWMdA)

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.foreign.ValueLayout.*;

public class TestMinimal1_OnlyOutputCP {
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
    
    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            System.out.println("=== MINIMAL TEST 1: Only SetConsoleOutputCP + System.setOut ===");
            
            // Only set output CP, skip input CP
            var setConsoleOutputCP = KERNEL32.find("SetConsoleOutputCP")
                .map(addr -> LINKER.downcallHandle(addr, 
                    FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT)))
                .orElseThrow();
                
            var result = (boolean) setConsoleOutputCP.invoke(65001);
            System.out.println("SetConsoleOutputCP result: " + result);
            
            // Reset System.out only
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            
            // Test Unicode
            System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}