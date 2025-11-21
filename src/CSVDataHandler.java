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

    public static void saveTeams(List<Team> teams, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("TeamID,MemberID,Name,PreferredGame,SkillLevel,Role,PersonalityType\n");
            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    bw.write(String.format("%d,%s,%s,%s,%d,%s,%s\n",
                            team.getTeamId(),
                            p.getId(),
                            p.getName(),
                            p.getPreferredGame(),
                            p.getSkillLevel(),
                            p.getPreferredRole(),
                            p.getPersonalityType()));
                }
            }
        }
    }
}