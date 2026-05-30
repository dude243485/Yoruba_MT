//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Yoruba Interpreter");
        System.out.println("==================");
        System.out.println("Type a Yoruba sentence to interpret it, or type 'exit' to quit.\n");

        while (true) {
            System.out.print("Enter Yoruba sentence: ");

            if (!scanner.hasNextLine()) {
                // EOF reached (e.g. Ctrl+Z on Windows)
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("Please enter a sentence.\n");
                continue;
            }

            // TODO: pass `input` to the interpreter
            System.out.println("You entered: " + input + "\n");
        }

        scanner.close();
    }
}