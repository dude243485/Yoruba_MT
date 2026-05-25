package interpreter;


import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String[] sourceWords;
    private final LexiconRepo dbRepo;
    private int cursor = 0;
    private final List<String> lexicalErrors = new ArrayList<>() ;


    //constructor
    public Lexer(String input, LexiconRepo repo) {
        this.dbRepo = repo;

        //cleaning the white space
        if (input == null || input.trim().isEmpty()) {
            this.sourceWords = new String[0];

        }else {
            this.sourceWords = input.trim().split("\\s+");
        }
    }

    /**
     * Generates the next sequential token in the text stream
     */
    public Token nextToken() {
        if (cursor >= sourceWords.length) {
            return new Token(TokenType.EOF, "");
        }

        String currentWord = sourceWords[cursor++];
        TokenType resolvedType = dbRepo.lookupWord(currentWord);

        //out-of-vocabulary verification check
        if (resolvedType == null) {
            lexicalErrors.add(String.format("Lexical Error [OOV]: The word '%s' is not registered in the database.", currentWord));
            return new Token(TokenType.UNKNOWN, currentWord);
        }

        return new Token(resolvedType, currentWord);
    }

    public List<String> getLexicalErrors() {
        return lexicalErrors;
    }

    public boolean hasErrors() {
        return !lexicalErrors.isEmpty();
    }

}