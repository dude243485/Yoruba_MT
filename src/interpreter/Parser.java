import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;
    private final List<String> syntaxErrors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Entry point — S → NP VP
    public boolean parse() {
        pos = 0;
        syntaxErrors.clear();

        if (tokens.isEmpty()) {
            syntaxErrors.add("SYNTAX ERROR: Empty input.");
            return false;
        }

        // Skip unknown tokens (already flagged as lexical errors)
        if (currentIs(TokenType.UNKNOWN)) {
            pos++;
        }

        boolean valid = parseS();
        if (pos < tokens.size()) {
            syntaxErrors.add("SYNTAX ERROR: Unexpected token '" +
                    tokens.get(pos).getValue() + "' at position " + (pos + 1));
            return false;
        }
        return valid;
    }

    // S → NP VP
    private boolean parseS() {
        int savedPos = pos;
        if (parseNP() && parseVP()) return true;
        pos = savedPos;
        syntaxErrors.add("SYNTAX ERROR: Expected [Subject + Verb phrase]. " +
                "Got: " + describeRemaining());
        return false;
    }

    // NP → PRONOUN | NOUN (ADJECTIVE)? (DETERMINER)?
    // Abiola et al. R1–R6, R16–R20
    private boolean parseNP() {
        if (match(TokenType.PRONOUN)) return true;
        if (match(TokenType.NOUN)) {
            match(TokenType.ADJECTIVE);   // optional
            match(TokenType.ADJECTIVE);   // double adj — R5, R6
            match(TokenType.DETERMINER);  // optional
            return true;
        }
        return false;
    }

    // VP → VERB (NP)? (PP)?
    // Ayoade & Eludiora: VP → V | V NP | V NP PP
    private boolean parseVP() {
        if (!match(TokenType.VERB)) return false;
        int savedPos = pos;
        if (!parseNP()) pos = savedPos;  // NP is optional
        int savedPos2 = pos;
        if (!parsePP()) pos = savedPos2; // PP is optional
        return true;
    }

    // PP → PREPOSITION NP
    private boolean parsePP() {
        int savedPos = pos;
        if (match(TokenType.PREPOSITION) && parseNP()) return true;
        pos = savedPos;
        return false;
    }

    private boolean match(TokenType type) {
        if (pos < tokens.size() && tokens.get(pos).getType() == type) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean currentIs(TokenType type) {
        return pos < tokens.size() && tokens.get(pos).getType() == type;
    }

    private String describeRemaining() {
        if (pos >= tokens.size()) return "end of input";
        return "'" + tokens.get(pos).getValue() + "' (" + tokens.get(pos).getType() + ")";
    }

    public List<String> getSyntaxErrors() { return syntaxErrors; }
}