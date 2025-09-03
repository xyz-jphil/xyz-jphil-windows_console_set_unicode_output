package xyz.jphil.windows_console_set_unicode_output;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.foreign.ValueLayout.*;

/**
 * Utility to enable Unicode console output on Windows.
 * 
 * <p>Usage: Call {@link #enable()} once at start of your program.</p>
 * 
 * <p>What it does:</p>
 * <ol>
 * <li>Sets Windows console output code page to UTF-8 (65001), similar to `chcp 65001` command</li>
 * <li>Resets Java's System.out to use UTF-8 encoding</li>
 * </ol>
 * 
 * <p>Requirements: Java 22+ with native access enabled for this module</p>
 */
public final class WindowsConsoleSetUnicodeOutput {
    
    /**
     * Result of attempting to enable Unicode console output.
     */
    public sealed interface EnableResult 
        permits EnableResult.Success, EnableResult.AlreadyEnabled, EnableResult.Failure {
        
        /**
         * Successfully enabled Unicode output.
         */
        record Success() implements EnableResult {}
        
        /**
         * Unicode output was already enabled.
         */
        record AlreadyEnabled() implements EnableResult {}
        
        /**
         * Failed to enable Unicode output.
         * @param reason the reason for failure
         * @param cause the underlying cause (may be null)
         */
        record Failure(FailureReason reason, Throwable cause) implements EnableResult {}
    }
    
    /**
     * Reasons why enabling Unicode output might fail.
     */
    public enum FailureReason {
        UNSUPPORTED_OS("Not running on Windows"),
        UNSUPPORTED_JDK_VERSION("JDK version below 22 not supported"),
        NATIVE_ACCESS_DENIED("Native access not available"),
        KERNEL32_NOT_FOUND("Windows kernel32.dll not found"),
        FUNCTION_NOT_FOUND("SetConsoleOutputCP function not found"),
        API_CALL_FAILED("Windows API call failed"),
        STREAM_RESET_FAILED("Failed to reset output streams");
        
        private final String description;
        
        FailureReason(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static volatile EnableResult lastResult;
    
    private WindowsConsoleSetUnicodeOutput() {
        // Utility class
    }
    
    /**
     * Enable Unicode console output. Safe to call multiple times.
     * 
     * @return the result of the enable operation
     */
    public static EnableResult enable() {
        // Return cached result if already attempted
        if (lastResult instanceof EnableResult.Success || 
            lastResult instanceof EnableResult.AlreadyEnabled) {
            return new EnableResult.AlreadyEnabled();
        }
        
        // Check OS compatibility
        if (!isWindows()) {
            lastResult = new EnableResult.Failure(FailureReason.UNSUPPORTED_OS, null);
            return lastResult;
        }
        
        // Check JDK version
        var versionCheck = checkJdkVersion();
        if (versionCheck != null) {
            lastResult = versionCheck;
            return lastResult;
        }
        
        try (var arena = Arena.ofConfined()) {
            // Check if native access is available
            SymbolLookup kernel32;
            try {
                kernel32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
            } catch (Exception e) {
                lastResult = new EnableResult.Failure(FailureReason.KERNEL32_NOT_FOUND, e);
                return lastResult;
            }
            
            // Get SetConsoleOutputCP function
            MethodHandle setConsoleOutputCP;
            try {
                setConsoleOutputCP = kernel32.find("SetConsoleOutputCP")
                    .map(addr -> LINKER.downcallHandle(addr, 
                        FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT)))
                    .orElseThrow(() -> new RuntimeException("SetConsoleOutputCP not found"));
            } catch (Exception e) {
                lastResult = new EnableResult.Failure(FailureReason.FUNCTION_NOT_FOUND, e);
                return lastResult;
            }
            
            // Set console output to UTF-8 (code page 65001)
            boolean apiResult;
            try {
                apiResult = (boolean) setConsoleOutputCP.invoke(65001);
            } catch (Throwable e) {
                lastResult = new EnableResult.Failure(FailureReason.API_CALL_FAILED, e);
                return lastResult;
            }
            
            if (!apiResult) {
                lastResult = new EnableResult.Failure(FailureReason.API_CALL_FAILED, 
                    new RuntimeException("SetConsoleOutputCP returned false"));
                return lastResult;
            }
            
            // Reset Java's System.out and System.err to use UTF-8
            try {
                System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
                System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
            } catch (Exception e) {
                lastResult = new EnableResult.Failure(FailureReason.STREAM_RESET_FAILED, e);
                return lastResult;
            }
            
            lastResult = new EnableResult.Success();
            return lastResult;
            
        } catch (Exception e) {
            lastResult = new EnableResult.Failure(FailureReason.NATIVE_ACCESS_DENIED, e);
            return lastResult;
        }
    }
    
    /**
     * Check if the current OS is Windows.
     * 
     * @return true if running on Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }
    
    /**
     * Check if the JDK version is supported.
     * 
     * @return null if version is supported, failure result otherwise
     */
    private static EnableResult checkJdkVersion() {
        String version = System.getProperty("java.version", "");
        try {
            // Parse major version (e.g., "22.0.1" -> 22)
            String[] parts = version.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            
            if (majorVersion < 21) {
                return new EnableResult.Failure(FailureReason.UNSUPPORTED_JDK_VERSION,
                    new RuntimeException("JDK " + majorVersion + " not supported. Minimum JDK 21 required, JDK 22+ recommended."));
            }
            
            if (majorVersion == 21) {
                System.err.println("WARNING: JDK 21 detected. JDK 22+ is recommended for full FFM API support.");
            }
            
            return null; // Version is acceptable
        } catch (Exception e) {
            return new EnableResult.Failure(FailureReason.UNSUPPORTED_JDK_VERSION,
                new RuntimeException("Could not parse Java version: " + version, e));
        }
    }
    
    /**
     * Get the last enable result, if any.
     * 
     * @return the last result, or null if enable() has not been called
     */
    public static EnableResult getLastResult() {
        return lastResult;
    }
    
    /**
     * Test/demo program showing Unicode console output.
     */
    public static void main(String[] args) {
        System.out.println("=== Windows Console Unicode Output Test ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println();
        
        System.out.println("Before enabling Unicode:");
        System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
        System.out.println();
        
        EnableResult result = enable();
        System.out.println("Enable result: " + formatResult(result));
        System.out.println();
        
        if (result instanceof EnableResult.Success || 
            result instanceof EnableResult.AlreadyEnabled) {
            System.out.println("After enabling Unicode:");
            System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
            System.out.println("More Unicode: àáâãäå çñüß ©®™ ┌─┐");
            System.out.println("Japanese: こんにちは世界");
            System.out.println("Arabic: مرحبا بالعالم");
            System.out.println("Russian: Привет мир");
        }
    }
    
    /**
     * Format an EnableResult for display.
     */
    private static String formatResult(EnableResult result) {
        return switch (result) {
            case EnableResult.Success() -> "SUCCESS - Unicode output enabled";
            case EnableResult.AlreadyEnabled() -> "ALREADY ENABLED - Unicode output was previously enabled";
            case EnableResult.Failure(var reason, var cause) -> 
                "FAILED - " + reason.getDescription() + 
                (cause != null ? " (" + cause.getMessage() + ")" : "");
        };
    }
}