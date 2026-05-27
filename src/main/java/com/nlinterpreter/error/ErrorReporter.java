package com.nlinterpreter.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects and prints all interpreter errors.
 *
 * Receives both lexical errors (from Lexer) and syntax errors
 * (from Parser) via addError(), then prints them all in a
 * formatted block via report().
 */
public class ErrorReporter {

    private final List<String> errors = new ArrayList<>();

    /** Adds a single error message. */
    public void addError(String message) {
        errors.add(message);
    }

    /**
     * Prints the error report to stdout.
     * If there are no errors, prints a success message.
     */
    public void report() {
        System.out.println();
        if (errors.isEmpty()) {
            System.out.println("=== ERROR REPORT ===");
            System.out.println("No errors detected.");
            return;
        }

        System.out.println("=== ERROR REPORT ===");
        for (String error : errors) {
            System.out.println("  " + error);
        }
    }

    /** Returns true if any errors were recorded. */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
