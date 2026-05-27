package com.nlinterpreter.model;

/**
 * A single lexical token produced by the Lexer.
 *
 * Fields:
 *   value    — the word exactly as typed by the user
 *   type     — POS classification from the Lexicon
 *   phonetic — "syllables|ipa"  e.g.  "ọ-kùn-rin|okũrin"
 *              UNKNOWN tokens get phonetic = "?"
 *
 * The phonetic field format is defined by JsonLexiconLoader and consumed
 * by PhoneticEngine (which splits on "|").
 */
public class Token {

    private final String    value;
    private final TokenType type;
    private final String    phonetic;

    public Token(String value, TokenType type, String phonetic) {
        this.value    = value;
        this.type     = type;
        this.phonetic = phonetic;
    }

    public String    getValue()    { return value;    }
    public TokenType getType()     { return type;     }
    public String    getPhonetic() { return phonetic; }

    @Override
    public String toString() {
        return String.format("<%s, \"%s\">", type, value);
    }
}
