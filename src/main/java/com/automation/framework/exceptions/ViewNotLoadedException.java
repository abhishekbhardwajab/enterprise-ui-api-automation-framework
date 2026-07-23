package com.automation.framework.exceptions;

/**
 * Thrown by page-load / element-wait methods when an expected view fails to
 * render within the configured timeout. Wrapping Selenium's generic
 * TimeoutException in a descriptive, framework-specific exception makes
 * failures easier to diagnose from a stack trace or CI log alone.
 */
public class ViewNotLoadedException extends RuntimeException {

    public ViewNotLoadedException(String message) {
        super(message);
    }

    public ViewNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
