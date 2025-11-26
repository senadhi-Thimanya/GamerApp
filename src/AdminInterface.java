import entity.Event;
import entity.Participant;
import entity.PersonalityType;
import entity.Team;
import helper.CSVDataHandler;
import helper.LoginHandler;
import helper.TeamBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                viewTeamStatistics();
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
        System.out.println("5. View Team Statistics");
        System.out.println("6. View Leftover Participants");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline
        return choice;
    }

    private static void createEventTeamFormation() {
        System.out.println("Creating a new Event Team Formation...");
        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("Event name cannot be empty.");
            return;
        }

        Event event = new Event(eventName);
        System.out.println("Event created: " + event);

        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");

            if (participants.isEmpty()) {
                System.out.println("No participants found. Please ensure participants have registered.");
                return;
            }

            System.out.print("Enter team size: ");
            int teamSize = sc.nextInt();
            sc.nextLine(); // Consume newline

            if (teamSize < 3) {
                System.out.println("Team size must be at least 3.");
                return;
            }

            String filePath = "TeamFormations/" + event.getEventName() + "_team_formation.csv";

            List<Team> teams = TeamBuilder.formTeams(participants, teamSize);

            if (!teams.isEmpty()) {
                teams.forEach(System.out::println);
            }

            // Save teams and leftovers together
            List<Participant> leftovers = TeamBuilder.getLeftoverParticipants();
            CSVDataHandler.saveTeamsWithLeftovers(teams, leftovers, filePath);
            System.out.println("Teams and leftovers saved to: " + filePath);

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
        System.out.println("\n=== View Leftover Participants ===");
        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("Event name cannot be empty.");
            return;
        }

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Event team file not found: " + filePath);
            return;
        }

        try {
            List<Participant> leftovers = CSVDataHandler.loadLeftovers(filePath);

            if (leftovers.isEmpty()) {
                System.out.println("No leftover participants for event '" + eventName + "'.");
                System.out.println("All participants have been assigned to teams.");
            } else {
                System.out.println("Event: " + eventName);
                System.out.println("The following participants are waiting to be assigned to teams:\n");
                for (int i = 0; i < leftovers.size(); i++) {
                    System.out.println((i + 1) + ". " + leftovers.get(i).toString());
                }
                System.out.println("\nTotal Leftovers: " + leftovers.size());
            }
        } catch (IOException e) {
            System.err.println("Error loading leftovers: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("===================================\n");
    }

    private static void viewTeamStatistics() {
        System.out.println("\n=== View Team Statistics ===");
        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("Event name cannot be empty.");
            return;
        }

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Event team file not found: " + filePath);
            return;
        }

        try {
            List<Team> teams = CSVDataHandler.loadTeams(filePath);

            if (teams.isEmpty()) {
                System.out.println("No teams found for event '" + eventName + "'.");
                return;
            }

            System.out.println("\n=== Statistics for Event: " + eventName + " ===\n");

            // Overall statistics
            int totalParticipants = teams.stream()
                    .mapToInt(Team::getSize)
                    .sum();

            double overallAvgSkill = teams.stream()
                    .mapToDouble(Team::getAverageSkill)
                    .average()
                    .orElse(0.0);

            double maxSkill = teams.stream()
                    .mapToDouble(Team::getAverageSkill)
                    .max()
                    .orElse(0.0);

            double minSkill = teams.stream()
                    .mapToDouble(Team::getAverageSkill)
                    .min()
                    .orElse(0.0);

            System.out.println("--- Overall Statistics ---");
            System.out.println("Total Teams: " + teams.size());
            System.out.println("Total Participants in Teams: " + totalParticipants);
            System.out.println("Average Team Size: " + String.format("%.1f", (double) totalParticipants / teams.size()));
            System.out.println("Overall Average Skill: " + String.format("%.2f", overallAvgSkill));
            System.out.println("Skill Range: " + String.format("%.2f", minSkill) + " - " + String.format("%.2f", maxSkill));
            System.out.println("Skill Variance: " + String.format("%.2f", maxSkill - minSkill));

            // Diversity statistics
            int goodGameDiversity = 0;
            int goodRoleDiversity = 0;
            int teamsWithLeader = 0;
            int teamsWithThinker = 0;
            int teamsWithBalanced = 0;

            Map<String, Integer> gameCount = new HashMap<>();
            Map<entity.Role, Integer> roleCount = new HashMap<>();
            Map<entity.PersonalityType, Integer> personalityCount = new HashMap<>();

            for (Team team : teams) {
                if (team.hasGoodGameDiversity()) goodGameDiversity++;
                if (team.hasRoleDiversity(team.getSize())) goodRoleDiversity++;

                Map<entity.PersonalityType, Long> teamPersonality = team.getPersonalityCount();
                if (teamPersonality.getOrDefault(entity.PersonalityType.LEADER, 0L) > 0) teamsWithLeader++;
                if (teamPersonality.getOrDefault(entity.PersonalityType.THINKER, 0L) > 0) teamsWithThinker++;
                if (teamPersonality.getOrDefault(entity.PersonalityType.BALANCED, 0L) > 0) teamsWithBalanced++;

                // Count games, roles, and personalities across all teams
                for (entity.Participant p : team.getMembers()) {
                    gameCount.merge(p.getPreferredGame(), 1, Integer::sum);
                    roleCount.merge(p.getPreferredRole(), 1, Integer::sum);
                    personalityCount.merge(p.getPersonalityType(), 1, Integer::sum);
                }
            }

            System.out.println("\n--- Diversity Statistics ---");
            System.out.println("Teams with Good Game Diversity: " + goodGameDiversity + "/" + teams.size() +
                    " (" + String.format("%.1f%%", (100.0 * goodGameDiversity / teams.size())) + ")");
            System.out.println("Teams with Good Role Diversity: " + goodRoleDiversity + "/" + teams.size() +
                    " (" + String.format("%.1f%%", (100.0 * goodRoleDiversity / teams.size())) + ")");
            System.out.println("Teams with at least 1 Leader: " + teamsWithLeader + "/" + teams.size() +
                    " (" + String.format("%.1f%%", (100.0 * teamsWithLeader / teams.size())) + ")");
            System.out.println("Teams with at least 1 Thinker: " + teamsWithThinker + "/" + teams.size() +
                    " (" + String.format("%.1f%%", (100.0 * teamsWithThinker / teams.size())) + ")");
            System.out.println("Teams with at least 1 Balanced: " + teamsWithBalanced + "/" + teams.size() +
                    " (" + String.format("%.1f%%", (100.0 * teamsWithBalanced / teams.size())) + ")");

            // Game distribution
            System.out.println("\n--- Game Distribution ---");
            gameCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

            // Role distribution
            System.out.println("\n--- Role Distribution ---");
            roleCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

            // Personality distribution
            System.out.println("\n--- Personality Type Distribution ---");
            personalityCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

            // Individual team breakdown
            System.out.println("\n--- Individual Team Statistics ---");
            for (Team team : teams) {
                System.out.println("\nTeam " + team.getTeamId() + ":");
                System.out.println("  Size: " + team.getSize() + " members");
                System.out.println("  Average Skill: " + String.format("%.2f", team.getAverageSkill()));
                System.out.println("  Games: " + String.join(", ", team.getPreferredGames()));
                System.out.println("  Roles: " + team.getPreferredRoles().size() + " different roles");

                Map<PersonalityType, Long> teamPersonality = team.getPersonalityCount();
                System.out.println("  Personalities: " +
                        "Leaders=" + teamPersonality.getOrDefault(entity.PersonalityType.LEADER, 0L) +
                        ", Thinkers=" + teamPersonality.getOrDefault(entity.PersonalityType.THINKER, 0L) +
                        ", Balanced=" + teamPersonality.getOrDefault(entity.PersonalityType.BALANCED, 0L));
            }

            // Check for leftovers
            List<entity.Participant> leftovers = CSVDataHandler.loadLeftovers(filePath);
            if (!leftovers.isEmpty()) {
                System.out.println("\n--- Leftover Participants ---");
                System.out.println("Total Leftovers: " + leftovers.size());
                System.out.println("These participants are waiting to be assigned to teams.");
            }

            System.out.println("\n========================================\n");

        } catch (IOException e) {
            System.err.println("Error loading team statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
