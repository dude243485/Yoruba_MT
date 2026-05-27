package interpreter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonLexiconLoader {

    /**
     * Loads yoruba_lexicon.json into a Lexicon instance.
     * The JSON was produced by extract_lexicon.py.
     */
    public static Lexicon load(String jsonPath) throws IOException {
        Lexicon lexicon = new Lexicon();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(jsonPath));

        int loaded = 0, skipped = 0;

        for (JsonNode node : root) {
            String word     = node.path("word").asText("").trim();
            String base     = node.path("base").asText("").trim();
            String posStr   = node.path("pos").asText("").trim();
            String ipa      = node.path("ipa").asText("").trim();
            String syllables= node.path("syllables").asText("").trim();
            String gloss    = node.path("gloss").asText("").trim();

            if (word.isEmpty() || posStr.isEmpty()) {
                skipped++;
                continue;
            }

            TokenType pos = mapPos(posStr);
            if (pos == null) {
                skipped++;
                continue;
            }

            // Build phonetic string for PhoneticEngine:
            // format: "syllables|ipa"  e.g. "ọ-mọ|ọmọ"
            String phonetic = syllables + "|" + ipa;

            // Register under the canonical (diacritized) form
            lexicon.add(word, pos, phonetic);

            // Also register the undiacritized base form as a fallback
            // so "omo" still resolves if user types without diacritics
            if (!base.isEmpty() && !base.equalsIgnoreCase(word)) {
                lexicon.addFallback(base, word);
            }

            loaded++;
        }

        System.out.printf("Lexicon loaded: %d entries, %d skipped%n", loaded, skipped);
        return lexicon;
    }

    private static TokenType mapPos(String pos) {
        return switch (pos) {
            case "VERB"        -> TokenType.VERB;
            case "NOUN"        -> TokenType.NOUN;
            case "PRONOUN"     -> TokenType.PRONOUN;
            case "PREPOSITION" -> TokenType.PREPOSITION;
            case "ADJECTIVE"   -> TokenType.ADJECTIVE;
            case "DETERMINER"  -> TokenType.DETERMINER;
            case "CONJUNCTION" -> TokenType.CONJUNCTION;
            case "PARTICLE"    -> TokenType.PARTICLE;
            case "ADVERB"      -> TokenType.ADVERB;
            default            -> null;
        };
    }
}
