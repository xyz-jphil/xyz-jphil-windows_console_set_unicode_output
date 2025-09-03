// Standalone Java code (not part of main project) - replaces bash/python/batch scripts with IDE-friendly, maintainable code using JDK 11/21/25 enhancements. To know why, refer to Cay Horstmann's JavaOne 2025 talk "Java for Small Coding Tasks" (https://youtu.be/04wFgshWMdA)

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.foreign.ValueLayout.*;

/**
 * Minimal utility to enable Unicode console output on Windows.
 * 
 * Usage: Call UnicodeConsoleUtility.enable() once at start of your program.
 * 
 * What it does:
 * 1. Sets Windows console output code page to UTF-8 (65001)
 * 2. Resets Java's System.out to use UTF-8 encoding
 * 
 * Requirements: Java 22+ with --enable-native-access=ALL-UNNAMED
 */
public class UnicodeConsoleUtility {
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
    private static boolean enabled = false;
    
    /**
     * Enable Unicode console output. Call once at program start.
     * @return true if successfully enabled, false if failed
     */
    public static boolean enable() {
        if (enabled) return true; // Already enabled
        
        try (var arena = Arena.ofConfined()) {
            // Get SetConsoleOutputCP function
            var setConsoleOutputCP = KERNEL32.find("SetConsoleOutputCP")
                .map(addr -> LINKER.downcallHandle(addr, 
                    FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT)))
                .orElseThrow(() -> new RuntimeException("SetConsoleOutputCP not found"));
            
            // Set console output to UTF-8 (code page 65001)
            var result = (boolean) setConsoleOutputCP.invoke(65001);
            
            if (result) {
                // Reset Java's System.out to use UTF-8
                System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
                System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
                enabled = true;
                return true;
            }
            return false;
            
        } catch (Throwable e) {
            System.err.println("Failed to enable Unicode console: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test/demo program
     */
    public static void main(String[] args) {
        System.out.println("Before enabling Unicode:");
        System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
        
        boolean success = enable();
        System.out.println("Unicode enablement: " + (success ? "SUCCESS" : "FAILED"));
        
        if (success) {
            System.out.println("After enabling Unicode:");
            System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
            System.out.println("More Unicode: àáâãäå çñüß ©®™ ┌─┐");
        }
    }
}