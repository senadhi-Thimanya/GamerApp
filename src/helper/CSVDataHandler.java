package helper;

import entity.*;
import exception.*;

import java.io.*;
import java.util.*;

public class CSVDataHandler {

    public static List<Participant> loadParticipants(String filePath) throws FileOperationException {
        List<Participant> participants = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileOperationException(filePath, "read", "Participants file does not exist.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null) {
                throw new FileOperationException(filePath, "read", "Participants file is empty.");
            }

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = line.split(",");
                    if (values.length < 7) {
                        System.err.println("Warning: Skipping line " + lineNumber + " - insufficient data");
                        continue;
                    }

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
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid number format");
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid role value");
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }
        return participants;
    }

    public static List<Event> loadEvents(String directoryPath) throws FileOperationException {
        List<Event> events = new ArrayList<>();
        File dir = new File(directoryPath);

        if (!dir.exists()) {
            throw new FileOperationException(directoryPath, "read", "Team formations directory does not exist.");
        }

        if (!dir.isDirectory()) {
            throw new FileOperationException(directoryPath, "read", "Path is not a directory.");
        }

        try {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
            if (files == null) {
                throw new FileOperationException(directoryPath, "read", "Unable to list files in directory.");
            }

            Set<String> seen = new HashSet<>();
            for (File f : files) {
                String name = f.getName();
                int dot = name.lastIndexOf('.');
                String base = dot > 0 ? name.substring(0, dot) : name;

                String eventName;
                int us = base.indexOf('_');
                if (us > 0) {
                    eventName = base.substring(0, us);
                } else {
                    String[] parts = base.split("[^A-Za-z0-9]+");
                    if (parts.length == 0) continue;
                    eventName = parts[0];
                }

                eventName = eventName.trim();
                if (eventName.isEmpty() || seen.contains(eventName)) continue;
                seen.add(eventName);
                events.add(new Event(eventName));
            }
        } catch (Exception e) {
            throw new FileOperationException(directoryPath, "read", e);
        }

        return events;
    }

    public static List<Team> loadTeams(String filePath) throws FileOperationException, EventNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new EventNotFoundException(filePath, "Team formation file does not exist: " + filePath);
        }

        Map<Integer, Team> teamMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null) {
                throw new FileOperationException(filePath, "read", "Team formation file is empty.");
            }

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = line.split(",");
                    if (values.length < 7) {
                        System.err.println("Warning: Skipping line " + lineNumber + " - insufficient data");
                        continue;
                    }

                    int teamId = Integer.parseInt(values[0].trim());
                    if (teamId == 0) continue; // Skip leftovers

                    Participant p = new Participant(
                            values[1].trim(),
                            values[2].trim(),
                            "",
                            values[3].trim(),
                            Integer.parseInt(values[4].trim()),
                            Role.valueOf(values[5].trim().toUpperCase()),
                            Integer.parseInt(values[6].trim())
                    );
                    teamMap.putIfAbsent(teamId, new Team(teamId));
                    teamMap.get(teamId).addMember(p);
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid number format");
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid role value");
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }
        return new ArrayList<>(teamMap.values());
    }

    public static void saveTeamsWithLeftovers(List<Team> teams, List<Participant> leftovers, String filePath) throws FileOperationException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("TeamID,MemberID,Name,PreferredGame,SkillLevel,Role,PersonalityScore\n");

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    bw.write(String.format("%d,%s,%s,%s,%d,%s,%d\n",
                            team.getTeamId(),
                            p.getId(),
                            p.getName(),
                            p.getPreferredGame(),
                            p.getSkillLevel(),
                            p.getPreferredRole(),
                            p.getPersonalityScore()));
                }
            }

            for (Participant p : leftovers) {
                bw.write(String.format("0,%s,%s,%s,%d,%s,%d\n",
                        p.getId(),
                        p.getName(),
                        p.getPreferredGame(),
                        p.getSkillLevel(),
                        p.getPreferredRole(),
                        p.getPersonalityScore()));
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "write", e);
        }
    }

    public static List<Participant> loadLeftovers(String filePath) throws FileOperationException, EventNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new EventNotFoundException(filePath, "Team formation file does not exist: " + filePath);
        }

        List<Participant> leftovers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null) {
                throw new FileOperationException(filePath, "read", "Team formation file is empty.");
            }

            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = line.split(",");
                    if (values.length < 7) {
                        System.err.println("Warning: Skipping line " + lineNumber + " - insufficient data");
                        continue;
                    }

                    int teamId = Integer.parseInt(values[0].trim());
                    if (teamId == 0) {
                        Participant p = new Participant(
                                values[1].trim(),
                                values[2].trim(),
                                "",
                                values[3].trim(),
                                Integer.parseInt(values[4].trim()),
                                Role.valueOf(values[5].trim().toUpperCase()),
                                Integer.parseInt(values[6].trim())
                        );
                        leftovers.add(p);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid number format");
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Skipping line " + lineNumber + " - invalid role value");
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }
        return leftovers;
    }

    public static Participant findParticipantById(String id, String filePath) throws FileOperationException, ParticipantNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileOperationException(filePath, "read", "Participants file does not exist.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length >= 7 && values[0].trim().equals(id)) {
                    return new Participant(
                            values[0].trim(),
                            values[1].trim(),
                            values[2].trim(),
                            values[3].trim(),
                            Integer.parseInt(values[4].trim()),
                            Role.valueOf(values[5].trim().toUpperCase()),
                            Integer.parseInt(values[6].trim())
                    );
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }

        throw new ParticipantNotFoundException(id);
    }

    public static Admin findAdminById(String id, String filePath) throws FileOperationException, AdminNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileOperationException(filePath, "read", "Admins file does not exist.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length >= 3 && values[0].trim().equals(id)) {
                    return new Admin(
                            values[0].trim(),
                            values[1].trim(),
                            values[2].trim()
                    );
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }

        throw new AdminNotFoundException(id);
    }

    public static void updateParticipant(Participant updatedParticipant, String filePath) throws FileOperationException, ParticipantNotFoundException {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            throw new FileOperationException(filePath, "update", "Participants file does not exist.");
        }

        File tempFile = new File(filePath.replace(".csv", "_temp.csv"));
        boolean participantFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line = reader.readLine();
            if (line != null) {
                writer.write(line);
                writer.newLine();
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length > 0 && values[0].trim().equals(updatedParticipant.getId())) {
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
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "update", e);
        }

        if (participantFound) {
            if (!inputFile.delete()) {
                throw new FileOperationException(filePath, "delete", "Could not delete original file");
            }
            if (!tempFile.renameTo(inputFile)) {
                throw new FileOperationException(filePath, "rename", "Could not rename temp file");
            }
        } else {
            tempFile.delete();
            throw new ParticipantNotFoundException(updatedParticipant.getId());
        }
    }

    public static String toSentenceCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = input.toLowerCase();
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String generateNewParticipantId(String filePath) throws FileOperationException {
        File file = new File(filePath);

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
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }

        try {
            String numPart = lastId.substring(1);
            int num = Integer.parseInt(numPart);
            num++;
            return String.format("P%03d", num);
        } catch (Exception e) {
            throw new FileOperationException(filePath, "read", "Invalid participant ID format in file");
        }
    }

    public static void addParticipant(Participant participant, String filePath) throws FileOperationException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(String.format("%s,%s,%s,%s,%d,%s,%d\n",
                    participant.getId(),
                    participant.getName(),
                    participant.getEmail(),
                    participant.getPreferredGame(),
                    participant.getSkillLevel(),
                    participant.getPreferredRole(),
                    participant.getPersonalityScore()));
        } catch (IOException e) {
            throw new FileOperationException(filePath, "write", e);
        }
    }

    public static Participant getParticipantDetails(String id, String filePath) throws FileOperationException, ParticipantNotFoundException {
        return findParticipantById(id, filePath);
    }

    public static void updateParticipantNameEmail(String id, String newName, String newEmail, String filePath) throws FileOperationException, ParticipantNotFoundException {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            throw new FileOperationException(filePath, "update", "Participants file does not exist.");
        }

        File tempFile = new File(filePath.replace(".csv", "_temp.csv"));
        boolean participantFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line = reader.readLine();
            if (line != null) {
                writer.write(line);
                writer.newLine();
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length >= 7 && values[0].trim().equals(id)) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                            values[0].trim(),
                            newName,
                            newEmail,
                            values[3].trim(),
                            values[4].trim(),
                            values[5].trim(),
                            values[6].trim(),
                            values.length > 7 ? values[7].trim() : ""));
                    writer.newLine();
                    participantFound = true;
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new FileOperationException(filePath, "update", e);
        }

        if (participantFound) {
            if (!inputFile.delete()) {
                throw new FileOperationException(filePath, "delete", "Could not delete original file");
            }
            if (!tempFile.renameTo(inputFile)) {
                throw new FileOperationException(filePath, "rename", "Could not rename temp file");
            }
        } else {
            tempFile.delete();
            throw new ParticipantNotFoundException(id);
        }
    }
}