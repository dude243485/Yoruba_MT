package com.nlinterpreter.lexer;

import com.nlinterpreter.lexicon.Lexicon;
import com.nlinterpreter.model.Token;
import com.nlinterpreter.model.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lexical analyser — converts an array of raw token strings into
 * a List&lt;Token&gt; with POS and phonetic data from the Lexicon.
 *
 * Unrecognised tokens are tagged UNKNOWN (phonetic = "?") and a
 * human-readable LEXICAL ERROR message is recorded.
 *
 * The error messages are later forwarded to ErrorReporter by Main.
 *
 * Design reference: MasakhaPOS (Dione et al. 2023) — Yoruba POS
 * tagging is performed via lexicon lookup (rule-based, not ML).
 */
public class Lexer {

    private final Lexicon      lexicon;
    private final List<String> lexicalErrors = new ArrayList<>();

    public Lexer(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    /**
     * Tokenises the raw string array produced by splitting user input.
     *
     * @param rawTokens array of whitespace-split words
     * @return ordered list of Tokens (one per input word)
     */
    public List<Token> tokenize(String[] rawTokens) {
        lexicalErrors.clear();
        List<Token> tokens = new ArrayList<>();

        for (int i = 0; i < rawTokens.length; i++) {
            String raw = rawTokens[i].trim();
            if (raw.isEmpty()) continue;

            Optional<Token> found = lexicon.lookup(raw);

            if (found.isPresent()) {
                // Reconstruct token so the *user-typed* value is preserved
                Token t = found.get();
                tokens.add(new Token(raw, t.getType(), t.getPhonetic()));
            } else {
                tokens.add(new Token(raw, TokenType.UNKNOWN, "?"));
                lexicalErrors.add(
                    "LEXICAL ERROR at position " + (i + 1) +
                    ": unrecognized token '" + raw + "'"
                );
            }
        }
        return tokens;
    }

    /** Returns the accumulated lexical error messages (read-only). */
    public List<String> getLexicalErrors() {
        return lexicalErrors;
    }
}
