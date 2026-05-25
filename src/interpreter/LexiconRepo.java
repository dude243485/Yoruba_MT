package interpreter;

import java.util.HashMap ;
import java.util.Map ;

public class LexiconRepo {
    //simulating a database cache map loaded from an external POS database

    private final Map<String, TokenType> dbMock = new HashMap<>();

    public LexiconRepo () {
        seedDatabase();
    }

    /**
     * Look up a word from the external database resource.
     * @param word the input word string with diacritics
     * @return the identified TokenType or null if Out-of-vocabulary (OOV)
     */
    public TokenType lookupWord(String word) {
        return dbMock.get(word);
    }

    private void seedDatabase() {
        //pronouns
        dbMock.put("mo", TokenType.PRONOUN);
        dbMock.put("o", TokenType.PRONOUN);
        dbMock.put("a", TokenType.PRONOUN);
        dbMock.put("won", TokenType.PRONOUN);

        //nouns
        dbMock.put("Olu", TokenType.NOUN);
        dbMock.put("Ade", TokenType.NOUN);
        dbMock.put("isu", TokenType.NOUN);
        dbMock.put("ile", TokenType.NOUN);
        dbMock.put("eja", TokenType.NOUN);

        //verbs
        dbMock.put("je", TokenType.VERB);
        dbMock.put("ra", TokenType.VERB);
        dbMock.put("lo", TokenType.VERB);
        dbMock.put("wa", TokenType.VERB);

        //Adjectives
        dbMock.put("pupa", TokenType.ADJECTIVE);
        dbMock.put("dudu", TokenType.ADJECTIVE);
        dbMock.put("rere", TokenType.ADJECTIVE);

    }
}