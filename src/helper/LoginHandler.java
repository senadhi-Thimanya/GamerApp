package helper;

import entity.Admin;
import entity.Participant;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LoginHandler {
    private static final String PARTICIPANTS_FILE = "participants.csv";
    private static final String ADMINS_FILE = "admins.csv";
    private static final Scanner sc = new Scanner(System.in);

    /**
     * Registers a new gamer by collecting their basic information and conducting the survey
     */
    public static boolean gamerRegister() {
        System.out.println("\n=== New Gamer Registration ===\n");

        try {
            // Generate new ID using CSVDataHandler
            String newId = null;
            try {
                newId = CSVDataHandler.generateNewParticipantId(PARTICIPANTS_FILE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Your assigned ID: " + newId);

            // Get name
            String name = getName();

            // Get and validate email
            String email = getValidEmail();

            System.out.println("\nRegistration information collected!");
            System.out.println("ID: " + newId);
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("\nNow, please complete the survey to finish your registration.\n");

            // Conduct survey for new user
            Participant newParticipant = SurveyHandler.conductSurveyForNewUser(newId, name, email);

            if (newParticipant != null) {
                // Add the new participant to CSV using CSVDataHandler
                CSVDataHandler.addParticipant(newParticipant, PARTICIPANTS_FILE);
                System.out.println("\n=== Registration Complete! ===");
                System.out.println("Welcome to the Gaming Club, " + name + "!");
                System.out.println("Your ID is: " + newId);
                System.out.println("Please remember this ID for future logins.\n");
                return true;
            } else {
                System.out.println("Registration failed. Please try again.");
                return false;
            }

        } catch (IOException e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the user's name with validation
     */
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

            return name;
        }
    }

    /**
     * Gets and validates email in the format name@university.edu
     */
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
        if(userExists(id)!=null) return true;
        else return false;
    }

    public static boolean adminLogin(String id) {
        if(adminExists(id)!=null) return true;
        else return false;
    }

    /**
     * Loads a participant from the participants.csv file by ID
     * @param id The participant ID to search for
     * @return Participant object if found, null otherwise
     */
    public static Participant userExists(String id) {
        try {
            Participant participant = CSVDataHandler.findParticipantById(id, PARTICIPANTS_FILE);
            if (participant == null) {
                System.out.println("Participant ID '" + id + "' not found.");
            }
            return participant;
        } catch (java.io.IOException e) {
            System.err.println("Error reading participants file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Admin adminExists(String id) {
        try {
            Admin admin = CSVDataHandler.findAdminById(id, ADMINS_FILE);
            if (admin == null) {
                System.out.println("Admin ID '" + id + "' not found.");
            }
            return admin;
        } catch (java.io.IOException e) {
            System.err.println("Error reading participants file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}