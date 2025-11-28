package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;

import java.io.IOException;
import java.util.Scanner;

public class SurveyHandler {
    private static final Scanner sc = new Scanner(System.in);
    private static final String PARTICIPANTS_FILE = "participants.csv";

    /**
     * Conducts survey for existing users (updates their information)
     * Now uses threading for concurrent processing
     */
    public static void conductSurvey(String loggedInUserId) {
        System.out.println("┏┓╻┏━╸╻ ╻   ┏━╸┏━┓┏┳┓┏━╸┏━┓   ┏━╸┏━┓┏┳┓╻┏┓╻┏━╸   ┏━╸╻  ╻ ╻┏┓    ┏━┓╻ ╻┏━┓╻ ╻┏━╸╻ ╻\n" +
                "┃┗┫┣╸ ┃╻┃   ┃╺┓┣━┫┃┃┃┣╸ ┣┳┛   ┃╺┓┣━┫┃┃┃┃┃┗┫┃╺┓   ┃  ┃  ┃ ┃┣┻┓   ┗━┓┃ ┃┣┳┛┃┏┛┣╸ ┗┳┛\n" +
                "╹ ╹┗━╸┗┻┛   ┗━┛╹ ╹╹ ╹┗━╸╹┗╸   ┗━┛╹ ╹╹ ╹╹╹ ╹┗━┛   ┗━╸┗━╸┗━┛┗━┛   ┗━┛┗━┛╹┗╸┗┛ ┗━╸ ╹ ");
        System.out.println("\nThis survey will help us understand your gaming preferences and personality.\n");

        //System.out.print("Enter your Participant ID: ");
        //String participantId = sc.nextLine().trim();

        Participant existingParticipant = LoginHandler.userExists(loggedInUserId);
        if (existingParticipant == null) {
            System.out.println("Participant ID not found. Please register first.");
            return;
        }

        System.out.println("Welcome, " + existingParticipant.getName() + "!\n");

        // Collect survey data
        SurveyData data = collectSurveyData();

        // Create updated participant
        Participant updatedParticipant = new Participant(
                existingParticipant.getId(),
                existingParticipant.getName(),
                existingParticipant.getEmail(),
                data.preferredGame,
                data.skillLevel,
                data.preferredRole,
                data.personalityScore
        );

        // Process survey using thread
        System.out.println("\nProcessing your survey data...");
        SurveyProcessingThread surveyThread = new SurveyProcessingThread(updatedParticipant, PARTICIPANTS_FILE);
        surveyThread.start();

        try {
            surveyThread.join(); // Wait for thread to complete

            if (surveyThread.isSuccess()) {
                displaySurveyResults(updatedParticipant, "updated");
            } else {
                System.err.println("Error saving survey results: " + surveyThread.getError().getMessage());
                surveyThread.getError().printStackTrace();
            }
        } catch (InterruptedException e) {
            System.err.println("Survey processing was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Conducts survey for new users during registration
     * Uses threading for processing
     */
    public static Participant conductSurveyForNewUser(String id, String name, String email) {
        System.out.println("┏━╸┏━┓┏┳┓┏━┓╻  ┏━╸╺┳╸┏━╸   ╻ ╻┏━┓╻ ╻┏━┓   ┏━┓┏━┓┏━┓┏━╸╻╻  ┏━╸\n" +
                "┃  ┃ ┃┃┃┃┣━┛┃  ┣╸  ┃ ┣╸    ┗┳┛┃ ┃┃ ┃┣┳┛   ┣━┛┣┳┛┃ ┃┣╸ ┃┃  ┣╸ \n" +
                "┗━╸┗━┛╹ ╹╹  ┗━╸┗━╸ ╹ ┗━╸    ╹ ┗━┛┗━┛╹┗╸   ╹  ╹┗╸┗━┛╹  ╹┗━╸┗━╸");

        // Collect survey data
        SurveyData data = collectSurveyData();

        // Create new participant object
        Participant newParticipant = new Participant(
                id,
                name,
                email,
                data.preferredGame,
                data.skillLevel,
                data.preferredRole,
                data.personalityScore
        );

        // Display summary
        System.out.println("\n=== Profile Summary ===");
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        displaySurveyResults(newParticipant, "created");

        return newParticipant;
    }

    /**
     * Batch process multiple surveys concurrently (for admin use or bulk operations)
     */
    public static void processSurveysInParallel(java.util.List<Participant> participants, String filePath) {
        System.out.println("\n=== Processing " + participants.size() + " surveys in parallel ===");

        java.util.List<SurveyProcessingThread> threads = new java.util.ArrayList<>();

        // Create and start threads
        for (Participant p : participants) {
            SurveyProcessingThread thread = new SurveyProcessingThread(p, filePath);
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        int successCount = 0;
        int failureCount = 0;

        for (SurveyProcessingThread thread : threads) {
            try {
                thread.join();
                if (thread.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                failureCount++;
            }
        }

        System.out.println("\n=== Batch Processing Complete ===");
        System.out.println("Successfully processed: " + successCount);
        System.out.println("Failed: " + failureCount);
        System.out.println("================================\n");
    }

    /**
     * Collects all survey data and returns it in a SurveyData object
     */
    private static SurveyData collectSurveyData() {
        SurveyData data = new SurveyData();
        data.personalityScore = conductPersonalitySurvey();
        data.preferredGame = getGamePreference();
        data.preferredRole = getPreferredRole();
        data.skillLevel = getSkillLevel();
        return data;
    }

    /**
     * Displays survey results in a consistent format
     */
    private static void displaySurveyResults(Participant participant, String action) {
        System.out.println("\n=== Survey Complete! ===");
        System.out.println("Your Profile:");
        System.out.println("  Personality Score: " + participant.getPersonalityScore());
        System.out.println("  Personality Type: " + participant.getPersonalityType());
        System.out.println("  Preferred Game: " + participant.getPreferredGame());
        System.out.println("  Skill Level: " + participant.getSkillLevel() + "/10");
        System.out.println("  Preferred Role: " + participant.getPreferredRole());
        System.out.println("\nYour information has been " + action + " successfully!");
    }

    private static int conductPersonalitySurvey() {
        System.out.println("--- Personality Assessment ---");
        System.out.println("Please rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)\n");

        String[] questions = {
                "I enjoy taking the lead and guiding others during group activities.",
                "I prefer analyzing situations and coming up with strategic solutions.",
                "I work well with others and enjoy collaborative teamwork.",
                "I am calm under pressure and can help maintain team morale.",
                "I like making quick decisions and adapting in dynamic situations."
        };

        int totalScore = 0;
        for (int i = 0; i < questions.length; i++) {
            System.out.println("Q" + (i + 1) + ": " + questions[i]);
            totalScore += getValidRating();
            System.out.println();
        }

        int scaledScore = totalScore * 4;
        System.out.println("Raw Score: " + totalScore + "/25");
        System.out.println("Scaled Score: " + scaledScore + "/100");
        System.out.println("Your Personality Type: " + Participant.calculatePersonalityType(scaledScore));
        System.out.println();

        return scaledScore;
    }

    private static int getValidRating() {
        while (true) {
            System.out.print("Your rating (1-5): ");
            try {
                int rating = Integer.parseInt(sc.nextLine().trim());
                if (rating >= 1 && rating <= 5) {
                    return rating;
                }
                System.out.println("Please enter a number between 1 and 5.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    private static String getGamePreference() {
        System.out.println("--- Game Preference ---");
        System.out.println("Select your preferred game:");
        String[] games = {"Valorant", "Dota", "FIFA", "CS:GO", "League of Legends", "Overwatch"};

        for (int i = 0; i < games.length; i++) {
            System.out.println((i + 1) + ". " + games[i]);
        }
        System.out.println("7. Other");

        while (true) {
            System.out.print("Enter your choice (1-7): ");
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= 6) {
                    System.out.println("Selected: " + games[choice - 1] + "\n");
                    return games[choice - 1];
                } else if (choice == 7) {
                    System.out.print("Enter game name: ");
                    String customGame = sc.nextLine().trim();
                    System.out.println("Selected: " + customGame + "\n");
                    return customGame;
                }
                System.out.println("Please enter a number between 1 and 7.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 7.");
            }
        }
    }

    private static Role getPreferredRole() {
        System.out.println("--- Preferred Role ---");
        System.out.println("Select your preferred playing role:");

        Role[] roles = {Role.STRATEGIST, Role.ATTACKER, Role.DEFENDER, Role.SUPPORTER, Role.COORDINATOR};
        String[] descriptions = {
                "Plans and coordinates team strategy",
                "Focuses on offensive plays",
                "Protects and holds positions",
                "Assists and enables teammates",
                "Manages team communication and timing"
        };

        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i] + " - " + descriptions[i]);
        }

        while (true) {
            System.out.print("Enter your choice (1-5): ");
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= 5) {
                    System.out.println("Selected: " + roles[choice - 1] + "\n");
                    return roles[choice - 1];
                }
                System.out.println("Please enter a number between 1 and 5.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    private static int getSkillLevel() {
        System.out.println("--- Skill Level ---");
        System.out.println("Rate your gaming skill level (1-10):");
        System.out.println("1-3: Beginner | 4-6: Intermediate | 7-8: Advanced | 9-10: Expert");

        while (true) {
            System.out.print("Enter your skill level (1-10): ");
            try {
                int skillLevel = Integer.parseInt(sc.nextLine().trim());
                if (skillLevel >= 1 && skillLevel <= 10) {
                    System.out.println("Skill Level: " + skillLevel + "/10\n");
                    return skillLevel;
                }
                System.out.println("Please enter a number between 1 and 10.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 10.");
            }
        }
    }

    /**
     * Inner class to hold survey data
     */
    private static class SurveyData {
        int personalityScore;
        String preferredGame;
        Role preferredRole;
        int skillLevel;
    }
}