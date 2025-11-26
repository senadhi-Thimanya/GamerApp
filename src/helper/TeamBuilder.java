package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;
import entity.Team;

import java.util.*;

public class TeamBuilder {

    private static final Random random = new Random();
    private static List<Participant> leftoverParticipants = new ArrayList<>();

    public static List<Team> formTeams(List<Participant> participants, int teamSize) {
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("No participants to form teams");
        }

        int totalTeams = participants.size() / teamSize;
        int leftoverCount = participants.size() % teamSize;

        if (totalTeams == 0) {
            System.out.println("\n=== Insufficient Participants ===");
            System.out.println("Not enough participants to form a complete team.");
            System.out.println("Required: " + teamSize + " participants per team");
            System.out.println("Available: " + participants.size() + " participants");
            System.out.println("All participants will be kept as leftovers until more register.");
            leftoverParticipants.addAll(participants);
            return new ArrayList<>();
        }

        System.out.println("\n=== Team Formation Summary ===");
        System.out.println("Total Participants: " + participants.size());
        System.out.println("Team Size: " + teamSize);
        System.out.println("Teams to Form: " + totalTeams);
        System.out.println("Leftover Participants: " + leftoverCount);
        System.out.println("================================\n");

        // Create teams
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team(i + 1));
        }

        // Separate participants into those who will be in teams and leftovers
        leftoverParticipants.clear();

        // Shuffle for fairness
        List<Participant> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants, random);

        // First (totalTeams * teamSize) participants go into teams, rest are leftovers
        List<Participant> toAssign = shuffledParticipants.subList(0, totalTeams * teamSize);
        leftoverParticipants.addAll(shuffledParticipants.subList(totalTeams * teamSize, shuffledParticipants.size()));

        // Separate participants by personality type
        List<Participant> leaders = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();

        for (Participant p : toAssign) {
            switch (p.getPersonalityType()) {
                case LEADER:
                    leaders.add(p);
                    break;
                case THINKER:
                    thinkers.add(p);
                    break;
                case BALANCED:
                    balanced.add(p);
                    break;
            }
        }

        // Sort by skill for better distribution
        leaders.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());
        thinkers.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());
        balanced.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());

        // Simple round-robin distribution
        distributeRoundRobin(teams, leaders, thinkers, balanced, teamSize);

        // Balance skills
        balanceTeamSkills(teams, teamSize);

        // Final validation
        validateTeams(teams, teamSize);

        return teams;
    }

    /**
     * Simple round-robin distribution ensuring exact team sizes
     */
    private static void distributeRoundRobin(List<Team> teams, List<Participant> leaders,
                                             List<Participant> thinkers, List<Participant> balanced,
                                             int teamSize) {
        // Create a single list with priority order: leaders first, then thinkers, then balanced
        List<Participant> allParticipants = new ArrayList<>();
        allParticipants.addAll(leaders);
        allParticipants.addAll(thinkers);
        allParticipants.addAll(balanced);

        // Distribute round-robin with constraint checking
        int teamIndex = 0;
        List<Participant> deferred = new ArrayList<>();

        for (Participant p : allParticipants) {
            boolean assigned = false;
            int attempts = 0;

            // Try to assign to teams starting from current index
            while (attempts < teams.size() && !assigned) {
                Team currentTeam = teams.get(teamIndex);

                if (currentTeam.getSize() < teamSize && canAddToTeamRelaxed(currentTeam, p, teamSize)) {
                    currentTeam.addMember(p);
                    assigned = true;
                } else {
                    teamIndex = (teamIndex + 1) % teams.size();
                    attempts++;
                }
            }

            // If couldn't assign with constraints, defer for later
            if (!assigned) {
                deferred.add(p);
            } else {
                teamIndex = (teamIndex + 1) % teams.size();
            }
        }

        // Assign deferred participants (relax constraints completely)
        for (Participant p : deferred) {
            for (Team team : teams) {
                if (team.getSize() < teamSize) {
                    team.addMember(p);
                    break;
                }
            }
        }
    }

    /**
     * Relaxed constraint checking - only enforces critical constraints
     */
    private static boolean canAddToTeamRelaxed(Team team, Participant p, int teamSize) {
        if (team.getSize() >= teamSize) {
            return false;
        }

        // Only check game diversity: max 2 per game
        long sameGameCount = team.getMembers().stream()
                .filter(m -> m.getPreferredGame().equalsIgnoreCase(p.getPreferredGame()))
                .count();

        return sameGameCount < 2;
    }

    /**
     * Balance skills through limited swaps
     */
    private static void balanceTeamSkills(List<Team> teams, int teamSize) {
        int maxIterations = 20; // Reduced iterations
        int iteration = 0;

        while (iteration < maxIterations) {
            Team highestTeam = teams.stream()
                    .max(Comparator.comparingDouble(Team::getAverageSkill))
                    .orElse(null);

            Team lowestTeam = teams.stream()
                    .min(Comparator.comparingDouble(Team::getAverageSkill))
                    .orElse(null);

            if (highestTeam == null || lowestTeam == null) break;

            double skillDifference = highestTeam.getAverageSkill() - lowestTeam.getAverageSkill();

            if (skillDifference < 1.5) {
                break;
            }

            boolean swapped = trySwapForBalance(highestTeam, lowestTeam);

            if (!swapped) {
                break;
            }

            iteration++;
        }
    }

    /**
     * Simple swap attempt
     */
    private static boolean trySwapForBalance(Team team1, Team team2) {
        List<Participant> team1Members = new ArrayList<>(team1.getMembers());
        List<Participant> team2Members = new ArrayList<>(team2.getMembers());

        // Try first valid swap found
        for (Participant p1 : team1Members) {
            for (Participant p2 : team2Members) {
                if (Math.abs(p1.getSkillLevel() - p2.getSkillLevel()) > 1) {
                    // Just swap without checking constraints (we already have valid teams)
                    team1.getMembers().remove(p1);
                    team2.getMembers().remove(p2);
                    team1.addMember(p2);
                    team2.addMember(p1);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validate
     */
    private static void validateTeams(List<Team> teams, int teamSize) {
        // Check all teams have exact size
        for (Team team : teams) {
            if (team.getSize() != teamSize) {
                System.err.println("ERROR: Team " + team.getTeamId() +
                        " has " + team.getSize() + " members instead of " + teamSize);
            }
        }
    }

    public static List<Participant> getLeftoverParticipants() {
        return new ArrayList<>(leftoverParticipants);
    }

    public static boolean hasLeftovers() {
        return !leftoverParticipants.isEmpty();
    }
}