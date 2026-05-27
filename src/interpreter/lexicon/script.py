import json
import sys
from collections import defaultdict

# Maps Wiktionary POS tags → your TokenType enum names
POS_MAP = {
    "verb":     "VERB",
    "noun":     "NOUN",
    "pron":     "PRONOUN",
    "prep":     "PREPOSITION",
    "adj":      "ADJECTIVE",
    "adv":      "ADVERB",
    "conj":     "CONJUNCTION",
    "det":      "DETERMINER",
    "particle": "PARTICLE",
    "num":      "NUMERAL",
}

# These carry no lexical value for the interpreter
SKIP_POS = {"character", "intj", "affix", "suffix", "prefix", "punct"}


def get_canonical(entry):
    """
    Gets the tone-marked canonical form.
    Priority: forms[canonical tag] > head_templates expansion > word field.
    The word field (e.g. 'fa') has no tone marks — always prefer forms/head.
    """
    for form in entry.get("forms", []):
        if "canonical" in form.get("tags", []):
            f = form.get("form", "").strip()
            if f and not any(c in f for c in [" ", "\n"]):
                return f

    templates = entry.get("head_templates", [])
    if templates:
        exp = templates[0].get("expansion", "").strip()
        if exp:
            return exp

    return entry.get("word", "").strip()


def get_ipa(entry):
    """Gets the first IPA pronunciation. Strips the slashes."""
    for sound in entry.get("sounds", []):
        ipa = sound.get("ipa", "")
        if ipa:
            return ipa.strip("/").strip()
    return ""


def get_gloss(entry):
    """Gets the first clean gloss from the first sense."""
    senses = entry.get("senses", [])
    if not senses:
        return ""
    glosses = senses[0].get("glosses", [])
    return glosses[0].strip() if glosses else ""


def simple_syllabify(word):
    """
    DRSA-lite syllabifier for the canonical Yoruba form.
    Handles V, CV, Vn, CVn syllable structures (Akinwonmi 2024).
    Produces hyphen-separated syllables e.g. ọkùnrin → ọ-kùn-rin
    """
    VOWELS  = set("aeiouáàâéèêíìîóòôúùûẹọẹ́ẹ̀ọ́ọ̀")
    NASALS  = {'n', 'm'}
    
    syllables = []
    i = 0
    w = list(word)

    while i < len(w):
        c = w[i]

        if c in VOWELS:
            # Check for Vn (nasal vowel)
            if i + 1 < len(w) and w[i + 1] in NASALS:
                # Confirm the nasal isn't followed by a vowel (would be CV)
                if i + 2 >= len(w) or w[i + 2] not in VOWELS:
                    syllables.append(w[i] + w[i + 1])  # Vn
                    i += 2
                    continue
            syllables.append(c)  # V
            i += 1

        elif c in NASALS and (i + 1 >= len(w) or w[i + 1] not in VOWELS):
            # Syllabic nasal N (stands alone)
            syllables.append(c)
            i += 1

        elif c.isalpha():
            # Consonant — look ahead for vowel to form CV or CVn
            if i + 1 < len(w) and w[i + 1] in VOWELS:
                cv = w[i] + w[i + 1]
                # CVn?
                if i + 2 < len(w) and w[i + 2] in NASALS:
                    if i + 3 >= len(w) or w[i + 3] not in VOWELS:
                        syllables.append(cv + w[i + 2])  # CVn
                        i += 3
                        continue
                syllables.append(cv)  # CV
                i += 2
            else:
                # Lone consonant — append to previous or keep alone
                syllables.append(c)
                i += 1
        else:
            # Diacritic combining character or punctuation — skip
            if syllables:
                syllables[-1] += c
            i += 1

    return "-".join(syllables)


def process(input_path, output_path):
    results = []
    seen    = set()          # deduplicate by (canonical_form, pos)
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

            # Deduplicate — one entry per (word, POS) pair
            key = (canonical.lower(), mapped_pos)
            if key in seen:
                skipped["duplicate"] += 1
                continue
            seen.add(key)

            ipa        = get_ipa(entry)
            gloss      = get_gloss(entry)
            syllables  = simple_syllabify(canonical)

            results.append({
                "word":      canonical,       # tone-marked form: fà, fá, ọmọ
                "base":      entry["word"],   # undiacritized:    fa,  fa, omo
                "pos":       mapped_pos,      # VERB, NOUN, etc.
                "ipa":       ipa,             # /fà/
                "syllables": syllables,       # fà  (or ọ-mọ for two-syllable words)
                "gloss":     gloss            # "to pull"
            })
            counts[mapped_pos] += 1

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    print(f"\n✓  Extracted {len(results)} entries → {output_path}")
    print("\nBreakdown by POS:")
    for pos, n in sorted(counts.items(), key=lambda x: -x[1]):
        print(f"   {pos:<15} {n}")
    print("\nSkipped:")
    for reason, n in sorted(skipped.items(), key=lambda x: -x[1]):
        print(f"   {reason:<25} {n}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python script.py <input.jsonl> <output.json>")
        sys.exit(1)
    process(sys.argv[1], sys.argv[2])