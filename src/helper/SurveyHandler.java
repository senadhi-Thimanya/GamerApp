package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;

import java.io.*;
import java.util.Scanner;

public class SurveyHandler {
    private static final Scanner sc = new Scanner(System.in);
    private static final String PARTICIPANTS_FILE = "participants.csv";

    public static void conductSurvey() {
        System.out.println("\n=== Gaming Club Survey ===");
        System.out.println("This survey will help us understand your gaming preferences and personality.\n");

        // Get participant ID (assuming they're already logged in or registered)
        System.out.print("Enter your Participant ID: ");
        String participantId = sc.nextLine().trim();

        // Check if participant exists
        Participant existingParticipant = LoginHandler.gamerLogin(participantId);
        if (existingParticipant == null) {
            System.out.println("Participant ID not found. Please register first.");
            return;
        }

        System.out.println("Welcome, " + existingParticipant.getName() + "!\n");

        // Conduct personality survey
        int personalityScore = conductPersonalitySurvey();

        // Get game preference
        String preferredGame = getGamePreference();

        // Get preferred role
        Role preferredRole = getPreferredRole();

        // Get skill level
        int skillLevel = getSkillLevel();

        // Create updated participant
        Participant updatedParticipant = new Participant(
                existingParticipant.getId(),
                existingParticipant.getName(),
                existingParticipant.getEmail(),
                preferredGame,
                skillLevel,
                preferredRole,
                personalityScore
        );

        // Save updated participant data using CSVDataHandler
        try {
            CSVDataHandler.updateParticipant(updatedParticipant, PARTICIPANTS_FILE);
            System.out.println("\n=== Survey Complete! ===");
            System.out.println("Your Profile:");
            System.out.println("  Personality Score: " + personalityScore);
            System.out.println("  Personality Type: " + updatedParticipant.getPersonalityType());
            System.out.println("  Preferred Game: " + preferredGame);
            System.out.println("  Skill Level: " + skillLevel + "/10");
            System.out.println("  Preferred Role: " + preferredRole);
            System.out.println("\nYour information has been updated successfully!");
        } catch (IOException e) {
            System.err.println("Error saving survey results: " + e.getMessage());
            e.printStackTrace();
        }
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
            int response = getValidRating();
            totalScore += response;
            System.out.println();
        }

        // Scale to 100
        int scaledScore = totalScore * 4;

        System.out.println("Raw Score: " + totalScore + "/25");
        System.out.println("Scaled Score: " + scaledScore + "/100");

        PersonalityType type = Participant.calculatePersonalityType(scaledScore);
        System.out.println("Your Personality Type: " + type);
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
                } else {
                    System.out.println("Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    private static String getGamePreference() {
        System.out.println("--- Game Preference ---");
        System.out.println("Select your preferred game:");
        System.out.println("1. Valorant");
        System.out.println("2. Dota");
        System.out.println("3. FIFA");
        System.out.println("4. CS:GO");
        System.out.println("5. League of Legends");
        System.out.println("6. Overwatch");
        System.out.println("7. Other");

        String[] games = {"Valorant", "Dota", "FIFA", "CS:GO", "League of Legends", "Overwatch"};

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
                } else {
                    System.out.println("Please enter a number between 1 and 7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 7.");
            }
        }
    }

    private static Role getPreferredRole() {
        System.out.println("--- Preferred Role ---");
        System.out.println("Select your preferred playing role:");
        System.out.println("1. STRATEGIST - Plans and coordinates team strategy");
        System.out.println("2. ATTACKER - Focuses on offensive plays");
        System.out.println("3. DEFENDER - Protects and holds positions");
        System.out.println("4. SUPPORTER - Assists and enables teammates");
        System.out.println("5. COORDINATOR - Manages team communication and timing");

        Role[] roles = {Role.STRATEGIST, Role.ATTACKER, Role.DEFENDER, Role.SUPPORTER, Role.COORDINATOR};

        while (true) {
            System.out.print("Enter your choice (1-5): ");
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= 5) {
                    System.out.println("Selected: " + roles[choice - 1] + "\n");
                    return roles[choice - 1];
                } else {
                    System.out.println("Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    private static int getSkillLevel() {
        System.out.println("--- Skill Level ---");
        System.out.println("Rate your gaming skill level (1-10):");
        System.out.println("1-3: Beginner");
        System.out.println("4-6: Intermediate");
        System.out.println("7-8: Advanced");
        System.out.println("9-10: Expert");

        while (true) {
            System.out.print("Enter your skill level (1-10): ");
            try {
                int skillLevel = Integer.parseInt(sc.nextLine().trim());
                if (skillLevel >= 1 && skillLevel <= 10) {
                    System.out.println("Skill Level: " + skillLevel + "/10\n");
                    return skillLevel;
                } else {
                    System.out.println("Please enter a number between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 10.");
            }
        }
    }
}