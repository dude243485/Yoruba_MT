import java.util.*;

public class Lexer {
    private final Lexicon lexicon;
    private final List<String> lexicalErrors = new ArrayList<>();

    public Lexer(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public List<Token> tokenize(String[] rawTokens) {
        lexicalErrors.clear();
        List<Token> tokens = new ArrayList<>();

        for (int i = 0; i < rawTokens.length; i++) {
            String raw = rawTokens[i];
            Optional<Token> found = lexicon.lookup(raw);

            if (found.isPresent()) {
                tokens.add(found.get());
            } else {
                // ErrorReporter feeds from this — Akinwonmi 2024
                tokens.add(new Token(raw, TokenType.UNKNOWN, "?"));
                lexicalErrors.add(
                        "LEXICAL ERROR at position " + (i + 1) +
                                ": unrecognized token '" + raw + "'"
                );
            }
        }
        return tokens;
    }

    public List<String> getLexicalErrors() { return lexicalErrors; }
}