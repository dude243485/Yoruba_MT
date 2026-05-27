package interpreter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Natural Language Interpreter        ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("Select language:  [1] Yoruba   [2] Igbo");
        int choice = Integer.parseInt(sc.nextLine().trim());

        // 1. Language Selector → activates correct lexicon
        Lexicon lexicon = (choice == 1) ? new YorubaLexicon() : new IgboLexicon();
        String lang = (choice == 1) ? "Yoruba" : "Igbo";

        System.out.println("Enter a " + lang + " sentence:");
        String input = sc.nextLine();

        // 2. Preprocessor
        Preprocessor pre = new Preprocessor();
        String[] rawTokens = pre.tokenizeRaw(input);

        // 3 + 4. Lexer → List<Token>
        Lexer lexer = new Lexer(lexicon);
        List<Token> tokens = lexer.tokenize(rawTokens);

        // 5. Display lexical analysis table
        System.out.println("\n┌─ Lexical Analysis ───────────────────────────────────┐");
        System.out.printf("│ %-15s %-15s %-20s │%n", "TOKEN", "POS", "PHONETIC");
        System.out.println("├───────────────────────────────────────────────────────┤");
        for (Token t : tokens) {
            System.out.printf("│ %-15s %-15s %-20s │%n",
                    t.getValue(), t.getType(), t.getPhonetic());
        }
        System.out.println("└───────────────────────────────────────────────────────┘");

        // 6. Phonetic reading
        PhoneticEngine phonetic = new PhoneticEngine();
        System.out.println("\n┌─ Phonetic Reading ────────────────────────────────────┐");
        for (Token t : tokens) {
            System.out.printf("│  %-12s →  %s%n", t.getValue(), phonetic.getPhoneticReading(t));
        }
        System.out.println("└───────────────────────────────────────────────────────┘");

        // 7. Syntax validation
        Parser parser = new Parser(tokens);
        boolean valid = parser.parse();
        System.out.println("\n┌─ Syntax Validation ───────────────────────────────────┐");
        System.out.println(valid
                ? "│  ✓  Valid sentence structure detected.                 │"
                : "│  ✗  Invalid sentence structure.                        │");
        System.out.println("└───────────────────────────────────────────────────────┘");

        // 8. Error Report
        ErrorReporter reporter = new ErrorReporter();
        lexer.getLexicalErrors().forEach(reporter::addError);
        parser.getSyntaxErrors().forEach(reporter::addError);
        reporter.report();
    }
}