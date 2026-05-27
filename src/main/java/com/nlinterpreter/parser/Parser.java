package com.nlinterpreter.parser;

import com.nlinterpreter.model.Token;
import com.nlinterpreter.model.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent CFG parser for Yoruba sentence structure.
 *
 * Grammar (First-Set Technique — Abiola et al. 2014):
 *
 *   S  → NP VP
 *
 *   NP → PRONOUN
 *      | NOUN (ADJECTIVE)? (ADJECTIVE)? (DETERMINER)?
 *
 *   VP → VERB (NP)? (PP)?
 *
 *   PP → PREPOSITION NP
 *
 * Yoruba NP word order is HEAD-INITIAL: N + ADJ + DET, unlike English.
 * (Abiola et al. 2014 — 29 CFG rules for Yoruba noun phrases.)
 *
 * VP rules:  VP → V | V NP | V NP PP
 * (Ayoade & Eludiora 2020 — VP machine translation system.)
 *
 * UNKNOWN tokens (already flagged as lexical errors) are silently
 * skipped during all match() calls so a single bad word does not
 * cascade into spurious syntax errors.
 */
public class Parser {

    private final List<Token>  tokens;
    private       int          pos;
    private final List<String> syntaxErrors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    /**
     * Parses the token list against the Yoruba CFG.
     *
     * @return true if the sentence is syntactically valid, false otherwise
     */
    public boolean parse() {
        pos = 0;
        syntaxErrors.clear();

        if (tokens.isEmpty()) {
            syntaxErrors.add("SYNTAX ERROR: Empty input — nothing to parse.");
            return false;
        }

        // Skip any leading UNKNOWN tokens (already flagged as lexical errors)
        skipUnknown();

        boolean valid = parseS();

        // Skip any trailing UNKNOWN tokens
        skipUnknown();

        if (pos < tokens.size()) {
            // There are leftover non-UNKNOWN tokens — the sentence is too long
            // or follows an unrecognised structure.
            syntaxErrors.add(
                "SYNTAX ERROR: Unexpected token '" +
                tokens.get(pos).getValue() +
                "' (" + tokens.get(pos).getType() + ") at position " + (pos + 1) +
                ". Expected end of sentence."
            );
            return false;
        }
        return valid;
    }

    // ── Grammar rules ────────────────────────────────────────────────────────

    // S → NP VP
    private boolean parseS() {
        int savedPos = pos;
        if (parseNP() && parseVP()) return true;

        pos = savedPos;
        syntaxErrors.add(
            "SYNTAX ERROR: Sentence must follow the pattern [Subject + Verb phrase]. " +
            "Found: " + describeRemaining()
        );
        return false;
    }

    // NP → PRONOUN
    //    | NOUN (ADJECTIVE)? (ADJECTIVE)? (DETERMINER)?
    //
    // Abiola et al. R1–R6, R16–R20 (Head-initial NP ordering)
    private boolean parseNP() {
        skipUnknown();

        if (match(TokenType.PRONOUN)) return true;

        if (match(TokenType.NOUN)) {
            skipUnknown();
            match(TokenType.ADJECTIVE);   // optional first adjective  (R4/R5)
            skipUnknown();
            match(TokenType.ADJECTIVE);   // optional second adjective (R6)
            skipUnknown();
            match(TokenType.DETERMINER);  // optional determiner       (R1/R2)
            return true;
        }

        return false;
    }

    // VP → VERB (NP)? (PP)?
    // Ayoade & Eludiora 2020: VP → V | V NP | V NP PP
    private boolean parseVP() {
        skipUnknown();

        if (!match(TokenType.VERB)) return false;

        skipUnknown();
        int savedPos = pos;
        if (!parseNP()) pos = savedPos;   // NP is optional

        skipUnknown();
        int savedPos2 = pos;
        if (!parsePP()) pos = savedPos2;  // PP is optional

        return true;
    }

    // PP → PREPOSITION NP
    private boolean parsePP() {
        int savedPos = pos;
        if (match(TokenType.PREPOSITION) && parseNP()) return true;
        pos = savedPos;
        return false;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Advances pos if the current token matches the expected type.
     * Skips any UNKNOWN tokens that appear before the target type.
     */
    private boolean match(TokenType type) {
        if (pos < tokens.size() && tokens.get(pos).getType() == type) {
            pos++;
            return true;
        }
        return false;
    }

    /**
     * Advances past any consecutive UNKNOWN tokens.
     * UNKNOWN tokens produce lexical errors, not syntax errors.
     */
    private void skipUnknown() {
        while (pos < tokens.size() && tokens.get(pos).getType() == TokenType.UNKNOWN) {
            pos++;
        }
    }

    /** Human-readable description of the token at the current position. */
    private String describeRemaining() {
        if (pos >= tokens.size()) return "end of input";
        Token t = tokens.get(pos);
        return "'" + t.getValue() + "' (" + t.getType() + ")";
    }

    /** Returns accumulated syntax error messages (read-only). */
    public List<String> getSyntaxErrors() {
        return syntaxErrors;
    }
}
