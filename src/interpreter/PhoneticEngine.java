import java.util.*;

public class PhoneticEngine {

    // Tone character sets — from MENYO-20k diacritic documentation
    private static final String HIGH_TONE  = "áéíóúẹ́ọ́";
    private static final String LOW_TONE   = "àèìòùẹ̀ọ̀";
    private static final String MID_VOWELS = "aeiouẹọ";

    public String getPhoneticReading(Token token) {
        if (!token.getPhonetic().equals("?")) {
            // Use lexicon phonetic + extract tone pattern
            return "[" + token.getPhonetic() + "] " + getTonePattern(token.getValue());
        }
        // Fallback: DRSA-lite for unknown tokens (Akinwonmi 2024)
        String syllabified = syllabifyDRSA(token.getValue());
        return "[" + syllabified + "] " + getTonePattern(token.getValue());
    }

    // Reads diacritics left to right and maps to H/M/L
    // MENYO-20k: tone marks are mandatory for Yoruba generation
    private String getTonePattern(String word) {
        StringBuilder tones = new StringBuilder("tones: ");
        for (char c : word.toCharArray()) {
            if (HIGH_TONE.indexOf(c) >= 0)  tones.append("H");
            else if (LOW_TONE.indexOf(c) >= 0) tones.append("L");
            else if (MID_VOWELS.indexOf(c) >= 0) tones.append("M");
        }
        return tones.toString().equals("tones: ") ? "" : tones.toString();
    }

    // Simplified DRSA from Akinwonmi 2024
    // Handles CV, V, N, CVn syllable structures
    private String syllabifyDRSA(String word) {
        List<String> syllables = new ArrayList<>();
        int i = 0;
        while (i < word.length()) {
            char c = word.charAt(i);
            if (isConsonant(c) && i + 1 < word.length() && isVowel(word.charAt(i + 1))) {
                // CV syllable
                if (i + 2 < word.length() && isNasal(word.charAt(i + 2))) {
                    syllables.add(word.substring(i, i + 3)); // CVn
                    i += 3;
                } else {
                    syllables.add(word.substring(i, i + 2)); // CV
                    i += 2;
                }
            } else if (isVowel(c)) {
                if (i + 1 < word.length() && isNasal(word.charAt(i + 1))) {
                    syllables.add(word.substring(i, i + 2)); // Vn
                    i += 2;
                } else {
                    syllables.add(String.valueOf(c)); // V
                    i++;
                }
            } else if (isNasal(c)) {
                syllables.add(String.valueOf(c)); // N (syllabic nasal)
                i++;
            } else {
                syllables.add(String.valueOf(c));
                i++;
            }
        }
        return String.join("-", syllables);
    }

    private boolean isVowel(char c) {
        return "aeiouáàâéèêíìîóòôúùûẹọ".indexOf(Character.toLowerCase(c)) >= 0;
    }

    private boolean isConsonant(char c) {
        return Character.isLetter(c) && !isVowel(c) && !isNasal(c);
    }

    private boolean isNasal(char c) {
        return c == 'n' || c == 'm';
    }
}