///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 22+

import java.io.PrintStream;
import java.nio.charset.Charset;

public class Test3_CP65001Reset {
    public static void main(String[] args) {
        System.out.println("=== Test 3: Reset System.out to CP65001 (no native changes) ===");
        System.out.println("BEFORE reset:");
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        
        try {
            // Try to reset System.out to CP65001 (UTF-8 code page)
            var cp65001 = Charset.forName("CP65001");
            System.setOut(new PrintStream(System.out, true, cp65001));
            System.out.println("AFTER reset to CP65001:");
        } catch (Exception e) {
            System.out.println("Failed to get CP65001 charset: " + e.getMessage());
            System.out.println("Falling back to UTF-8...");
            System.setOut(new PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("AFTER reset to UTF-8:");
        }
        
        System.out.println("  Unicode test: 🚀 ñ ü 中文 🎉");
        System.out.println("  More Unicode: àáâãäå çñüß ©®™ ┌─┐");
    }
}