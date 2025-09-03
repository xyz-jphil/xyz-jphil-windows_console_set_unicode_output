///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 22+

public class Test1_Baseline {
    public static void main(String[] args) {
        System.out.println("=== Test 1: Baseline (no changes) ===");
        System.out.println("System default charset: " + java.nio.charset.Charset.defaultCharset());
        System.out.println("file.encoding property: " + System.getProperty("file.encoding"));
        System.out.println("Unicode test: 🚀 ñ ü 中文 🎉");
        System.out.println("More Unicode: àáâãäå çñüß ©®™ ┌─┐");
    }
}