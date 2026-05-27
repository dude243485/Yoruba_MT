package com.nlinterpreter.lexicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlinterpreter.model.TokenType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Loads yoruba_lexicon.json into a Lexicon instance at runtime.
 *
 * The JSON was produced by generate_lexicon.py from the kaikki.org
 * Wiktionary JSONL dump. Each entry has the shape:
 *
 *   {
 *     "word":      "ọkùnrin",    // canonical tone-marked form (lookup key)
 *     "base":      "okunrin",    // undiacritized base (fallback key)
 *     "pos":       "NOUN",       // matches TokenType enum names
 *     "ipa":       "okũrin",     // IPA pronunciation (no / / slashes)
 *     "syllables": "ọ-kùn-rin", // DRSA-syllabified, hyphen-separated
 *     "gloss":     "man, male"   // English gloss (for diagnostics)
 *   }
 *
 * Dependency: Jackson-databind 2.17.0 (declared in pom.xml).
 */
public class JsonLexiconLoader {

    /**
     * Loads the lexicon from a file path.
     *
     * Tries two strategies in order:
     *   1. File path as given (works when run from project root or
     *      when an absolute path is supplied).
     *   2. Classpath resource "yoruba_lexicon.json" (works when the
     *      file has been placed in src/main/resources/).
     *
     * @param jsonPath path to yoruba_lexicon.json
     * @return populated Lexicon
     * @throws IOException if the file cannot be read
     */
    public static Lexicon load(String jsonPath) throws IOException {
        Lexicon      lexicon = new Lexicon();
        ObjectMapper mapper  = new ObjectMapper();
        JsonNode     root;

        // Strategy 1 — file on disk
        if (Files.exists(Paths.get(jsonPath))) {
            root = mapper.readTree(Paths.get(jsonPath).toFile());
        } else {
            // Strategy 2 — classpath resource
            InputStream is = JsonLexiconLoader.class
                    .getClassLoader()
                    .getResourceAsStream("yoruba_lexicon.json");
            if (is == null) {
                throw new IOException(
                        "yoruba_lexicon.json not found at '" + jsonPath +
                        "' and not on classpath.\n" +
                        "Place the file at the project root or in src/main/resources/.");
            }
            root = mapper.readTree(is);
        }

        int loaded = 0, skipped = 0;

        for (JsonNode node : root) {
            String word      = node.path("word").asText("").trim();
            String base      = node.path("base").asText("").trim();
            String posStr    = node.path("pos").asText("").trim();
            String ipa       = node.path("ipa").asText("").trim();
            String syllables = node.path("syllables").asText("").trim();
            // gloss is available but not stored — only used for debugging
            // String gloss  = node.path("gloss").asText("").trim();

            if (word.isEmpty() || posStr.isEmpty()) {
                skipped++;
                continue;
            }

            TokenType pos = mapPos(posStr);
            if (pos == null) {
                skipped++;
                continue;
            }

            // phonetic format consumed by PhoneticEngine:  "syllables|ipa"
            // e.g. "ọ-kùn-rin|okũrin"
            // If IPA is missing, fall back to the word itself
            String phoneticIpa = ipa.isEmpty() ? word : ipa;
            String phonetic    = syllables + "|" + phoneticIpa;

            // Register under the canonical (diacritized) form
            lexicon.add(word, pos, phonetic);

            // Register the undiacritized base form as a fallback
            // so "omo" still resolves if the user types without diacritics
            if (!base.isEmpty() && !base.equalsIgnoreCase(word)) {
                lexicon.addFallback(base, word);
            }

            loaded++;
        }

        System.out.printf("Lexicon loaded: %d entries, %d skipped%n", loaded, skipped);
        return lexicon;
    }

    /**
     * Maps the pos string from JSON to the TokenType enum.
     * Returns null for unrecognised POS tags (entry will be skipped).
     */
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
            case "NUMERAL"     -> TokenType.NUMERAL;
            default            -> null;
        };
    }
}
