package com.nlinterpreter.lexicon;

import com.nlinterpreter.model.Token;
import com.nlinterpreter.model.TokenType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory symbol table for the Yoruba lexicon.
 *
 * Two internal maps:
 *   entries  — word.toLowerCase() → Token  (diacritized lookup)
 *   fallback — base.toLowerCase() → canonical.toLowerCase()
 *              (undiacritized input "omo" → "ọmọ")
 *
 * Design follows MENYO-20k (Adelani et al.) requirement that diacritics
 * are mandatory for word identity — so "fà" and "fá" are distinct keys.
 * The fallback allows the user to type without diacritics and still
 * get a meaningful result (resolves to whichever sense was loaded last).
 */
public class Lexicon {

    private final Map<String, Token>  entries  = new HashMap<>();
    private final Map<String, String> fallback = new HashMap<>();

    /**
     * Register a word under its diacritized (canonical) lowercase form.
     *
     * @param word     canonical tone-marked Yoruba word
     * @param pos      POS tag
     * @param phonetic "syllables|ipa" string, e.g. "ọ-kùn-rin|okũrin"
     */
    public void add(String word, TokenType pos, String phonetic) {
        entries.put(word.toLowerCase(), new Token(word, pos, phonetic));
    }

    /**
     * Register an undiacritized fallback mapping.
     * "omo" → "ọmọ" so undiacritized user input still resolves.
     *
     * @param base      undiacritized form (e.g. "omo")
     * @param canonical diacritized canonical form (e.g. "ọmọ")
     */
    public void addFallback(String base, String canonical) {
        fallback.put(base.toLowerCase(), canonical.toLowerCase());
    }

    /**
     * Look up a word: tries diacritized entries first, then fallback map.
     *
     * @param word the token string as typed by the user
     * @return the Token if found, empty Optional otherwise
     */
    public Optional<Token> lookup(String word) {
        String key = word.toLowerCase();

        // 1. Direct diacritized match
        Token t = entries.get(key);
        if (t != null) return Optional.of(t);

        // 2. Fallback: undiacritized → canonical → entry
        String canon = fallback.get(key);
        if (canon != null) {
            Token fb = entries.get(canon);
            if (fb != null) return Optional.of(fb);
        }

        return Optional.empty();
    }

    /** Returns the number of entries in the main table. */
    public int size() {
        return entries.size();
    }
}
