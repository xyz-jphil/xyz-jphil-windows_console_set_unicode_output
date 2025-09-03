# Windows Console Unicode Output

A Java utility library that enables proper Unicode console output on Windows systems using the Foreign Function & Memory (FFM) API.

## Problem and Solution

Windows console applications often display Unicode characters incorrectly, showing question marks or garbled text instead of emojis, international characters, and symbols. 

### Why Not Just Use `chcp 65001`?

The traditional solution `chcp 65001` has a critical limitation: **it only affects the NEXT process, not the currently running Java process**. This forces developers to use batch files:

```batch
@echo off
chcp 65001 > nul
java MyApplication
```

This approach is cumbersome and doesn't work for applications that need to enable Unicode output dynamically.

### Our Solution

This library performs the **same effect as `chcp 65001`** but **applies it to the current running Java process** by:

1. **Direct Windows API calls**: Uses `SetConsoleOutputCP(65001)` via FFM API to set the console code page programmatically
2. **Stream replacement**: Swaps Java's `System.out` and `System.err` streams with UTF-8 encoded versions
3. **Immediate effect**: Changes take effect instantly in the current process, no batch files needed

The result is identical to `chcp 65001` but works within your running Java application without external process management.

## Requirements

- **Java 22+** (JDK 22+ recommended for full FFM API support)
  - **JDK 21**: May work with minor modifications or out-of-the-box, but shows compatibility warnings
  - **Below JDK 21**: Not supported - library will return failure result with specific error message
- **Windows OS only** (useless on non-Windows environments)
  - Automatically detects OS and returns `UNSUPPORTED_OS` failure on Linux/Mac/other platforms
  - No crashes or exceptions - fails gracefully with typed error results
- **Native access enabled** for the module (automatically configured in JAR manifest)

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.xyz-jphil</groupId>
    <artifactId>xyz-jphil-windows_console_set_unicode_output</artifactId>
    <version>1.0</version>
</dependency>
```

### Basic Usage

```java
import xyz.jphil.windows_console_set_unicode_output.WindowsConsoleSetUnicodeOutput;
import xyz.jphil.windows_console_set_unicode_output.WindowsConsoleSetUnicodeOutput.EnableResult;

public class MyApp {
    public static void main(String[] args) {
        // Enable Unicode output at application start
        EnableResult result = WindowsConsoleSetUnicodeOutput.enable();
        
        // Handle the result
        switch (result) {
            case EnableResult.Success() -> 
                System.out.println("✅ Unicode output enabled!");
            case EnableResult.AlreadyEnabled() -> 
                System.out.println("ℹ️ Unicode output was already enabled");
            case EnableResult.Failure(var reason, var cause) -> 
                System.err.println("❌ Failed to enable Unicode: " + reason.getDescription());
        }
        
        // Now you can safely print Unicode characters
        System.out.println("🚀 Hello World! 中文 🎉");
        System.out.println("àáâãäå çñüß ©®™ ┌─┐");
    }
}
```

### Type-Safe Result Handling and Safe Execution

The library **never throws exceptions** - instead it uses sealed interfaces for comprehensive, type-safe result handling:

```java
EnableResult result = WindowsConsoleSetUnicodeOutput.enable();

// Pattern matching with switch expression
String message = switch (result) {
    case EnableResult.Success() -> "Successfully enabled Unicode output";
    case EnableResult.AlreadyEnabled() -> "Unicode output was already enabled";
    case EnableResult.Failure(var reason, var cause) -> 
        "Failed: " + reason.getDescription() + 
        (cause != null ? " - " + cause.getMessage() : "");
};

System.out.println(message);
```

### Return Types and Safe Execution Rules

The library follows **safe execution principles**:

**Return Types** (sealed interface `EnableResult`):
- `Success()` - Unicode output successfully enabled
- `AlreadyEnabled()` - Was already enabled in a previous call
- `Failure(reason, cause)` - Failed with specific reason and optional underlying cause

**Failure Reasons** (`FailureReason` enum):
- `UNSUPPORTED_OS` - Not running on Windows (useless on Linux/Mac/other platforms)
- `UNSUPPORTED_JDK_VERSION` - JDK version below 21 (hard requirement)
- `NATIVE_ACCESS_DENIED` - Native access not available or blocked
- `KERNEL32_NOT_FOUND` - Windows kernel32.dll not found
- `FUNCTION_NOT_FOUND` - SetConsoleOutputCP function not found
- `API_CALL_FAILED` - Windows API call failed
- `STREAM_RESET_FAILED` - Failed to reset output streams

**Safety Guarantees**:
- ✅ **No exceptions thrown** - all failures return typed results
- ✅ **Platform detection** - fails gracefully on non-Windows systems  
- ✅ **Version validation** - checks JDK compatibility before attempting native calls
- ✅ **Idempotent** - safe to call multiple times
- ✅ **Thread-safe** - uses proper synchronization

## Running the Demo

The library includes a built-in demo:

```bash
java -jar xyz-jphil-windows_console_set_unicode_output-1.0.jar
```

Or run the main class directly:

```bash
java --module-path target/classes --module xyz.jphil.windows_console_set_unicode_output/xyz.jphil.windows_console_set_unicode_output.WindowsConsoleSetUnicodeOutput
```

## Building from Source

```bash
git clone https://github.com/xyz-jphil/xyz-jphil-windows_console_set_unicode_output.git
cd xyz-jphil-windows_console_set_unicode_output
mvn clean compile
```

## Testing and Experiments

### Test Scripts
The project includes comprehensive test files from the **experimental research phase** in the `test-scripts/` folder:

```bash
# Run all test variations (baseline, UTF-8 reset, CP65001, system properties)
cd test-scripts
run_all_tests.bat

# Compare visual Unicode output across different approaches
visual_test_comparison.bat

# Test minimal implementation variations
test_minimal_versions.bat

# Capture and save test outputs for comparison
run_tests_capture_output.bat
```

### Experimental Files
- **`src/test/java/`** - Contains all experimental Java test files:
  - `Test1_Baseline.java` - Baseline without any Unicode fixes ❌
  - `Test2_UTF8Reset.java` - UTF-8 stream reset approach ❌
  - `Test3_CP65001Reset.java` - Code page + stream reset approach ❌
  - `Test4_SystemProperty.java` - System property based approach ❌
  - `TestMinimal1_OnlyOutputCP.java` - Minimal code page only ❌
  - `TestMinimal2_NoSystemOut.java` - **Minimal working approach** ✅
  - `TestMinimal3_NoWriteConsoleW.java` - **Minimal working approach** ✅
- **`notes/`** - Research documentation from experimental phase
- **`test-scripts/*.txt`** - Captured outputs from different test approaches

### Research Notes
The `notes/` folder contains detailed documentation of the experimental process that led to the current implementation. **Most approaches failed** - only `TestMinimal2_NoSystemOut.java` and `TestMinimal3_NoWriteConsoleW.java` actually worked. The final library implementation is based on these two successful minimal approaches that proved most reliable across different Windows and JDK versions.

## Technical Details

### How It Works

1. **OS Detection**: Checks if running on Windows using `System.getProperty("os.name")`
2. **JDK Validation**: Ensures JDK 21+ (warns for JDK 21, recommends 22+)
3. **Native Access**: Uses FFM API to call Windows `kernel32.dll`
4. **API Call**: Invokes `SetConsoleOutputCP(65001)` to set UTF-8 code page
5. **Stream Reset**: Recreates `System.out` and `System.err` with UTF-8 encoding

### Module System

The library is a proper Java module with native access configured:

```java
module xyz.jphil.windows_console_set_unicode_output {
    exports xyz.jphil.windows_console_set_unicode_output;
    requires java.base;
}
```

The JAR manifest includes `Enable-Native-Access` for this specific module, providing secure native access without requiring `--enable-native-access=ALL-UNNAMED`.

## Safety Features

- **Platform Detection**: Automatically detects non-Windows systems and fails gracefully
- **Version Checking**: Validates JDK version compatibility
- **Idempotent**: Safe to call `enable()` multiple times
- **Thread Safe**: Uses volatile fields and proper synchronization
- **No Exceptions**: Returns typed results instead of throwing exceptions

## License

This project is available under the MIT License.

## Contributing

Contributions welcome! Please ensure all tests pass and follow the existing code style.

## Related

This library evolved from experimental JBang scripts that tested various approaches to Windows Unicode console output. The final implementation uses the minimal approach that proved most reliable across different Windows and JDK versions.