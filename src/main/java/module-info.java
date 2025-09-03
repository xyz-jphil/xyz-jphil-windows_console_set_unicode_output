/**
 * Windows Console Unicode Output module.
 * 
 * <p>Provides utilities for enabling Unicode console output on Windows platforms
 * using the Foreign Function & Memory (FFM) API.</p>
 * 
 * <p>This module requires native access to Windows kernel32.dll for calling
 * the SetConsoleOutputCP function.</p>
 * 
 * @since 1.0
 */
module xyz.jphil.windows_console_set_unicode_output {
    
    /**
     * Export the main package containing the Unicode console utilities.
     */
    exports xyz.jphil.windows_console_set_unicode_output;
    
    /**
     * Require java.base for core functionality.
     */
    requires java.base;
}