package com.nlinterpreter.model;

/**
 * Part-of-speech categories for Yoruba tokens.
 *
 * Values match the "pos" strings in yoruba_lexicon.json exactly.
 * Justified by MasakhaPOS (Dione et al. 2023) POS tag set for Yoruba.
 */
public enum TokenType {
    VERB,
    NOUN,
    PRONOUN,
    PREPOSITION,
    ADJECTIVE,
    DETERMINER,
    CONJUNCTION,
    PARTICLE,
    ADVERB,
    NUMERAL,
    UNKNOWN
}
