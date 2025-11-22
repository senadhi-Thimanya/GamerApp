import java.util.Scanner;

public class AppInitializer {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(" _____ _____  ___  ___  ______  ___  ___ _____ _____ \n" +
                "|_   _|  ___|/ _ \\ |  \\/  ||  \\/  | / _ \\_   _|  ___|\n" +
                "  | | | |__ / /_\\ \\| .  . || .  . |/ /_\\ \\| | | |__  \n" +
                "  | | |  __||  _  || |\\/| || |\\/| ||  _  || | |  __| \n" +
                "  | | | |___| | | || |  | || |  | || | | || | | |___ \n" +
                "  \\_/ \\____/\\_| |_/\\_|  |_/\\_|  |_/\\_| |_/\\_/ \\____/ \n" +
                "                                                     \n" +
                "                                                     ");
        System.out.println("Welcome to TeamMate - IIT E-sports Club Gaming Platform!\n");

        System.out.println("Are you a 1. Gamer or an 2. Admin? (Enter 1 or 2): ");
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
