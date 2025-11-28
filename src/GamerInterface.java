import entity.Event;
import entity.Team;
import helper.CSVDataHandler;
import helper.LoginHandler;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static helper.SurveyHandler.conductSurvey;

public class GamerInterface {
    private static final Scanner sc = new Scanner(System.in);
    public static String loggedInUserId;

    public static void launch() {
        System.out.print("\nAre you 1. Already a member or 2. A new user? (Enter 1 or 2) : ");
        String choice = sc.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("\nWhat is your id? ");
            String id = sc.nextLine().trim();
            if (LoginHandler.gamerLogin(id)) {
                System.out.println("\n\tLogin successful. Welcome back!");
                loggedInUserId = id;
            }
            else {
                System.out.println("\n\tLogin failed. Exiting application.");
                return;
            }
        }
        else if (choice.equals("2")) LoginHandler.gamerRegister(); // get the personal details first
        else {
            System.out.println("\n\tInvalid choice. Exiting application.");
            return;
        }

        gamerMenu();
        while(true) {
            System.out.print("\nDo you want to perform another action? 1. yes 2. no : ");
            String again = sc.nextLine().trim().toLowerCase();
            if (again.equals("1")) {
                gamerMenu();
            } else {
                System.out.println("\n\tExiting Gamer Interface. Goodbye!");
                break;
            }

        }

    }

    public static void gamerMenu() {
        int option = getUserOptions();
        switch (option){
            case 1:
                conductSurvey(loggedInUserId);
                break;
            case 2:
                viewTeamAssignment();
                break;
            case 3:
                viewPersonalDetails();
                break;
            case 4:
                updatePersonalDetails();
                break;
            case 5:
                viewEvents();
                break;
            default:
                System.out.println("\n\tInvalid option selected.");
        }
    }

    public static int getUserOptions() {
        System.out.println("┏━╸┏━┓┏┳┓┏━╸┏━┓   ┏━┓┏━┓╺┳╸╻┏━┓┏┓╻┏━┓ \n" +
                "┃╺┓┣━┫┃┃┃┣╸ ┣┳┛   ┃ ┃┣━┛ ┃ ┃┃ ┃┃┗┫┗━┓╹\n" +
                "┗━┛╹ ╹╹ ╹┗━╸╹┗╸   ┗━┛╹   ╹ ╹┗━┛╹ ╹┗━┛╹");
        System.out.println("1. Take the Survey"); // Will update details if already a user
        System.out.println("2. View Team Assignment"); // per event. So name of the event needed
        System.out.println("3. View personal details");
        System.out.println("4. Update personal details");
        System.out.println("5. View all Events");
        System.out.print("\tEnter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline
        return choice;
    }

    public static void viewTeamAssignment() {
        System.out.println("╻ ╻╻┏━╸╻ ╻   ╺┳╸┏━╸┏━┓┏┳┓   ┏━┓┏━┓┏━┓╻┏━╸┏┓╻┏┳┓┏━╸┏┓╻╺┳╸\n" +
                "┃┏┛┃┣╸ ┃╻┃    ┃ ┣╸ ┣━┫┃┃┃   ┣━┫┗━┓┗━┓┃┃╺┓┃┗┫┃┃┃┣╸ ┃┗┫ ┃ \n" +
                "┗┛ ╹┗━╸┗┻┛    ╹ ┗━╸╹ ╹╹ ╹   ╹ ╹┗━┛┗━┛╹┗━┛╹ ╹╹ ╹┗━╸╹ ╹ ╹ ");

        // Ask for event name
        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        // Ask for participant ID
        //System.out.print("Enter your Participant ID: ");
        //String participantId = sc.nextLine().trim();

        // Construct the file path
        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";

        // Check if file exists
        java.io.File file = new java.io.File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("\tError: No team formation found for event '" + eventName + "'");
            System.out.println("\tPlease check the event name and try again.");
            return;
        }

        try {
            // Load teams from the event file
            List<Team> teams = CSVDataHandler.loadTeams(filePath);

            // Search for the participant in the teams
            boolean found = false;
            for (Team team : teams) {
                for (entity.Participant p : team.getMembers()) {
                    if (p.getId().equals(loggedInUserId)) {
                        found = true;
                        System.out.println("\n=== Your Team Assignment ===");
                        System.out.println("Event: " + eventName);
                        System.out.println("Team ID: " + team.getTeamId());
                        System.out.println("Team Average Skill: " + String.format("%.2f", team.getAverageSkill()));
                        System.out.println("\n--- Your Details ---");
                        System.out.println(p.toString());
                        System.out.println("\n--- Your Teammates ---");
                        for (entity.Participant teammate : team.getMembers()) {
                            if (!teammate.getId().equals(loggedInUserId)) {
                                System.out.println("  " + teammate.toString());
                            }
                        }
                        System.out.println("\n=========================");
                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
                System.out.println("\tParticipant ID '" + loggedInUserId + "' not found in event '" + eventName + "'");
                System.out.println("\tPlease verify your ID and ensure you're registered for this event.");
            }

        } catch (IOException e) {
            System.err.println("\tError reading team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewEvents(){
        System.out.println("┏━┓╻  ╻     ┏━╸╻ ╻┏━╸┏┓╻╺┳╸┏━┓\n" +
                "┣━┫┃  ┃     ┣╸ ┃┏┛┣╸ ┃┗┫ ┃ ┗━┓\n" +
                "╹ ╹┗━╸┗━╸   ┗━╸┗┛ ┗━╸╹ ╹ ╹ ┗━┛");
        try {
            List<Event> events = CSVDataHandler.loadEvents("TeamFormations/");
            if (events == null || events.isEmpty()) {
                System.out.println("\tNo events found.");
                return;
            }
            for (int i = 0; i < events.size(); i++) {
                System.out.println((i + 1) + ". " + events.get(i));
            }
        } catch (IOException e) {
            System.err.println("\tError loading events: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewPersonalDetails() {
        System.out.println("╻ ╻╻┏━╸╻ ╻   ┏━┓┏━╸┏━┓┏━┓┏━┓┏┓╻┏━┓╻     ╺┳┓┏━╸╺┳╸┏━┓╻╻  ┏━┓\n" +
                "┃┏┛┃┣╸ ┃╻┃   ┣━┛┣╸ ┣┳┛┗━┓┃ ┃┃┗┫┣━┫┃      ┃┃┣╸  ┃ ┣━┫┃┃  ┗━┓\n" +
                "┗┛ ╹┗━╸┗┻┛   ╹  ┗━╸╹┗╸┗━┛┗━┛╹ ╹╹ ╹┗━╸   ╺┻┛┗━╸ ╹ ╹ ╹╹┗━╸┗━┛");
        //System.out.print("Enter your Participant ID: ");
        //String id = sc.nextLine().trim();

        try {
            entity.Participant participant = CSVDataHandler.getParticipantDetails(loggedInUserId, "participants.csv");

            if (participant == null) {
                System.out.println("Participant ID '" + loggedInUserId + "' not found.");
                return;
            }

            System.out.println("\n--- Your Details ---");
            System.out.println("ID: " + participant.getId() + " (non-editable)");
            System.out.println("Name: " + participant.getName());
            System.out.println("Email: " + participant.getEmail());
            System.out.println("Preferred Game: " + participant.getPreferredGame());
            System.out.println("Skill Level: " + participant.getSkillLevel() + "/10");
            System.out.println("Preferred Role: " + participant.getPreferredRole());
            System.out.println("Personality Score: " + participant.getPersonalityScore());
            System.out.println("Personality Type: " + participant.getPersonalityType());
            System.out.println("====================\n");

        } catch (IOException e) {
            System.err.println("Error loading participant details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updatePersonalDetails() {
        System.out.println("╻ ╻┏━┓╺┳┓┏━┓╺┳╸┏━╸   ┏━┓┏━╸┏━┓┏━┓┏━┓┏┓╻┏━┓╻     ╺┳┓┏━╸╺┳╸┏━┓╻╻  ┏━┓\n" +
                "┃ ┃┣━┛ ┃┃┣━┫ ┃ ┣╸    ┣━┛┣╸ ┣┳┛┗━┓┃ ┃┃┗┫┣━┫┃      ┃┃┣╸  ┃ ┣━┫┃┃  ┗━┓\n" +
                "┗━┛╹  ╺┻┛╹ ╹ ╹ ┗━╸   ╹  ┗━╸╹┗╸┗━┛┗━┛╹ ╹╹ ╹┗━╸   ╺┻┛┗━╸ ╹ ╹ ╹╹┗━╸┗━┛");
        //System.out.print("Enter your Participant ID: ");
        //String id = sc.nextLine().trim();

        try {
            entity.Participant participant = CSVDataHandler.getParticipantDetails(loggedInUserId, "participants.csv");

            if (participant == null) {
                System.out.println("\tParticipant ID '" + loggedInUserId + "' not found.");
                return;
            }

            System.out.println("\nCurrent Details:");
            System.out.println("ID: " + participant.getId() + " (non-editable)");
            System.out.println("Name: " + participant.getName());
            System.out.println("Email: " + participant.getEmail());

            System.out.println("\n--- Update Information ---");
            System.out.println("Press Enter to keep current value");

            // Update name
            System.out.print("Enter new name (current: " + participant.getName() + "): ");
            String newName = sc.nextLine().trim();
            if (newName.isEmpty()) {
                newName = participant.getName();
            }

            // Update email with validation
            String newEmail;
            while (true) {
                System.out.print("Enter new email (current: " + participant.getEmail() + "): ");
                newEmail = sc.nextLine().trim();

                if (newEmail.isEmpty()) {
                    newEmail = participant.getEmail();
                    break;
                }

                // Validate email format: name@university.edu
                if (newEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.edu$")) {
                    break;
                } else {
                    System.out.println("\tInvalid email format. Please use format: name@university.edu");
                }
            }

            // Confirm changes
            System.out.println("\nNew Details:");
            System.out.println("Name: " + newName);
            System.out.println("Email: " + newEmail);
            System.out.print("\nConfirm changes? (yes/no): ");
            String confirm = sc.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                CSVDataHandler.updateParticipantNameEmail(loggedInUserId, newName, newEmail, "participants.csv");
                System.out.println("\nPersonal details updated successfully!");
            } else {
                System.out.println("\nUpdate cancelled.");
            }
            System.out.println("===============================\n");

        } catch (IOException e) {
            System.err.println("\tError updating participant details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
