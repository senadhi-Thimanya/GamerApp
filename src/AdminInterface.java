import entity.Event;
import entity.Participant;
import entity.Team;
import helper.CSVDataHandler;
import helper.LoginHandler;
import helper.TeamBuilder;

import java.util.List;
import java.util.Scanner;

public class AdminInterface {
    private static final Scanner sc = new Scanner(System.in);

    public static void launch() {
        //Admin has to be a club member

        System.out.println("Are you 1. Already a member or 2. A new user? (Enter 1 or 2) : ");
        String choice = sc.nextLine().trim();

        if (choice.equals("1")) LoginHandler.Login();
        else if (choice.equals("2")) LoginHandler.register();
        else System.out.println("Invalid choice. Exiting application.");

        int option = getAdminOptions();
        switch (option){
            case 1:
                createEventTeamFormation();
                break;
            case 2:
                //View Teams
                break;
            case 3:
                //View Admin personal details
                break;
            case 4:
                //View All Gamers
                break;
            case 5:
                //Update Team names
                break;
            default:
                System.out.println("Invalid option selected.");
        }
    }

    public static int getAdminOptions() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Admin Options:");
        System.out.println("1. Create new Event Team Formation");
        System.out.println("2. View Teams");
        System.out.println("3. View Admin personal details");
        System.out.println("4. View All Gamers");
        System.out.println("5. Update Team names");
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
            CSVDataHandler.saveTeams(teams, event.getEventName()+"_team_formation.csv");
            System.out.println("Teams saved...");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewTeamFormation() {
        // Implementation for viewing team formations
    }

    private static void viewAllGamers() {
        // Implementation for viewing all gamers
    }

    private static void updateTeamFormation() {
        // Implementation for updating team formations
    }

    private static void deleteTeamFormation() {
        // Implementation for deleting team formations
    }

    private static void deleteAllGamers() {
        // Implementation for deleting all gamers
    }

    private static void updateAdminDetails() {
        // Implementation for updating admin details
    }
}
