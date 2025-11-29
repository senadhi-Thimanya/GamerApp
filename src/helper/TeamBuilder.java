package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;
import entity.Team;
import exception.TeamFormationException;

import java.util.*;

public class TeamBuilder {

    private static final Random random = new Random();
    private static List<Participant> leftoverParticipants = new ArrayList<>();
    private static final int PARALLEL_THRESHOLD = 10;

    public static List<Team> formTeams(List<Participant> participants, int teamSize) throws TeamFormationException {
        if (participants == null || participants.isEmpty()) {
            throw new TeamFormationException("No participants available to form teams");
        }

        if (teamSize < 3) {
            throw new TeamFormationException("Team size must be at least 3 participants");
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

        boolean useParallel = participants.size() >= PARALLEL_THRESHOLD;
        System.out.println("Processing Mode: " + (useParallel ? "PARALLEL (Multi-threaded)" : "SEQUENTIAL"));
        System.out.println("================================\n");

        List<Team> teams;
        try {
            if (useParallel) {
                teams = formTeamsParallel(participants, teamSize, totalTeams);
            } else {
                teams = formTeamsSequential(participants, teamSize, totalTeams);
            }

            balanceTeamSkills(teams, teamSize);
            validateTeams(teams, teamSize);

            return teams;
        } catch (Exception e) {
            throw new TeamFormationException("Error during team formation: " + e.getMessage(), e);
        }
    }

    private static List<Team> formTeamsSequential(List<Participant> participants, int teamSize, int totalTeams) {
        System.out.println("Using sequential processing...");

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team(i + 1));
        }

        leftoverParticipants.clear();

        List<Participant> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants, random);

        List<Participant> toAssign = shuffledParticipants.subList(0, totalTeams * teamSize);
        leftoverParticipants.addAll(shuffledParticipants.subList(totalTeams * teamSize, shuffledParticipants.size()));

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

        leaders.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());
        thinkers.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());
        balanced.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());

        distributeRoundRobin(teams, leaders, thinkers, balanced, teamSize);

        return teams;
    }

    private static List<Team> formTeamsParallel(List<Participant> participants, int teamSize, int totalTeams) {
        System.out.println("Using parallel processing with " + totalTeams + " threads...");

        leftoverParticipants.clear();

        List<Participant> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants, random);

        int toAssignCount = totalTeams * teamSize;

        List<Participant> sharedPool = Collections.synchronizedList(
                new ArrayList<>(shuffledParticipants.subList(0, Math.min(toAssignCount, shuffledParticipants.size())))
        );

        if (shuffledParticipants.size() > toAssignCount) {
            leftoverParticipants.addAll(shuffledParticipants.subList(toAssignCount, shuffledParticipants.size()));
        }

        List<TeamFormationThread> threads = new ArrayList<>();

        for (int i = 0; i < totalTeams; i++) {
            TeamFormationThread thread = new TeamFormationThread(i + 1, teamSize, sharedPool);
            threads.add(thread);
            thread.start();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Waiting for all threads to complete...");
        List<Team> formedTeams = new ArrayList<>();

        for (TeamFormationThread thread : threads) {
            try {
                thread.join();

                if (thread.isSuccess()) {
                    formedTeams.add(thread.getFormedTeam());
                } else {
                    System.err.println("Warning: Team " + thread.getFormedTeam().getTeamId() +
                            " was not fully formed");
                    if (thread.getFormedTeam().getSize() > 0) {
                        formedTeams.add(thread.getFormedTeam());
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All threads completed. Formed " + formedTeams.size() + " teams.");

        if (!sharedPool.isEmpty()) {
            System.out.println("Warning: " + sharedPool.size() + " participants remained unassigned");
            leftoverParticipants.addAll(sharedPool);
        }

        formedTeams.sort(Comparator.comparingInt(Team::getTeamId));

        return formedTeams;
    }

    private static void distributeRoundRobin(List<Team> teams, List<Participant> leaders,
                                             List<Participant> thinkers, List<Participant> balanced,
                                             int teamSize) {
        List<Participant> allParticipants = new ArrayList<>();
        allParticipants.addAll(leaders);
        allParticipants.addAll(thinkers);
        allParticipants.addAll(balanced);

        int teamIndex = 0;
        List<Participant> deferred = new ArrayList<>();

        for (Participant p : allParticipants) {
            boolean assigned = false;
            int attempts = 0;

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

            if (!assigned) {
                deferred.add(p);
            } else {
                teamIndex = (teamIndex + 1) % teams.size();
            }
        }

        for (Participant p : deferred) {
            for (Team team : teams) {
                if (team.getSize() < teamSize) {
                    team.addMember(p);
                    break;
                }
            }
        }
    }

    private static boolean canAddToTeamRelaxed(Team team, Participant p, int teamSize) {
        if (team.getSize() >= teamSize) {
            return false;
        }

        long sameGameCount = team.getMembers().stream()
                .filter(m -> m.getPreferredGame().equalsIgnoreCase(p.getPreferredGame()))
                .count();

        return sameGameCount < 2;
    }

    private static void balanceTeamSkills(List<Team> teams, int teamSize) {
        int maxIterations = 20;
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

    private static boolean trySwapForBalance(Team team1, Team team2) {
        List<Participant> team1Members = new ArrayList<>(team1.getMembers());
        List<Participant> team2Members = new ArrayList<>(team2.getMembers());

        for (Participant p1 : team1Members) {
            for (Participant p2 : team2Members) {
                if (Math.abs(p1.getSkillLevel() - p2.getSkillLevel()) > 1) {
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

    private static void validateTeams(List<Team> teams, int teamSize) {
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