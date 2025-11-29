import entity.Event;
import entity.Participant;
import entity.PersonalityType;
import entity.Team;
import helper.CSVDataHandler;
import helper.LoginHandler;
import helper.TeamBuilder;
import exception.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminInterface {
    private static final Scanner sc = new Scanner(System.in);

    public static void launch() {
        System.out.print("\nWhat is your ID? ");
        String id = sc.nextLine().trim();

        if (id.isEmpty()) {
            System.out.println("ID cannot be empty. Exiting Admin Interface.");
            return;
        }

        if (!LoginHandler.adminLogin(id)) return;

        adminMenu();
        while (true) {
            System.out.print("\nDo you want to perform another action? 1. yes 2. no : ");
            String again = sc.nextLine().trim().toLowerCase();
            if (again.equals("1")) {
                adminMenu();
            } else {
                System.out.println("\n\tExiting Admin Interface. Goodbye!");
                break;
            }
        }
    }

    public static void adminMenu() {
        int option = getAdminOptions();
        switch (option) {
            case 1:
                createEventTeamFormation();
                break;
            case 2:
                viewTeamFormation();
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
            case 7:
                bulkProcessSurveys();
                break;
            default:
                System.out.println("\tInvalid option selected.");
        }
    }

    public static int getAdminOptions() {
        System.out.println("┏━┓╺┳┓┏┳┓╻┏┓╻   ┏┳┓┏━╸┏┓╻╻ ╻\n" +
                "┣━┫ ┃┃┃┃┃┃┃┗┫   ┃┃┃┣╸ ┃┗┫┃ ┃\n" +
                "╹ ╹╺┻┛╹ ╹╹╹ ╹   ╹ ╹┗━╸╹ ╹┗━┛");
        System.out.println("1. Create new Event Team Formation");
        System.out.println("2. View Teams");
        System.out.println("3. View All Gamers");
        System.out.println("4. View all Events");
        System.out.println("5. View Team Statistics");
        System.out.println("6. View Leftover Participants");
        System.out.println("7. Bulk Process Surveys (Demo)");

        while (true) {
            System.out.print("\tEnter your choice: ");
            try {
                String input = sc.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please enter a number between 1 and 7.");
                    continue;
                }
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 7) {
                    return choice;
                }
                System.out.println("Please enter a number between 1 and 7.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 7.");
            }
        }
    }

    private static void createEventTeamFormation() {
        System.out.println("┏━╸┏━┓┏━╸┏━┓╺┳╸┏━╸   ┏┓╻┏━╸╻ ╻   ┏━╸╻ ╻┏━╸┏┓╻╺┳╸   ╺┳╸┏━╸┏━┓┏┳┓   ┏━╸┏━┓┏━┓┏┳┓┏━┓╺┳╸╻┏━┓┏┓╻\n" +
                "┃  ┣┳┛┣╸ ┣━┫ ┃ ┣╸    ┃┗┫┣╸ ┃╻┃   ┣╸ ┃┏┛┣╸ ┃┗┫ ┃     ┃ ┣╸ ┣━┫┃┃┃   ┣╸ ┃ ┃┣┳┛┃┃┃┣━┫ ┃ ┃┃ ┃┃┗┫\n" +
                "┗━╸╹┗╸┗━╸╹ ╹ ╹ ┗━╸   ╹ ╹┗━╸┗┻┛   ┗━╸┗┛ ┗━╸╹ ╹ ╹     ╹ ┗━╸╹ ╹╹ ╹   ╹  ┗━┛╹┗╸╹ ╹╹ ╹ ╹ ╹┗━┛╹ ╹");

        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("\tEvent name cannot be empty.");
            return;
        }

        Event event = new Event(eventName);
        System.out.println("\nEvent created: " + event);

        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");

            if (participants.isEmpty()) {
                System.out.println("\tNo participants found. Please ensure participants have registered.");
                return;
            }

            int teamSize = 0;
            while (true) {
                System.out.print("\nEnter team size: ");
                try {
                    String input = sc.nextLine().trim();
                    if (input.isEmpty()) {
                        System.out.println("Input cannot be empty. Please enter a number.");
                        continue;
                    }
                    teamSize = Integer.parseInt(input);
                    if (teamSize >= 3) {
                        break;
                    }
                    System.out.println("\tTeam size must be at least 3.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            String filePath = "TeamFormations/" + event.getEventName() + "_team_formation.csv";

            List<Team> teams = TeamBuilder.formTeams(participants, teamSize);

            if (!teams.isEmpty()) {
                teams.forEach(System.out::println);
            }

            List<Participant> leftovers = TeamBuilder.getLeftoverParticipants();
            CSVDataHandler.saveTeamsWithLeftovers(teams, leftovers, filePath);
            System.out.println("\tTeams and leftovers saved to: " + filePath);

        } catch (TeamFormationException e) {
            System.err.println("\tTeam formation error: " + e.getMessage());
        } catch (FileOperationException e) {
            System.err.println("\tFile operation error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\tUnexpected error: " + e.getMessage());
        }
    }

    private static void viewTeamFormation() {
        System.out.println("╻ ╻╻┏━╸╻ ╻   ╺┳╸┏━╸┏━┓┏┳┓   ┏━╸┏━┓┏━┓┏┳┓┏━┓╺┳╸╻┏━┓┏┓╻\n" +
                "┃┏┛┃┣╸ ┃╻┃    ┃ ┣╸ ┣━┫┃┃┃   ┣╸ ┃ ┃┣┳┛┃┃┃┣━┫ ┃ ┃┃ ┃┃┗┫\n" +
                "┗┛ ╹┗━╸┗┻┛    ╹ ┗━╸╹ ╹╹ ╹   ╹  ┗━┛╹┗╸╹ ╹╹ ╹ ╹ ╹┗━┛╹ ╹");
        System.out.print("Enter Event Name to view teams: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("\tEvent name cannot be empty.");
            return;
        }

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";

        try {
            List<Team> teams = CSVDataHandler.loadTeams(filePath);
            if (teams.isEmpty()) {
                System.out.println("\tNo teams found for this event.");
            } else {
                teams.forEach(System.out::println);
            }
        } catch (EventNotFoundException e) {
            System.out.println("\t" + e.getMessage());
        } catch (FileOperationException e) {
            System.err.println("\tError reading teams: " + e.getMessage());
        }
    }

    private static void viewAllGamers() {
        System.out.println("┏━┓╻  ╻     ┏━┓┏━╸┏━╸╻┏━┓╺┳╸┏━╸┏━┓┏━╸╺┳┓   ┏━╸┏━┓┏┳┓┏━╸┏━┓┏━┓\n" +
                "┣━┫┃  ┃     ┣┳┛┣╸ ┃╺┓┃┗━┓ ┃ ┣╸ ┣┳┛┣╸  ┃┃   ┃╺┓┣━┫┃┃┃┣╸ ┣┳┛┗━┓\n" +
                "╹ ╹┗━╸┗━╸   ╹┗╸┗━╸┗━┛╹┗━┛ ╹ ┗━╸╹┗╸┗━╸╺┻┛   ┗━┛╹ ╹╹ ╹┗━╸╹┗╸┗━┛");
        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");
            if (participants.isEmpty()) {
                System.out.println("\tNo gamers registered yet.");
            } else {
                System.out.println("\nTotal Gamers: " + participants.size() + "\n");
                for (int i = 0; i < participants.size(); i++) {
                    System.out.println((i + 1) + ". " + participants.get(i).toString());
                }
            }
        } catch (FileOperationException e) {
            System.err.println("\tError loading participants: " + e.getMessage());
        }
    }

    private static void viewLeftoverParticipants() {
        System.out.println("╻ ╻╻┏━╸╻ ╻   ╻  ┏━╸┏━╸╺┳╸┏━┓╻ ╻┏━╸┏━┓   ┏━┓┏━┓┏━┓╺┳╸╻┏━╸╻┏━┓┏━┓┏┓╻╺┳╸┏━┓\n" +
                "┃┏┛┃┣╸ ┃╻┃   ┃  ┣╸ ┣╸  ┃ ┃ ┃┃┏┛┣╸ ┣┳┛   ┣━┛┣━┫┣┳┛ ┃ ┃┃  ┃┣━┛┣━┫┃┗┫ ┃ ┗━┓\n" +
                "┗┛ ╹┗━╸┗┻┛   ┗━╸┗━╸╹   ╹ ┗━┛┗┛ ┗━╸╹┗╸   ╹  ╹ ╹╹┗╸ ╹ ╹┗━╸╹╹  ╹ ╹╹ ╹ ╹ ┗━┛");

        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("\tEvent name cannot be empty.");
            return;
        }

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";

        try {
            List<Participant> leftovers = CSVDataHandler.loadLeftovers(filePath);

            if (leftovers.isEmpty()) {
                System.out.println("\tNo leftover participants for event '" + eventName + "'.");
                System.out.println("\tAll participants have been assigned to teams.");
            } else {
                System.out.println("\nEvent: " + eventName);
                System.out.println("The following participants are waiting to be assigned to teams:\n");
                for (int i = 0; i < leftovers.size(); i++) {
                    System.out.println((i + 1) + ". " + leftovers.get(i).toString());
                }
                System.out.println("\nTotal Leftovers: " + leftovers.size());
            }
        } catch (EventNotFoundException e) {
            System.out.println("\t" + e.getMessage());
        } catch (FileOperationException e) {
            System.err.println("\tError loading leftovers: " + e.getMessage());
        }
        System.out.println("===================================\n");
    }

    private static void viewTeamStatistics() {
        System.out.println("╻ ╻╻┏━╸╻ ╻   ╺┳╸┏━╸┏━┓┏┳┓   ┏━┓╺┳╸┏━┓╺┳╸╻┏━┓╺┳╸╻┏━╸┏━┓\n" +
                "┃┏┛┃┣╸ ┃╻┃    ┃ ┣╸ ┣━┫┃┃┃   ┗━┓ ┃ ┣━┫ ┃ ┃┗━┓ ┃ ┃┃  ┗━┓\n" +
                "┗┛ ╹┗━╸┗┻┛    ╹ ┗━╸╹ ╹╹ ╹   ┗━┛ ╹ ╹ ╹ ╹ ╹┗━┛ ╹ ╹┗━╸┗━┛");

        System.out.print("Enter Event Name: ");
        String eventName = sc.nextLine().trim();

        if (eventName.isEmpty()) {
            System.out.println("\tEvent name cannot be empty.");
            return;
        }

        String filePath = "TeamFormations/" + eventName + "_team_formation.csv";

        try {
            List<Team> teams = CSVDataHandler.loadTeams(filePath);

            if (teams.isEmpty()) {
                System.out.println("\tNo teams found for event '" + eventName + "'.");
                return;
            }

            System.out.println("\n=== Statistics for Event: " + eventName + " ===\n");

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

            System.out.println("\n--- Game Distribution ---");
            gameCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

            System.out.println("\n--- Role Distribution ---");
            roleCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

            System.out.println("\n--- Personality Type Distribution ---");
            personalityCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() +
                            " participants (" + String.format("%.1f%%", (100.0 * entry.getValue() / totalParticipants)) + ")"));

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

            List<entity.Participant> leftovers = CSVDataHandler.loadLeftovers(filePath);
            if (!leftovers.isEmpty()) {
                System.out.println("\n--- Leftover Participants ---");
                System.out.println("Total Leftovers: " + leftovers.size());
                System.out.println("These participants are waiting to be assigned to teams.");
            }

            System.out.println("\n========================================\n");

        } catch (EventNotFoundException e) {
            System.out.println("\t" + e.getMessage());
        } catch (FileOperationException e) {
            System.err.println("\tError loading team statistics: " + e.getMessage());
        }
    }

    private static void bulkProcessSurveys() {
        System.out.println("┏┓ ╻ ╻╻  ╻┏    ┏━┓╻ ╻┏━┓╻ ╻┏━╸╻ ╻   ┏━┓┏━┓┏━┓┏━╸┏━╸┏━┓┏━┓╻┏┓╻┏━╸\n" +
                "┣┻┓┃ ┃┃  ┣┻┓   ┗━┓┃ ┃┣┳┛┃┏┛┣╸ ┗┳┛   ┣━┛┣┳┛┃ ┃┃  ┣╸ ┗━┓┗━┓┃┃┗┫┃╺┓\n" +
                "┗━┛┗━┛┗━╸╹ ╹   ┗━┛┗━┛╹┗╸┗┛ ┗━╸ ╹    ╹  ╹┗╸┗━┛┗━╸┗━╸┗━┛┗━┛╹╹ ╹┗━┛");
        System.out.println("\nThis feature demonstrates multi-threaded survey processing.");
        System.out.println("Multiple surveys are processed concurrently using separate threads.\n");

        try {
            List<Participant> allParticipants = CSVDataHandler.loadParticipants("participants.csv");

            if (allParticipants.isEmpty()) {
                System.out.println("No participants found. Please register some participants first.");
                return;
            }

            System.out.println("Total Participants Available: " + allParticipants.size());

            int count = 0;
            while (true) {
                System.out.print("How many participants to process? (1-" + allParticipants.size() + "): ");
                try {
                    String input = sc.nextLine().trim();
                    if (input.isEmpty()) {
                        System.out.println("Input cannot be empty.");
                        continue;
                    }
                    count = Integer.parseInt(input);
                    if (count >= 1 && count <= allParticipants.size()) {
                        break;
                    }
                    System.out.println("Please enter a number between 1 and " + allParticipants.size());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            List<Participant> toProcess = allParticipants.subList(0, count);

            System.out.println("\n--- Processing Configuration ---");
            System.out.println("Participants to process: " + count);
            System.out.println("Available CPU cores: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Processing mode: Multi-threaded (Concurrent)");
            System.out.println("--------------------------------\n");

            long startTime = System.currentTimeMillis();

            helper.SurveyHandler.processSurveysInParallel(toProcess, "participants.csv");

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("\n=== Performance Metrics ===");
            System.out.println("Total Processing Time: " + totalTime + "ms");
            System.out.println("Average Time per Participant: " + (totalTime / count) + "ms");
            System.out.println("Throughput: " + String.format("%.2f", (count * 1000.0 / totalTime)) + " participants/second");

            long estimatedSequentialTime = count * 100;
            if (totalTime > 0) {
                double speedup = (double) estimatedSequentialTime / totalTime;
                System.out.println("\n--- Concurrency Benefit ---");
                System.out.println("Estimated Sequential Time: ~" + estimatedSequentialTime + "ms");
                System.out.println("Actual Parallel Time: " + totalTime + "ms");
                System.out.println("Speedup Factor: " + String.format("%.2fx faster", speedup));
            }

            System.out.println("===========================\n");

        } catch (FileOperationException e) {
            System.err.println("Error loading participants: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during bulk processing: " + e.getMessage());
        }
    }
}