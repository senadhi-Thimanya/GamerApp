package helper;

import entity.Event;
import entity.Participant;
import entity.Team;
import exception.EventNotFoundException;
import exception.FileOperationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static entity.Role.*;
import static entity.Role.SUPPORTER;
import static helper.CSVDataHandler.*;

public class FileIntegrityTests {
    public static void main(String[] args) {
        System.out.println("CSVDataHandler Test Suite\n");
        // Test 1: File doesn't exist
        System.out.println("Test 1: Non-existent participants file");
        try {
            List<Participant> result = loadParticipants("nonexistent.csv");
            System.out.println("ERROR: Should have thrown exception!");
        } catch (FileOperationException e) {
            System.out.println("✓ Caught expected exception: " + e.getMessage());
        }

        // Test 3: Valid file
        System.out.println("\nTest 3: Valid participants file");
        try {
            List<Participant> result = loadParticipants("participants.csv");
            System.out.println("✓ Loaded " + result.size() + " participants");
            for (Participant p : result) {
                System.out.println("  - " + p.getName());
            }
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 1: Directory doesn't exist
        System.out.println("Test 4: Non-existent TeamFormations directory");
        try {
            List<Event> result = loadEvents("nonexistent_dir");
            System.out.println("ERROR: Should have thrown exception!");
        } catch (FileOperationException e) {
            System.out.println("✓ Caught expected exception: " + e.getMessage());
        }

        // Test 2: Path is a file, not a directory
        System.out.println("\nTest 5: TeamFormations Path is a file");
        try {
            List<Event> result = loadEvents("sample.txt");
            System.out.println("ERROR: Should have thrown exception!");
        } catch (FileOperationException e) {
            System.out.println("✓ Caught expected exception: " + e.getMessage());
        }

        // Test 4: Valid directory with CSV files
        System.out.println("\nTest 6: Valid TeamFormations directory with events");
        try {
            List<Event> result = loadEvents("TeamFormations");
            System.out.println("✓ Loaded " + result.size() + " events:");
            for (Event e : result) {
                System.out.println("  - " + e.getEventName());
            }
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 5: Directory with mixed file types
        System.out.println("\nTest 7: TeamFormations Directory with mixed files");
        try {
            List<Event> result = loadEvents("TeamFormations");
            System.out.println("✓ Loaded " + result.size() + " events (only CSV files counted)");
            for (Event e : result) {
                System.out.println("  - " + e.getEventName());
            }
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 1: File doesn't exist
        System.out.println("Test 8: Non-existent teams file");
        try {
            List<Team> result = loadTeams("nonexistent_teams.csv");
            System.out.println("ERROR: Should have thrown exception!");
        } catch (EventNotFoundException e) {
            System.out.println("✓ Caught expected exception: " + e.getMessage());
        } catch (FileOperationException e) {
            System.out.println("ERROR: Wrong exception type!");
        }

        // Test 3: Valid teams file
        System.out.println("\nTest 10: Valid teams file");
        try {
            List<Team> result = loadTeams("TeamFormations/L_team_formation.csv");
            System.out.println("✓ Loaded " + result.size() + " teams:");
            for (Team t : result) {
                System.out.println("  Team " + t.getTeamId() + ": " + t.getMembers().size() + " members");
                for (Participant p : t.getMembers()) {
                    System.out.println("    - " + p.getName() + " (" + p.getPreferredRole() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 4: File with team 0 (leftovers - should be skipped)
        System.out.println("\nTest 11: File with leftovers only (team 0)");
        try {
            List<Team> result = loadTeams("TeamFormations/LeftoversOnly_team_formation.csv");
            System.out.println("✓ Loaded " + result.size() + " teams (team 0 skipped)");
            for (Team t : result) {
                System.out.println("  Team " + t.getTeamId() + ": " + t.getMembers().size() + " members");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 1: Save teams with no leftovers
        System.out.println("Test 1: Save teams with no leftovers");
        try {
            List<Team> teams = createSampleTeams();
            List<Participant> leftovers = new ArrayList<>();

            saveTeamsWithLeftovers(teams, leftovers, "TeamFormations/output_no_leftovers.csv");
            System.out.println("✓ File saved successfully");
            verifyFile("TeamFormations/output_no_leftovers.csv");
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 2: Save teams with leftovers
        System.out.println("\nTest 2: Save teams with leftovers");
        try {
            List<Team> teams = createSampleTeams();
            List<Participant> leftovers = createSampleLeftovers();

            saveTeamsWithLeftovers(teams, leftovers, "TeamFormations/output_with_leftovers.csv");
            System.out.println("✓ File saved successfully");
            verifyFile("TeamFormations/output_with_leftovers.csv");
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 3: Save to invalid path (should throw exception)
        System.out.println("\nTest 3: Invalid file path");
        try {
            List<Team> teams = createSampleTeams();
            List<Participant> leftovers = new ArrayList<>();

            saveTeamsWithLeftovers(teams, leftovers, "/invalid/path/output.csv");
            System.out.println("ERROR: Should have thrown exception!");
        } catch (FileOperationException e) {
            System.out.println("✓ Caught expected exception: " + e.getMessage());
        }

        // Test 4: Empty teams and empty leftovers
        System.out.println("\nTest 4: Empty teams and leftovers");
        try {
            List<Team> teams = new ArrayList<>();
            List<Participant> leftovers = new ArrayList<>();

            saveTeamsWithLeftovers(teams, leftovers, "TeamFormations/output_empty.csv");
            System.out.println("✓ File saved successfully (header only)");
            verifyFile("TeamFormations/output_empty.csv");
        } catch (FileOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 3: File with only team members (no leftovers)
        System.out.println("\nTest 3: File with no leftovers (only teams)");
        try {
            List<Participant> result = loadLeftovers("TeamFormations/output_no_leftovers.csv");
            System.out.println("✓ Loaded " + result.size() + " leftovers (should be 0)");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 4: File with only leftovers
        System.out.println("\nTest 4: File with only leftovers");
        try {
            List<Participant> result = loadLeftovers("TeamFormations/LeftoversOnly_team_formation.csv");
            System.out.println("✓ Loaded " + result.size() + " leftovers:");
            for (Participant p : result) {
                System.out.println("  - " + p.getName() + " (" + p.getPreferredRole() + ")");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Test 5: File with mixed teams and leftovers
        System.out.println("\nTest 5: File with teams and leftovers");
        try {
            List<Participant> result = loadLeftovers("TeamFormations/output_with_leftovers.csv");
            System.out.println("✓ Loaded " + result.size() + " leftovers (teams ignored):");
            for (Participant p : result) {
                System.out.println("  - " + p.getName() + " (" + p.getPreferredRole() + ")");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static List<Team> createSampleTeams() {
        List<Team> teams = new ArrayList<>();

        Team team1 = new Team(1);
        team1.addMember(new Participant("P001", "John Doe", "John@university.edu", "Chess",8, ATTACKER, 75));
        team1.addMember(new Participant("P002", "Jane Smith", "Jane@university.edu", "Chess", 7, DEFENDER, 82));
        teams.add(team1);

        Team team2 = new Team(2);
        team2.addMember(new Participant("P003", "Bob Johnson", "Bob@university.edu","Poker", 6, SUPPORTER, 68));
        team2.addMember(new Participant("P004", "Alice Williams", "Alice@university.edu", "Poker", 9, ATTACKER, 90));
        teams.add(team2);

        return teams;
    }

    private static List<Participant> createSampleLeftovers() {
        List<Participant> leftovers = new ArrayList<>();
        leftovers.add(new Participant("P005", "Charlie Brown", "Charlie@university.edu", "Chess", 5, DEFENDER, 60));
        leftovers.add(new Participant("P006", "Diana Prince", "Diana@university.edu", "Poker", 4, SUPPORTER, 55));
        return leftovers;
    }

    private static void verifyFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.println("  File contents:");
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null && lineCount < 10) {
                System.out.println("    " + line);
                lineCount++;
            }
            if (lineCount == 10) {
                System.out.println("    ... (file continues)");
            }
        } catch (IOException e) {
            System.out.println("  ERROR reading file: " + e.getMessage());
        }
    }
}