///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 22+

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Test4_SystemProperty {
    public static void main(String[] args) {
        System.out.println("=== Test 4: Set file.encoding property + reset (no native changes) ===");
        System.out.println("BEFORE changes:");
        System.out.println("  file.encoding: " + System.getProperty("file.encoding"));
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        
        // Try setting file.encoding property (usually doesn't work at runtime)
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("AFTER setting file.encoding=UTF-8:");
        System.out.println("  file.encoding: " + System.getProperty("file.encoding"));
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        
        // Now also reset System.out
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.out.println("AFTER also resetting System.out:");
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        System.out.println("  More Unicode: àáâãäå çñüß ©®™ ┌─┐");
    }
}