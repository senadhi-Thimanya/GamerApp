package helper;

import entity.Participant;

public class LoginHandler {
    private static final String PARTICIPANTS_FILE = "participants.csv";

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
        try {
            Participant participant = CSVDataHandler.findParticipantById(id, PARTICIPANTS_FILE);
            if (participant == null) {
                System.out.println("Participant ID '" + id + "' not found.");
            }
            return participant;
        } catch (java.io.IOException e) {
            System.err.println("Error reading participants file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}