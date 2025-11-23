package helper;

import entity.Event;
import entity.Participant;
import entity.Role;
import entity.Team;

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
}