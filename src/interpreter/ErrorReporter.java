package interpreter;

import java.util.*;

public class ErrorReporter {
    private final List<String> errors = new ArrayList<>();

    public void addError(String msg) { errors.add(msg); }

    public void report() {
        if (errors.isEmpty()) {
            System.out.println("✓  No errors detected.");
            return;
        }
        System.out.println("\n╔══ Error Report ══════════════════╗");
        errors.forEach(e -> System.out.println("  " + e));
        System.out.println("╚══════════════════════════════════╝");
    }

    public boolean hasErrors() { return !errors.isEmpty(); }
}
