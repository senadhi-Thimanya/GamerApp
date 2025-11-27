import java.util.Scanner;

public class AppInitializer {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n\n╺┳╸┏━╸┏━┓┏┳┓┏┳┓┏━┓╺┳╸┏━╸\n" +
                " ┃ ┣╸ ┣━┫┃┃┃┃┃┃┣━┫ ┃ ┣╸ \n" +
                " ╹ ┗━╸╹ ╹╹ ╹╹ ╹╹ ╹ ╹ ┗━╸");
        System.out.println("╻ ╻┏━╸╻  ┏━╸┏━┓┏┳┓┏━╸   ╺┳╸┏━┓   ╻╻╺┳╸   ┏━╸   ┏━┓┏━┓┏━┓┏━┓╺┳╸┏━┓   ┏━┓╻  ┏━┓╺┳╸┏━╸┏━┓┏━┓┏┳┓╻\n" +
                "┃╻┃┣╸ ┃  ┃  ┃ ┃┃┃┃┣╸     ┃ ┃ ┃   ┃┃ ┃    ┣╸ ╺━╸┗━┓┣━┛┃ ┃┣┳┛ ┃ ┗━┓   ┣━┛┃  ┣━┫ ┃ ┣╸ ┃ ┃┣┳┛┃┃┃╹\n" +
                "┗┻┛┗━╸┗━╸┗━╸┗━┛╹ ╹┗━╸    ╹ ┗━┛   ╹╹ ╹    ┗━╸   ┗━┛╹  ┗━┛╹┗╸ ╹ ┗━┛   ╹  ┗━╸╹ ╹ ╹ ╹  ┗━┛╹┗╸╹ ╹╹\n");
        System.out.print("Are you a 1. Gamer or an 2. Admin? (Enter 1 or 2): ");
        String roleChoice = sc.nextLine().trim();
        if (roleChoice.equals("1")) {
            GamerInterface.launch();
        } else if (roleChoice.equals("2")) {
            AdminInterface.launch();
        } else {
            System.out.println("Invalid choice. Exiting application.");
        }
    }
}
