import entity.Event;
import entity.Participant;
import entity.Team;
import helper.CSVDataHandler;
import helper.LoginHandler;
import helper.TeamBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class AdminInterface {
    private static final Scanner sc = new Scanner(System.in);

    public static void launch() {
        //Admin has to be a club member

        System.out.print("What is your ID? ");
        String id = sc.nextLine().trim();
        if (!LoginHandler.adminLogin(id)) return; //if id ! then quit

        adminMenu();
        while(true) {
            System.out.print("Do you want to perform another action? 1. yes 2. no : ");
            String again = sc.nextLine().trim().toLowerCase();
            if (again.equals("1")) {
                adminMenu();
            } else {
                System.out.println("Exiting Admin Interface. Goodbye!");
                break;
            }

        }
    }

    public static void adminMenu(){
        int option = getAdminOptions();
        switch (option){
            case 1:
                createEventTeamFormation();
                break;
            case 2:
                try {
                    viewTeamFormation();
                } catch (IOException e) {
                    System.err.println("Failed to view team formation: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case 3:
                viewAllGamers();
                break;
            case 4:
                GamerInterface.viewEvents();
                break;
            case 5:

                break;
            case 6:
                viewLeftoverParticipants();
                break;
            default:
                System.out.println("Invalid option selected.");
        }
    }

    public static int getAdminOptions() {
        System.out.println("Admin Options:");
        System.out.println("1. Create new Event Team Formation");
        System.out.println("2. View Teams");
        System.out.println("3. View All Gamers");
        System.out.println("4. View all Events");
        System.out.println("5. Delete Team Formation");
        System.out.println("6. View Leftover Participants");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline
        return choice;
    }

    private static void createEventTeamFormation() {
        // Implementation for creating a new event team formation

        //Create an event first
        System.out.println("Creating a new Event Team Formation...");
        System.out.println("Enter Event Name: ");
        String eventName = sc.nextLine();

        Event event = new Event(eventName);
        System.out.println("Event created: " + event);

        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");

            System.out.print("Enter team size: ");
            int teamSize = sc.nextInt();

            List<Team> teams = TeamBuilder.formTeams(participants, teamSize);

            teams.forEach(System.out::println);

            // need the event name as a csv name
            CSVDataHandler.saveTeams(teams, "TeamFormations/"+event.getEventName()+"_team_formation.csv");
            System.out.println("Teams saved...");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewTeamFormation() throws IOException {
        System.out.println("Viewing Team Formation...");
        System.out.println("Enter Event Name to view teams: ");
        String eventName = sc.nextLine().trim();

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Event team file not found: " + filePath);
            return;
        }

        try {
            List<Team> teams = CSVDataHandler.loadTeams(filePath);
            teams.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Error reading teams from " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewAllGamers() {
        System.out.println("\n=== All Registered Gamers ===");
        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");
            if (participants.isEmpty()) {
                System.out.println("No gamers registered yet.");
            } else {
                System.out.println("Total Gamers: " + participants.size() + "\n");
                for (int i = 0; i < participants.size(); i++) {
                    System.out.println((i + 1) + ". " + participants.get(i).toString());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteTeamFormation() {
        // Implementation for deleting team formations
    }

    private static void viewLeftoverParticipants() {
        System.out.println("\n=== Leftover Participants ===");
        if (TeamBuilder.hasLeftovers()) {
            List<Participant> leftovers = TeamBuilder.getLeftoverParticipants();
            System.out.println("The following participants are waiting to be assigned to teams:\n");
            for (int i = 0; i < leftovers.size(); i++) {
                System.out.println((i + 1) + ". " + leftovers.get(i).toString());
            }
            System.out.println("\nTotal Leftovers: " + leftovers.size());
        } else {
            System.out.println("No leftover participants. All participants have been assigned to teams.");
        }
        System.out.println("=============================\n");
    }
}
