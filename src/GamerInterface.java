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

    public static void launch() {
        System.out.print("Are you 1. Already a member or 2. A new user? (Enter 1 or 2) : ");
        String choice = sc.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("What is your id? ");
            String id = sc.nextLine().trim();
            if (LoginHandler.gamerLogin(id)) System.out.println("Login successful. Welcome back!");
            else {
                System.out.println("Login failed. Exiting application.");
                return;
            }
        }
        else if (choice.equals("2")) LoginHandler.gamerRegister(); // get the personal details first
        else {
            System.out.println("Invalid choice. Exiting application.");
            return;
        }

        int option = getUserOptions();
        switch (option){
            case 1:
                conductSurvey();
                break;
            case 2:
                viewTeamAssignment();
                break;
            case 3:
                viewEvents();
                break;
            default:
                System.out.println("Invalid option selected.");
        }
    }

    public static int getUserOptions() {
        System.out.println("Gamer Options:");
        System.out.println("1. Take the Survey"); // Will update details if already a user
        System.out.println("2. View Team Assignment"); // per event. So name of the event needed
        // System.out.println("3. View personal details"); //Shouldn't be changeable
        // System.out.println("4. Update personal details");
        System.out.println("3. View all Events");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline
        return choice;
    }

    public static void viewTeamAssignment() {
        System.out.println("=== View Team Assignment ===");

        // Ask for event name
        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        // Ask for participant ID
        System.out.print("Enter your Participant ID: ");
        String participantId = sc.nextLine().trim();

        // Construct the file path
        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";

        // Check if file exists
        java.io.File file = new java.io.File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Error: No team formation found for event '" + eventName + "'");
            System.out.println("Please check the event name and try again.");
            return;
        }

        try {
            // Load teams from the event file
            List<Team> teams = CSVDataHandler.loadTeams(filePath);

            // Search for the participant in the teams
            boolean found = false;
            for (Team team : teams) {
                for (entity.Participant p : team.getMembers()) {
                    if (p.getId().equals(participantId)) {
                        found = true;
                        System.out.println("\n=== Your Team Assignment ===");
                        System.out.println("Event: " + eventName);
                        System.out.println("Team ID: " + team.getTeamId());
                        System.out.println("Team Average Skill: " + String.format("%.2f", team.getAverageSkill()));
                        System.out.println("\n--- Your Details ---");
                        System.out.println(p.toString());
                        System.out.println("\n--- Your Teammates ---");
                        for (entity.Participant teammate : team.getMembers()) {
                            if (!teammate.getId().equals(participantId)) {
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
                System.out.println("Participant ID '" + participantId + "' not found in event '" + eventName + "'");
                System.out.println("Please verify your ID and ensure you're registered for this event.");
            }

        } catch (IOException e) {
            System.err.println("Error reading team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewEvents(){
        System.out.println("=== All Events ===");
        try {
            List<Event> events = CSVDataHandler.loadEvents("TeamFormations/");
            if (events == null || events.isEmpty()) {
                System.out.println("No events found.");
                return;
            }
            for (int i = 0; i < events.size(); i++) {
                System.out.println((i + 1) + ". " + events.get(i));
            }
        } catch (IOException e) {
            System.err.println("Error loading events: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
