package helper;

import entity.*;

import java.io.*;
import java.util.*;

public class CSVDataHandler {

    public static List<Participant> loadParticipants(String filePath) throws IOException {
        List<Participant> participants = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Participant p = new Participant(
                        values[0].trim(),
                        values[1].trim(),
                        values[2].trim(),
                        values[3].trim(),
                        Integer.parseInt(values[4].trim()),
                        Role.valueOf(values[5].trim().toUpperCase()),
                        Integer.parseInt(values[6].trim())
                );
                participants.add(p);
            }
        }
        return participants;
    }

    public static List<Admin> loadAdmins(String filePath) throws IOException {
        List<Admin> admins = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Admin a = new Admin(
                        values[0].trim(),
                        values[1].trim(),
                        values[2].trim()
                );
                admins.add(a);
            }
        }
        return admins;
    }

    public static List<Event> loadEvents(String directoryPath) throws IOException {
        List<Event> events = new ArrayList<>();
        File dir = new File(directoryPath);
        //If directory does not exist or is not a directory, return empty list
        if (!dir.exists() || !dir.isDirectory()) {
            return events;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null) return events;
        Set<String> seen = new HashSet<>();
        for (File f : files) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            // For filenames like EventName_Team_Formation.csv take substring before first underscore
            String eventName;
            int us = base.indexOf('_');
            if (us > 0) {
                eventName = base.substring(0, us);
            } else {
                // fallback: remove non-alphanumeric characters and take first token
                String[] parts = base.split("[^A-Za-z0-9]+");
                if (parts.length == 0) continue;
                eventName = parts[0];
            }
            eventName = eventName.trim();
            if (eventName.isEmpty() || seen.contains(eventName)) continue;
            seen.add(eventName);
            events.add(new Event(eventName));
        }
        return events;
    }

    public static List<Team> loadTeams(String filePath) throws IOException {
        Map<Integer, Team> teamMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int teamId = Integer.parseInt(values[0].trim());
                Participant p = new Participant(
                        values[1].trim(),  // MemberID
                        values[2].trim(),  // Name
                        "",                // Email (not stored in team formation CSV, use empty string)
                        values[3].trim(),  // PreferredGame
                        Integer.parseInt(values[4].trim()),  // SkillLevel
                        Role.valueOf(values[5].trim().toUpperCase()),  // Role
                        Integer.parseInt(values[6].trim())  // PersonalityScore (note: column says PersonalityType but value is score)
                );
                teamMap.putIfAbsent(teamId, new Team(teamId));
                teamMap.get(teamId).addMember(p);
            }
        }
        return new ArrayList<>(teamMap.values());
    }

    public static void saveTeams(List<Team> teams, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("TeamID,MemberID,Name,PreferredGame,SkillLevel,Role,PersonalityScore\n");
            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    bw.write(String.format("%d,%s,%s,%s,%d,%s,%d\n",  // Changed last %s to %d
                            team.getTeamId(),
                            p.getId(),
                            p.getName(),
                            p.getPreferredGame(),
                            p.getSkillLevel(),
                            p.getPreferredRole(),
                            p.getPersonalityScore()));  // Changed from getPersonalityType()
                }
            }
        }
    }

    /**
     * Finds and returns a participant by ID from the CSV file
     */
    public static Participant findParticipantById(String id, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length >= 7 && values[0].trim().equals(id)) {
                    return new Participant(
                            values[0].trim(),  // id
                            values[1].trim(),  // name
                            values[2].trim(),  // email
                            values[3].trim(),  // preferredGame
                            Integer.parseInt(values[4].trim()),  // skillLevel
                            Role.valueOf(values[5].trim().toUpperCase()),  // preferredRole
                            Integer.parseInt(values[6].trim())  // personalityScore
                    );
                }
            }
        }
        return null; // Participant not found
    }

    public static Admin findAdminById(String id, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length >= 3 && values[0].trim().equals(id)) {
                    return new Admin(
                            values[0].trim(),  // id
                            values[1].trim(),  // name
                            values[2].trim()   // email
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates a participant's information in the CSV file
     */
    public static void updateParticipant(Participant updatedParticipant, String filePath) throws IOException {
        File inputFile = new File(filePath);
        File tempFile = new File(filePath.replace(".csv", "_temp.csv"));

        boolean participantFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            // Copy header
            line = reader.readLine();
            if (line != null) {
                writer.write(line);
                writer.newLine();
            }

            // Process each line
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length > 0 && values[0].trim().equals(updatedParticipant.getId())) {
                    // Write updated participant data
                    writer.write(String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                            updatedParticipant.getId(),
                            updatedParticipant.getName(),
                            updatedParticipant.getEmail(),
                            updatedParticipant.getPreferredGame(),
                            updatedParticipant.getSkillLevel(),
                            toSentenceCase(String.valueOf(updatedParticipant.getPreferredRole())),
                            updatedParticipant.getPersonalityScore(),
                            toSentenceCase(String.valueOf(updatedParticipant.getPersonalityType()))));
                    writer.newLine();
                    participantFound = true;
                } else {
                    // Copy existing line
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        // Replace original file with updated file
        if (participantFound) {
            if (!inputFile.delete()) {
                throw new IOException("Could not delete original file");
            }
            if (!tempFile.renameTo(inputFile)) {
                throw new IOException("Could not rename temp file");
            }
        } else {
            tempFile.delete();
            throw new IOException("Participant not found in CSV");
        }
    }

    //To change the THINKER to Thinker
    public static String toSentenceCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return as is if input is null or empty
        }
        input = input.toLowerCase(); // Convert the entire string to lowercase
        return input.substring(0, 1).toUpperCase() + input.substring(1); // Capitalize the first letter
    }

    /**
     * Generates a new participant ID by reading the last ID from CSV and incrementing
     */
    public static String generateNewParticipantId(String filePath) throws IOException {
        File file = new File(filePath);

        // If file doesn't exist, start with P001
        if (!file.exists()) {
            return "P001";
        }

        String lastId = "P000";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",");
                if (values.length > 0) {
                    lastId = values[0].trim();
                }
            }
        }

        // Extract number from last ID and increment
        // Assumes format like P001, P002, etc.
        String numPart = lastId.substring(1); // Remove 'P'
        int num = Integer.parseInt(numPart);
        num++;

        // Format with leading zeros (e.g., P001, P002, ..., P099, P100)
        return String.format("P%03d", num);
    }

    /**
     * Adds a new participant to the CSV file
     */
    public static void addParticipant(Participant participant, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(String.format("%s,%s,%s,%s,%d,%s,%d\n",
                    participant.getId(),
                    participant.getName(),
                    participant.getEmail(),
                    participant.getPreferredGame(),
                    participant.getSkillLevel(),
                    participant.getPreferredRole(),
                    participant.getPersonalityScore()));
        }
    }

    /**
     * Checks if a participant ID already exists in the CSV
     */
    public static boolean participantExists(String id, String filePath) {
        try {
            return findParticipantById(id, filePath) != null;
        } catch (IOException e) {
            return false;
        }
    }

}