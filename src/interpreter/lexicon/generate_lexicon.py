"""
Yoruba Lexicon Generator
Extracts Yoruba entries from the kaikki.org Wiktionary JSONL dump
and produces yoruba_lexicon.json for the NL Interpreter.

Usage:
    python generate_lexicon.py <input.jsonl> <output.json>
"""

import json
import sys
import unicodedata
from collections import defaultdict

# Maps Wiktionary POS tags → TokenType enum names
POS_MAP = {
    "verb":      "VERB",
    "noun":      "NOUN",
    "pron":      "PRONOUN",
    "prep":      "PREPOSITION",
    "adj":       "ADJECTIVE",
    "adv":       "ADVERB",
    "conj":      "CONJUNCTION",
    "det":       "DETERMINER",
    "particle":  "PARTICLE",
    "num":       "NUMERAL",
}

# POS tags that carry no lexical value for the interpreter
SKIP_POS = {"character", "intj", "affix", "suffix", "prefix", "punct",
            "name", "phrase", "proverb", "romanization"}

# Yoruba vowels (precomposed NFC forms) — used by syllabifier
VOWELS = set("aeiouàáâäèéêëìíîïòóôöùúûü"
             "\u1EB9\u1EBA\u1EBB\u1EBC\u1EBD"   # ẹ variants
             "\u1ECC\u1ECD\u1ECE\u1ECF\u1ED0"   # ọ variants
             "\u1EB8\u1EB9\u1ECA\u1ECB\u1ECC\u1ECD")  # more dotted
NASALS = {'n', 'm'}


def nfc(s):
    """NFC-normalize a string so precomposed diacritics are canonical."""
    return unicodedata.normalize("NFC", s) if s else s


def get_canonical(entry):
    """
    Gets the tone-marked canonical form.
    Priority: forms[canonical tag] > head_templates expansion > word field.
    Always NFC-normalizes the result.
    """
    for form in entry.get("forms", []):
        if "canonical" in form.get("tags", []):
            f = nfc(form.get("form", "").strip())
            if f and " " not in f and "\n" not in f:
                return f

    templates = entry.get("head_templates", [])
    if templates:
        exp = nfc(templates[0].get("expansion", "").strip())
        if exp:
            return exp

    return nfc(entry.get("word", "").strip())


def get_ipa(entry):
    """Gets the first IPA pronunciation, strips slashes, NFC-normalizes."""
    for sound in entry.get("sounds", []):
        ipa = sound.get("ipa", "")
        if ipa:
            return nfc(ipa.strip("/").strip())
    return ""


def get_gloss(entry):
    """Gets the first clean gloss from the first sense."""
    senses = entry.get("senses", [])
    if not senses:
        return ""
    glosses = senses[0].get("glosses", [])
    return glosses[0].strip() if glosses else ""


def is_vowel(c):
    return c.lower() in VOWELS or unicodedata.category(c) in ("Ll", "Lu") and \
           unicodedata.decomposition(c).startswith("0061") or \
           c.lower() in "aeiou"


def simple_syllabify(word):
    """
    DRSA-lite syllabifier for the canonical Yoruba form.
    Handles V, CV, Vn, CVn, N syllable structures (Akinwonmi 2024).
    Produces hyphen-separated syllables, e.g. ọkùnrin → ọ-kùn-rin.
    Works on NFC-normalized strings.
    """
    # Build vowel check using Unicode category + known vowel chars
    def is_v(c):
        lc = c.lower()
        return lc in VOWELS or lc in "aeiou"

    def is_n(c):
        return c in NASALS

    def is_alpha(c):
        return c.isalpha()

    syllables = []
    i = 0
    chars = list(word)
    n = len(chars)

    while i < n:
        c = chars[i]

        if is_v(c):
            # Vn (nasal vowel coda)?
            if i + 1 < n and is_n(chars[i + 1]):
                # Only Vn if nasal is NOT followed by a vowel
                if i + 2 >= n or not is_v(chars[i + 2]):
                    syllables.append(c + chars[i + 1])
                    i += 2
                    continue
            syllables.append(c)
            i += 1

        elif is_n(c) and (i + 1 >= n or not is_v(chars[i + 1])):
            # Syllabic nasal N
            syllables.append(c)
            i += 1

        elif is_alpha(c):
            # Consonant — look ahead
            if i + 1 < n and is_v(chars[i + 1]):
                cv = c + chars[i + 1]
                # CVn?
                if i + 2 < n and is_n(chars[i + 2]):
                    if i + 3 >= n or not is_v(chars[i + 3]):
                        syllables.append(cv + chars[i + 2])
                        i += 3
                        continue
                syllables.append(cv)
                i += 2
            else:
                # Lone consonant
                syllables.append(c)
                i += 1
        else:
            # Combining character or punctuation — attach to previous syllable
            if syllables:
                syllables[-1] += c
            else:
                syllables.append(c)
            i += 1

    return "-".join(syllables) if syllables else word


def process(input_path, output_path):
    results = []
    seen    = set()           # deduplicate by (canonical_lower, pos)
    skipped = defaultdict(int)
    counts  = defaultdict(int)

    with open(input_path, "r", encoding="utf-8") as f:
        for line_num, raw in enumerate(f, 1):
            raw = raw.strip()
            if not raw:
                continue

            try:
                entry = json.loads(raw)
            except json.JSONDecodeError as e:
                print(f"  [!] Line {line_num} parse error: {e}", file=sys.stderr)
                continue

            # Only Yoruba
            if entry.get("lang_code") != "yo":
                skipped["not_yoruba"] += 1
                continue

            pos = entry.get("pos", "")

            if pos in SKIP_POS:
                skipped[pos] += 1
                continue

            mapped_pos = POS_MAP.get(pos)
            if not mapped_pos:
                skipped[f"unknown_pos:{pos}"] += 1
                continue

            canonical = get_canonical(entry)
            if not canonical:
                skipped["no_canonical"] += 1
                continue

            # Skip multi-word entries
            if " " in canonical or "\n" in canonical:
                skipped["multiword"] += 1
                continue

            # Deduplicate — one entry per (word, POS) pair
            key = (canonical.lower(), mapped_pos)
            if key in seen:
                skipped["duplicate"] += 1
                continue
            seen.add(key)

            base_word  = nfc(entry.get("word", "").strip())
            ipa        = get_ipa(entry)
            gloss      = get_gloss(entry)
            syllables  = simple_syllabify(canonical)

            # Use canonical as IPA fallback if none found
            if not ipa:
                ipa = canonical

            results.append({
                "word":      canonical,    # tone-marked form: fà, fá, ọmọ
                "base":      base_word,    # undiacritized:    fa,  fa, omo
                "pos":       mapped_pos,   # VERB, NOUN, etc.
                "ipa":       ipa,          # phonetic pronunciation
                "syllables": syllables,    # DRSA-syllabified with hyphens
                "gloss":     gloss         # English meaning
            })
            counts[mapped_pos] += 1

    # Write with ensure_ascii=False to preserve Yoruba characters
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    print("\n[OK] Extracted %d entries -> %s" % (len(results), output_path))
    print("\nBreakdown by POS:")
    for pos, n in sorted(counts.items(), key=lambda x: -x[1]):
        print("   %-15s %d" % (pos, n))
    print("\nSkipped:")
    for reason, n in sorted(skipped.items(), key=lambda x: -x[1]):
        print("   %-25s %d" % (reason, n))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python generate_lexicon.py <input.jsonl> <output.json>")
        sys.exit(1)
    process(sys.argv[1], sys.argv[2])
