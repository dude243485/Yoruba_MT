package interpreter;

public class Token {
    private final TokenType type ;
    private final String lexeme;
    private final String phonetic;


    public Token(TokenType type, String lexeme, String phonetic) {
        this.type = type;
        this.lexeme = lexeme;
        this.phonetic = phonetic;
    }

    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public String getPhonetic() { return phonetic ;}


    @Override
    public String toString() {
        return String.format("<%s, \"%s\">", type, lexeme);
    }
}