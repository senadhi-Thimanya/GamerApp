package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;
import entity.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple unit tests for helper package classes
 * Concurrency tests + Unit tests
 */
public class HelperTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("HELPER PACKAGE UNIT TESTS");
        System.out.println("=".repeat(60));

        // Run all test suites
        testTeamFormationThread();
        testTeamBuilder();
        testSurveyProcessingThread();

        // Print summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests + " ✓");
        System.out.println("Failed: " + failedTests + " ✗");
        System.out.println("Success Rate: " + String.format("%.2f", (passedTests * 100.0 / totalTests)) + "%");
        System.out.println("=".repeat(60));
    }

    // ==================== TeamFormationThread Tests ====================

    private static void testTeamFormationThread() {
        System.out.println("\n--- TeamFormationThread Tests ---\n");

        testThreadFormation();
        testThreadWithInsufficientParticipants();
        testThreadConcurrentAccess();
    }

    private static void testThreadFormation() {
        String testName = "TeamFormationThread - Basic Team Formation";
        try {
            List<Participant> pool = createTestParticipants(5);
            TeamFormationThread thread = new TeamFormationThread(1, 3, pool);

            thread.start();
            thread.join();

            Team formedTeam = thread.getFormedTeam();
            boolean success = thread.isSuccess();

            if (formedTeam != null && success && formedTeam.getSize() == 3) {
                pass(testName);
            } else {
                fail(testName, "Expected team size 3, got " + (formedTeam != null ? formedTeam.getSize() : "null"));
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testThreadWithInsufficientParticipants() {
        String testName = "TeamFormationThread - Insufficient Participants";
        try {
            List<Participant> pool = createTestParticipants(2);
            TeamFormationThread thread = new TeamFormationThread(1, 5, pool);

            thread.start();
            thread.join();

            Team formedTeam = thread.getFormedTeam();
            boolean success = thread.isSuccess();

            if (formedTeam != null && !success && formedTeam.getSize() < 5) {
                pass(testName);
            } else {
                fail(testName, "Expected incomplete team, got success: " + success);
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testThreadConcurrentAccess() {
        String testName = "TeamFormationThread - Concurrent Access";
        try {
            List<Participant> pool = createTestParticipants(9);

            TeamFormationThread thread1 = new TeamFormationThread(1, 3, pool);
            TeamFormationThread thread2 = new TeamFormationThread(2, 3, pool);
            TeamFormationThread thread3 = new TeamFormationThread(3, 3, pool);

            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join();
            thread2.join();
            thread3.join();

            int totalAssigned = thread1.getFormedTeam().getSize() +
                    thread2.getFormedTeam().getSize() +
                    thread3.getFormedTeam().getSize();

            if (totalAssigned == 9 && pool.isEmpty()) {
                pass(testName);
            } else {
                fail(testName, "Expected 9 assigned and empty pool, got " + totalAssigned + " assigned, pool size: " + pool.size());
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    // ==================== TeamBuilder Tests ====================

    private static void testTeamBuilder() {
        System.out.println("\n--- TeamBuilder Tests ---\n");

        testSequentialTeamFormation();
        testParallelTeamFormation();
        testInsufficientParticipants();
        testLeftoverParticipants();
    }

    private static void testSequentialTeamFormation() {
        String testName = "TeamBuilder - Sequential Formation";
        try {
            List<Participant> participants = createTestParticipants(9);
            List<Team> teams = TeamBuilder.formTeams(participants, 3);

            if (teams.size() == 3 && teams.stream().allMatch(t -> t.getSize() == 3)) {
                pass(testName);
            } else {
                fail(testName, "Expected 3 teams with 3 members each");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testParallelTeamFormation() {
        String testName = "TeamBuilder - Parallel Formation";
        try {
            List<Participant> participants = createTestParticipants(15);
            List<Team> teams = TeamBuilder.formTeams(participants, 3);

            if (teams.size() == 5 && teams.stream().allMatch(t -> t.getSize() == 3)) {
                pass(testName);
            } else {
                fail(testName, "Expected 5 teams with 3 members each");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testInsufficientParticipants() {
        String testName = "TeamBuilder - Insufficient Participants";
        try {
            List<Participant> participants = createTestParticipants(2);
            List<Team> teams = TeamBuilder.formTeams(participants, 3);

            if (teams.isEmpty() && TeamBuilder.hasLeftovers()) {
                pass(testName);
            } else {
                fail(testName, "Expected empty teams and leftovers");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testLeftoverParticipants() {
        String testName = "TeamBuilder - Leftover Participants";
        try {
            List<Participant> participants = createTestParticipants(10);
            List<Team> teams = TeamBuilder.formTeams(participants, 3);

            List<Participant> leftovers = TeamBuilder.getLeftoverParticipants();

            if (teams.size() == 3 && leftovers.size() == 1) {
                pass(testName);
            } else {
                fail(testName, "Expected 3 teams and 1 leftover, got " + teams.size() + " teams and " + leftovers.size() + " leftovers");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    // ==================== SurveyProcessingThread Tests ====================

    private static void testSurveyProcessingThread() {
        System.out.println("\n--- SurveyProcessingThread Tests ---\n");

        testThreadValidSurveyData();
        testThreadInvalidPersonalityScore();
        testThreadInvalidSkillLevel();
    }

    private static void testThreadValidSurveyData() {
        String testName = "SurveyProcessingThread - Valid Survey Data";
        try {
            Participant p = new Participant("T001", "Test User", "test@uni.edu",
                    "Valorant", 5, Role.STRATEGIST, 60);

            SurveyProcessingThread thread = new SurveyProcessingThread(p, "participants.csv", true);
            thread.start();
            thread.join();

            if (thread.isSuccess() && thread.getError() == null) {
                pass(testName);
            } else {
                fail(testName, "Expected successful processing");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        }
    }

    private static void testThreadInvalidPersonalityScore() {
        String testName = "SurveyProcessingThread - Invalid Personality Score";

        // Temporarily redirect System.err to suppress expected error messages
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            public void write(int b) {} // Discard output
        }));

        try {
            Participant p = new Participant("T002", "Test User", "test@uni.edu",
                    "Valorant", 5, Role.STRATEGIST, 150);

            SurveyProcessingThread thread = new SurveyProcessingThread(p, "participants.csv", true);
            thread.start();
            thread.join();

            if (!thread.isSuccess() && thread.getError() != null) {
                pass(testName);
            } else {
                fail(testName, "Expected validation failure for invalid personality score");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        } finally {
            // Restore System.err
            System.setErr(originalErr);
        }
    }

    private static void testThreadInvalidSkillLevel() {
        String testName = "SurveyProcessingThread - Invalid Skill Level";

        // Temporarily redirect System.err to suppress expected error messages
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            public void write(int b) {} // Discard output
        }));

        try {
            Participant p = new Participant("T003", "Test User", "test@uni.edu",
                    "Valorant", 15, Role.STRATEGIST, 60);

            SurveyProcessingThread thread = new SurveyProcessingThread(p, "participants.csv", true);
            thread.start();
            thread.join();

            if (!thread.isSuccess() && thread.getError() != null) {
                pass(testName);
            } else {
                fail(testName, "Expected validation failure for invalid skill level");
            }
        } catch (Exception e) {
            fail(testName, e.getMessage());
        } finally {
            // Restore System.err
            System.setErr(originalErr);
        }
    }

    // ==================== Helper Methods ====================

    private static List<Participant> createTestParticipants(int count) {
        List<Participant> participants = new ArrayList<>();
        String[] games = {"Valorant", "Dota", "FIFA", "CS:GO"};
        Role[] roles = {Role.STRATEGIST, Role.ATTACKER, Role.DEFENDER, Role.SUPPORTER};
        int[] scores = {40, 65, 85};

        for (int i = 0; i < count; i++) {
            String id = "P" + String.format("%03d", i + 1);
            String name = "Player " + (i + 1);
            String email = "player" + (i + 1) + "@uni.edu";
            String game = games[i % games.length];
            int skillLevel = (i % 10) + 1;
            Role role = roles[i % roles.length];
            int personalityScore = scores[i % scores.length];

            participants.add(new Participant(id, name, email, game, skillLevel, role, personalityScore));
        }

        return participants;
    }

    private static void pass(String testName) {
        totalTests++;
        passedTests++;
        System.out.println("✓ PASS: " + testName);
    }

    private static void fail(String testName, String reason) {
        totalTests++;
        failedTests++;
        System.out.println("✗ FAIL: " + testName);
        System.out.println("  Reason: " + reason);
    }
}