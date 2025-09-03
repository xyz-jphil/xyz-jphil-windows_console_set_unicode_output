///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 22+

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Test2_UTF8Reset {
    public static void main(String[] args) {
        System.out.println("=== Test 2: Reset System.out to UTF-8 (no native changes) ===");
        System.out.println("BEFORE reset:");
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        
        // Reset System.out to UTF-8 without touching native console
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        
        System.out.println("AFTER reset to UTF-8:");
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        System.out.println("  More Unicode: àáâãäå çñüß ©®™ ┌─┐");
    }
}