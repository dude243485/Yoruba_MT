package com.nlinterpreter;

import com.nlinterpreter.error.ErrorReporter;
import com.nlinterpreter.lexer.Lexer;
import com.nlinterpreter.lexicon.JsonLexiconLoader;
import com.nlinterpreter.lexicon.Lexicon;
import com.nlinterpreter.model.Token;
import com.nlinterpreter.parser.Parser;
import com.nlinterpreter.phonetic.PhoneticEngine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Yoruba Natural Language Interpreter.
 *
 * Pipeline (CSC 334 — Systems Programming):
 *   1. Load Lexicon  — JsonLexiconLoader reads yoruba_lexicon.json
 *   2. Tokenize      — user input split by whitespace
 *   3. Lex           — Lexer classifies each token via Lexicon lookup
 *   4. Print table   — Lexical Analysis section (TOKEN | POS | SYLLABLES | …)
 *   5. Parse         — Parser validates sentence structure (CFG)
 *   6. Print result  — Syntax Validation section
 *   7. Phonetics     — PhoneticEngine formats each token's reading
 *   8. Error report  — ErrorReporter aggregates lexical + syntax errors
 *

 */
public class Main {

    public static void main(String[] args) {

        // Force UTF-8 output on Windows so Yoruba characters print correctly
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // If this fails we continue anyway — characters may display oddly
        }

        // ── 1. Load lexicon ───────────────────────────────────────────────────
        Lexicon lexicon;
        try {
            lexicon = JsonLexiconLoader.load("yoruba_lexicon.json");
        } catch (IOException e) {
            System.err.println("FATAL: Could not load lexicon — " + e.getMessage());
            System.exit(1);
            return; // unreachable; satisfies compiler
        }

        // ── 2. Read input (loop) ──────────────────────────────────────────────
        Scanner       sc     = new Scanner(System.in, StandardCharsets.UTF_8);
        Lexer         lexer  = new Lexer(lexicon);
        PhoneticEngine engine = new PhoneticEngine();

        System.out.println();
        System.out.println("============================================");
        System.out.println("  Yoruba Natural Language Interpreter");
        System.out.println("  Type 'exit' or press Ctrl+Z to quit.");
        System.out.println("============================================");

        while (true) {

            // Prompt
            System.out.println();
            System.out.print("Enter a Yoruba sentence: ");

            // EOF (Ctrl+Z on Windows)
            if (!sc.hasNextLine()) {
                System.out.println("\nGoodbye!");
                break;
            }

            String input = sc.nextLine().trim();

            // Exit keyword
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            // Empty input — re-prompt without running the pipeline
            if (input.isEmpty()) {
                System.out.println("Please enter a sentence.");
                continue;
            }

            // ── 3. Tokenize (split on whitespace) ────────────────────────────
            String[] rawTokens = input.split("\\s+");

            // ── 4. Lex ───────────────────────────────────────────────────────
            List<Token> tokens = lexer.tokenize(rawTokens);

            // ── 5. Print Lexical Analysis Table ──────────────────────────────
            System.out.println();
            System.out.println("=== LEXICAL ANALYSIS ===");
            System.out.printf("%-18s %-15s %-22s %-22s%n",
                    "TOKEN", "POS", "SYLLABLES", "PRONUNCIATION");
            System.out.println("-".repeat(79));

            for (Token t : tokens) {
                String phonetic  = t.getPhonetic();
                String syllables = "?";
                String ipa       = "?";

                if (!"?".equals(phonetic)) {
                    String[] parts = phonetic.split("\\|", 2);
                    syllables = parts[0].trim();
                    ipa       = parts.length > 1 ? parts[1].trim() : syllables;
                }

                System.out.printf("%-18s %-15s %-22s %-22s%n",
                        t.getValue(), t.getType(), syllables, ipa);
            }

            // ── 6. Parse — Syntax Validation ─────────────────────────────────
            Parser  parser = new Parser(tokens);
            boolean valid  = parser.parse();

            System.out.println();
            System.out.println("=== SYNTAX VALIDATION ===");
            if (valid) {
                System.out.println("Valid sentence structure [NP VP]");
            } else {
                System.out.println("Invalid sentence structure.");
                parser.getSyntaxErrors().forEach(e -> System.out.println("  " + e));
            }

            // ── 7. Phonetic Reading ───────────────────────────────────────────
            System.out.println();
            System.out.println("=== PHONETIC READING ===");
            for (Token t : tokens) {
                System.out.printf("%-18s -> %s%n",
                        t.getValue(), engine.getPhoneticReading(t));
            }

            // ── 8. Error Report ───────────────────────────────────────────────
            ErrorReporter reporter = new ErrorReporter();
            lexer.getLexicalErrors().forEach(reporter::addError);
            parser.getSyntaxErrors().forEach(reporter::addError);
            reporter.report();

        } // end while

        sc.close();
    }
}
