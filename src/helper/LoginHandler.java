package helper;

import entity.Admin;
import entity.Participant;
import exception.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LoginHandler {
    private static final String PARTICIPANTS_FILE = "participants.csv";
    private static final String ADMINS_FILE = "admins.csv";
    private static final Scanner sc = new Scanner(System.in);
    private static String newlyRegisteredUserId = null;

    public static boolean gamerRegister() {
        System.out.println("┏┓╻┏━╸╻ ╻   ┏━╸┏━┓┏┳┓┏━╸┏━┓   ┏━┓┏━╸┏━╸╻┏━┓╺┳╸┏━┓┏━┓╺┳╸╻┏━┓┏┓╻\n" +
                "┃┗┫┣╸ ┃╻┃   ┃╺┓┣━┫┃┃┃┣╸ ┣┳┛   ┣┳┛┣╸ ┃╺┓┃┗━┓ ┃ ┣┳┛┣━┫ ┃ ┃┃ ┃┃┗┫\n" +
                "╹ ╹┗━╸┗┻┛   ┗━┛╹ ╹╹ ╹┗━╸╹┗╸   ╹┗╸┗━╸┗━┛╹┗━┛ ╹ ╹┗╸╹ ╹ ╹ ╹┗━┛╹ ╹");

        try {
            newlyRegisteredUserId = CSVDataHandler.generateNewParticipantId(PARTICIPANTS_FILE);

            System.out.println("Your assigned ID: " + newlyRegisteredUserId);

            String name = getName();
            String email = getValidEmail();

            System.out.println("\nRegistration information collected!");
            System.out.println("ID: " + newlyRegisteredUserId);
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("\nNow, please complete the survey to finish your registration.\n");

            Participant newParticipant = SurveyHandler.conductSurveyForNewUser(newlyRegisteredUserId, name, email);

            if (newParticipant != null) {
                CSVDataHandler.addParticipant(newParticipant, PARTICIPANTS_FILE);
                System.out.println("\n=== Registration Complete! ===");
                System.out.println("Welcome to the Gaming Club, " + name + "!");
                System.out.println("Your ID is: " + newlyRegisteredUserId);
                System.out.println("Please remember this ID for future logins.\n");
                return true;
            } else {
                System.out.println("Registration failed. Please try again.");
                return false;
            }

        } catch (FileOperationException e) {
            System.err.println("Error during registration: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            return false;
        }
    }

    private static String getName() {
        while (true) {
            System.out.print("Enter your full name: ");
            String name = sc.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please try again.");
                continue;
            }

            if (name.length() < 2) {
                System.out.println("Name must be at least 2 characters long.");
                continue;
            }

            if (!name.matches("^[\\w\\s-]+$")) {
                System.out.println("Name can only contain letters, spaces, and underscores.");
                continue;
            }

            return name;
        }
    }

    private static String getValidEmail() {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.edu$");

        while (true) {
            System.out.print("Enter your university email (format: name@university.edu): ");
            String email = sc.nextLine().trim();

            if (email.isEmpty()) {
                System.out.println("Email cannot be empty. Please try again.");
                continue;
            }

            if (emailPattern.matcher(email).matches()) {
                return email;
            } else {
                System.out.println("Invalid email format. Please use the format: name@university.edu");
                System.out.println("Example: john.doe@myuniversity.edu");
            }
        }
    }

    public static boolean gamerLogin(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.out.println("ID cannot be empty.");
            return false;
        }

        try {
            userExists(id);
            return true;
        } catch (ParticipantNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (FileOperationException e) {
            System.err.println("Error accessing participants file: " + e.getMessage());
            return false;
        }
    }

    public static boolean adminLogin(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.out.println("ID cannot be empty.");
            return false;
        }

        try {
            adminExists(id);
            return true;
        } catch (AdminNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (FileOperationException e) {
            System.err.println("Error accessing admins file: " + e.getMessage());
            return false;
        }
    }

    public static Participant userExists(String id) throws ParticipantNotFoundException, FileOperationException {
        return CSVDataHandler.findParticipantById(id, PARTICIPANTS_FILE);
    }

    public static Admin adminExists(String id) throws AdminNotFoundException, FileOperationException {
        return CSVDataHandler.findAdminById(id, ADMINS_FILE);
    }

    public static String getNewlyRegisteredUserId() {
        return newlyRegisteredUserId;
    }
}