package helper;

import entity.Participant;
import entity.Role;

public class LoginHandler {
    public static boolean register() {
        // Registration logic here
        return true; // Placeholder
    }

    public static boolean Login() {
        // Login logic here
        return true; // Placeholder
    }

    /**
     * Loads a participant from the participants.csv file by ID
     * @param id The participant ID to search for
     * @return Participant object if found, null otherwise
     */
    public static Participant gamerLogin(String id) {
        java.io.File file = new java.io.File("participants.csv");
        if (!file.exists()) {
            System.out.println("Error: participants.csv file not found.");
            return null;
        }

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            // Skip header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length < 7) continue; // Need at least 7 columns

                // Check if this is the participant we're looking for
                if (values[0].trim().equals(id)) {
                    // Create and return the Participant object
                    // Format: ID,Name,Email,PreferredGame,SkillLevel,Role,PersonalityScore
                    Participant p = new Participant(
                            values[0].trim(),  // id
                            values[1].trim(),  // name
                            values[2].trim(),  // email
                            values[3].trim(),  // preferredGame
                            Integer.parseInt(values[4].trim()),  // skillLevel
                            Role.valueOf(values[5].trim().toUpperCase()),  // preferredRole
                            Integer.parseInt(values[6].trim())  // personalityScore
                    );
                    return p;
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Error reading participants file: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing participant data: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing role: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Participant not found
    }
}