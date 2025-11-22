import helper.LoginHandler;

import java.util.Scanner;

public class GamerInterface {
    public static void launch() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Are you 1. Already a member or 2. A new user? (Enter 1 or 2) : ");
        String choice = sc.nextLine().trim();
        if (choice.equals("1")) {
            LoginHandler.Login();
        } else if (choice.equals("2")) {
            LoginHandler.register();
        } else {
            System.out.println("Invalid choice. Exiting application.");
        }
    }
}
