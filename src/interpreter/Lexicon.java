package interpreter;

public class Lexicon {
    private final Map<String, Token> entries  = new HashMap<>();
    private final Map<String, String> fallback = new HashMap<>(); // base → canonical

    public void add(String word, TokenType pos, String phonetic) {
        entries.put(word.toLowerCase(), new Token(word, pos, phonetic));
    }

    // Maps undiacritized "omo" → canonical "ọmọ" so lookup still works
    public void addFallback(String base, String canonical) {
        fallback.put(base.toLowerCase(), canonical.toLowerCase());
    }

    public Optional<Token> lookup(String word) {
        String key = word.toLowerCase();
        // Direct hit first
        Token t = entries.get(key);
        if (t != null) return Optional.of(t);
        // Try fallback (undiacritized input)
        String canon = fallback.get(key);
        if (canon != null) return Optional.ofNullable(entries.get(canon));
        return Optional.empty();
    }
}