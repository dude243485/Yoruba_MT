package interpreter;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private final List<String> syntaxErrors = new ArrayList<>();

    public Parser(Lexer  lexer){
        this.lexer = lexer;

        //grab the first token
        this.currentToken = lexer.nextToken();
    }

    private void consume(TokenType expectedType) {
        if (currentToken.getType() == expectedType){
            currentToken = lexer.nextToken();
        } else {
            syntaxErrors.add(String.format("Syntax Error: Expected components of type [%s] but hit unexpected lexeme '%s'.",
                    expectedType, currentToken.getLexeme()));
        }
    }

    /**
     * Entry point for grammar parsing*/
    public void parseSentence() {
        //don't parse if word is not known
        if (currentToken.getType() == TokenType.UNKNOWN){
            syntaxErrors.add("Syntax Parsing aborted due to preceding OOV token errors");
            return;
        }

        parseNounPhrase();
        parseVerbPhrase();

        if (currentToken.getType() != TokenType.EOF) {
            syntaxErrors.add(String.format("Syntax Error: Ending dangling tokens discovered after structural sentence completion: '%s'.",
                    currentToken.getLexeme()));
        }
    }

    //NP -> Pronoun | Noun (Adjective) ?
    private void parseNounPhrase() {
        if (currentToken.getType() == TokenType.PRONOUN){
            consume(TokenType.PRONOUN);
        } else if (currentToken.getType() == TokenType.NOUN){
            consume(TokenType.NOUN);

            if (currentToken.getType() == TokenType.ADJECTIVE){
                consume(TokenType.ADJECTIVE);
            }
        } else {
            syntaxErrors.add(String.format("Syntax Error: Structural Noun Phrase cannot begin with '%s'",
                    currentToken.getLexeme()));
            currentToken = lexer.nextToken();
        }
    }


    //VP -> Verb (NP)
    private void parseVerbPhrase() {

    }


}