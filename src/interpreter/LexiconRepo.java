public class YorubaLexicon extends Lexicon {
    public YorubaLexicon() {
        // NOUNS — Abiola et al. Table 1 examples
        add("ọmọ",     TokenType.NOUN,       "ọ-mọ");
        add("ọkùnrin", TokenType.NOUN,       "ọ-kùn-rin");
        add("ìyá",     TokenType.NOUN,       "ì-yá");
        add("bàbá",    TokenType.NOUN,       "bà-bá");
        add("ilé",     TokenType.NOUN,       "i-lé");
        add("eran",    TokenType.NOUN,       "e-ran");
        add("omi",     TokenType.NOUN,       "o-mi");
        add("tabili",  TokenType.NOUN,       "ta-bi-li");
        add("kìnún",   TokenType.NOUN,       "kì-nún");
        add("aso",     TokenType.NOUN,       "a-so");

        // VERBS — Ayoade & Eludiora corpus
        add("pa",      TokenType.VERB,       "pa");
        add("jẹ",      TokenType.VERB,       "jẹ");
        add("lo",      TokenType.VERB,       "lo");
        add("rí",      TokenType.VERB,       "rí");
        add("fò",      TokenType.VERB,       "fò");
        add("jókò",    TokenType.VERB,       "jó-kò");
        add("sè",      TokenType.VERB,       "sè");
        add("fún",     TokenType.VERB,       "fún");
        add("wà",      TokenType.VERB,       "wà");

        // ADJECTIVES — Abiola et al. Table 1
        add("kékeré",  TokenType.ADJECTIVE,  "ké-ke-ré");
        add("nlá",     TokenType.ADJECTIVE,  "n-lá");
        add("tútù",    TokenType.ADJECTIVE,  "tú-tù");
        add("gbígbóná", TokenType.ADJECTIVE, "gbíg-bó-ná");
        add("dúdú",    TokenType.ADJECTIVE,  "dú-dú");

        // DETERMINERS — Abiola et al. R1/R2 rules
        add("náà",     TokenType.DETERMINER, "náà");
        add("kan",     TokenType.DETERMINER, "kan");

        // PRONOUNS
        add("mo",      TokenType.PRONOUN,    "mo");
        add("o",       TokenType.PRONOUN,    "o");
        add("a",       TokenType.PRONOUN,    "a");
        add("ó",       TokenType.PRONOUN,    "ó");

        // PREPOSITIONS
        add("sí",      TokenType.PREPOSITION,"sí");
        add("lórí",    TokenType.PREPOSITION,"ló-rí");
        add("nínú",    TokenType.PREPOSITION,"ní-nú");
        add("fún",     TokenType.PREPOSITION,"fún");
    }
}