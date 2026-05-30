package com.nlinterpreter.phonetic;

import com.nlinterpreter.model.Token;

/**
 * Phonetic engine — formats the phonetic reading for a token.
 *
 * The phonetic field of each Token has the format "syllables|ipa"
 * (e.g. "ọ-kùn-rin|okũrin"), set by JsonLexiconLoader.
 * This class splits that string and additionally derives the
 * Yoruba tone pattern from the token value's diacritics.
 * For UNKNOWN tokens (phonetic = "?"), a graceful fallback is returned.
 *
 * Akinwonmi 2024: The DRSA syllabification data is already stored in
 * the lexicon; we do not recompute it here, which sidesteps the
 * nasal-vowel mis-syllabification problem described in the paper.
 */
public class PhoneticEngine {

    // Precomposed NFC forms for high-tone vowels (acute accent)
    // Includes plain dotted vowels ẹ ọ with acute
    private static final String HIGH_TONE =
            "áéíóú"           // U+00E1 U+00E9 U+00ED U+00F3 U+00FA
          + "\u1EB9\u0301"    // ẹ + combining acute (NFD form)
          + "\u1ECD\u0301"    // ọ + combining acute (NFD form)
          + "\u1EBB"          // ẹ́ precomposed  (rare but possible)
          + "\u1ECF"          // ọ́ precomposed
          + "\u1E3F";         // ḿ (nasal with high tone, if present)

    // Precomposed NFC forms for low-tone vowels (grave accent)
    private static final String LOW_TONE =
            "àèìòù"           // U+00E0 U+00E8 U+00EC U+00F2 U+00F9
          + "\u1EB9\u0300"    // ẹ + combining grave
          + "\u1ECD\u0300"    // ọ + combining grave
          + "\u1EBD"          // ẹ̀ precomposed (ẽ, if mapped)
          + "\u1ED1"          // ọ̀ — not standard but guard it
          + "\u1EB8";         // ẹ̀ (some encodings)

    // Plain mid-tone vowels (no diacritic mark)
    private static final String MID_VOWELS = "aeiou\u1EB9\u1ECD"; // a e i o u ẹ ọ

    /**
     * Returns the full phonetic reading line for a token.
     *
     * Format: "syllables: X | pronunciation: Y | tones: Z"
     *
     * @param token the Token to describe
     * @return formatted phonetic string
     */
    public String getPhoneticReading(Token token) {
        String ph = token.getPhonetic();

        if ("?".equals(ph)) {
            // Unknown token — no lexicon entry
            String tones = getTonePattern(token.getValue());
            return "syllables: ? | pronunciation: not found" +
                   (tones.isEmpty() ? "" : " | tones: " + tones);
        }

        // Split the stored "syllables|ipa" field
        String[] parts      = ph.split("\\|", 2);
        String   syllables  = parts[0].trim();
        String   ipa        = parts.length > 1 ? parts[1].trim() : syllables;
        String   tones      = getTonePattern(token.getValue());

        return "syllables: " + syllables +
               " | pronunciation: " + ipa +
               (tones.isEmpty() ? "" : " | tones: " + tones);
    }

    /**
     * Derives the Yoruba tone pattern by scanning vowel diacritics
     * left-to-right across the word.
     *
     * Each vowel contributes one letter to the tone pattern:
     *   H = high (acute), L = low (grave), M = mid (plain)
     *
     * @param word the original word value (with diacritics)
     * @return tone string such as "HML" or "" if no vowels detected
     */
    private String getTonePattern(String word) {
        StringBuilder tones = new StringBuilder();
        int i = 0;
        while (i < word.length()) {
            char c = word.charAt(i);

            // Detect combining diacritical marks (U+0300–U+036F)
            // These modify the previous base character.
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                if (c == '\u0301') {           // combining acute → high
                    if (tones.length() > 0 && tones.charAt(tones.length() - 1) == 'M') {
                        tones.setCharAt(tones.length() - 1, 'H');
                    } else {
                        tones.append("H");
                    }
                } else if (c == '\u0300') {    // combining grave → low
                    if (tones.length() > 0 && tones.charAt(tones.length() - 1) == 'M') {
                        tones.setCharAt(tones.length() - 1, 'L');
                    } else {
                        tones.append("L");
                    }
                }
                i++;
                continue;
            }

            // Precomposed characters
            if (HIGH_TONE.indexOf(c) >= 0) {
                tones.append("H");
            } else if (LOW_TONE.indexOf(c) >= 0) {
                tones.append("L");
            } else if (MID_VOWELS.indexOf(c) >= 0) {
                tones.append("M");
            }
            i++;
        }
        return tones.toString();
    }
}
