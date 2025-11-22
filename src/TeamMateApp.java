import entity.Participant;
import entity.Team;
import helper.CSVDataHandler;
import helper.TeamBuilder;

import java.util.List;
import java.util.Scanner;

public class TeamMateApp {
    public static void main(String[] args) {
        try {
            List<Participant> participants = CSVDataHandler.loadParticipants("participants.csv");

            Scanner sc = new Scanner(System.in);
            System.out.print("Enter team size: ");
            int teamSize = sc.nextInt();

            List<Team> teams = TeamBuilder.formTeams(participants, teamSize);

            teams.forEach(System.out::println);

            // need the event name as a csv name
            CSVDataHandler.saveTeams(teams, "formed_teams.csv");
            System.out.println("Teams saved to formed_teams.csv");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}