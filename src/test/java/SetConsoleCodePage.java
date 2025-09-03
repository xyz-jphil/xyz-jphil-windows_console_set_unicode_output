///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 22+
//JAVA_OPTIONS --enable-native-access=ALL-UNNAMED

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.*;

public class SetConsoleCodePage {
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
    
    // Function signatures
    private static final FunctionDescriptor SET_CONSOLE_OUTPUT_CP_DESC = 
        FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT);
    private static final FunctionDescriptor SET_CONSOLE_CP_DESC = 
        FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT);
    private static final FunctionDescriptor GET_CONSOLE_OUTPUT_CP_DESC = 
        FunctionDescriptor.of(JAVA_INT);
    private static final FunctionDescriptor GET_CONSOLE_CP_DESC = 
        FunctionDescriptor.of(JAVA_INT);
    private static final FunctionDescriptor GET_STD_HANDLE_DESC = 
        FunctionDescriptor.of(ADDRESS, JAVA_INT);
    private static final FunctionDescriptor WRITE_CONSOLE_W_DESC = 
        FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS);
    
    // Constants
    private static final int STD_OUTPUT_HANDLE = -11;
    
    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            var setConsoleOutputCP = findFunction("SetConsoleOutputCP", SET_CONSOLE_OUTPUT_CP_DESC);
            var setConsoleCP = findFunction("SetConsoleCP", SET_CONSOLE_CP_DESC);
            var getConsoleOutputCP = findFunction("GetConsoleOutputCP", GET_CONSOLE_OUTPUT_CP_DESC);
            var getConsoleCP = findFunction("GetConsoleCP", GET_CONSOLE_CP_DESC);
            var getStdHandle = findFunction("GetStdHandle", GET_STD_HANDLE_DESC);
            var writeConsoleW = findFunction("WriteConsoleW", WRITE_CONSOLE_W_DESC);
            
            // Get current code pages
            var currentOutputCP = (int) getConsoleOutputCP.invoke();
            var currentInputCP = (int) getConsoleCP.invoke();
            System.out.println("Current output code page: " + currentOutputCP);
            System.out.println("Current input code page: " + currentInputCP);
            
            // Set both input and output to UTF-8 (65001) - this is what chcp does
            var outputResult = (boolean) setConsoleOutputCP.invoke(65001);
            var inputResult = (boolean) setConsoleCP.invoke(65001);
            
            if (outputResult && inputResult) {
                var newOutputCP = (int) getConsoleOutputCP.invoke();
                var newInputCP = (int) getConsoleCP.invoke();
                System.out.println("Successfully set output code page to: " + newOutputCP);
                System.out.println("Successfully set input code page to: " + newInputCP);
                
                // Force Java to reinitialize console streams with new encoding
                // This is the key - Java caches the console encoding at startup
                try {
                    // Get the current console and force it to reinitialize with new encoding
                    var consoleCharset = java.nio.charset.Charset.forName("CP" + newOutputCP);
                    System.setOut(new PrintStream(System.out, true, consoleCharset));
                    System.setErr(new PrintStream(System.err, true, consoleCharset));
                } catch (Exception e) {
                    // Fallback to UTF-8 if CP65001 charset name doesn't work
                    System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
                    System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
                }
                
                // Get console handle for direct Unicode output
                var hOut = (MemorySegment) getStdHandle.invoke(STD_OUTPUT_HANDLE);
                
                // Test Unicode using WriteConsoleW (the proper way)
                writeUnicodeToConsole(writeConsoleW, hOut, arena, "Direct Unicode via WriteConsoleW: 🚀 ñ ü 中文 🎉\n");
                writeUnicodeToConsole(writeConsoleW, hOut, arena, "More Unicode: àáâãäå çñüß ©®™ ┌─┐\n");
                
                // Now test System.out - should work after reinitializing streams
                System.out.println("System.out (now fixed): 🚀 ñ ü 中文 🎉");
                
            } else {
                System.err.println("Failed to set console code pages");
                System.err.println("Output result: " + outputResult);
                System.err.println("Input result: " + inputResult);
                System.exit(1);
            }
            
        } catch (Throwable e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void writeUnicodeToConsole(MethodHandle writeConsoleW, MemorySegment hOut, 
                                            Arena arena, String text) throws Throwable {
        // Convert Java String to UTF-16 char array for WriteConsoleW
        char[] chars = text.toCharArray();
        var buffer = arena.allocateFrom(JAVA_CHAR, chars);
        var written = arena.allocate(JAVA_INT);
        
        writeConsoleW.invoke(hOut, buffer, chars.length, written, MemorySegment.NULL);
    }
    
    private static MethodHandle findFunction(String name, FunctionDescriptor descriptor) {
        return KERNEL32.find(name)
            .map(addr -> LINKER.downcallHandle(addr, descriptor))
            .orElseThrow(() -> new RuntimeException("Function '" + name + "' not found"));
    }
}