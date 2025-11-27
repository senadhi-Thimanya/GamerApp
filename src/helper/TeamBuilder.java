package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Role;
import entity.Team;

import java.util.*;

public class TeamBuilder {

    private static final Random random = new Random();
    private static List<Participant> leftoverParticipants = new ArrayList<>();
    private static final int PARALLEL_THRESHOLD = 10; // Use parallel processing if more than 10 participants

    /**
     * Forms teams with optional parallel processing for large datasets
     */
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

        // Decide whether to use parallel processing
        boolean useParallel = participants.size() >= PARALLEL_THRESHOLD;
        System.out.println("Processing Mode: " + (useParallel ? "PARALLEL (Multi-threaded)" : "SEQUENTIAL"));
        System.out.println("================================\n");

        List<Team> teams;
        if (useParallel) {
            teams = formTeamsParallel(participants, teamSize, totalTeams);
        } else {
            teams = formTeamsSequential(participants, teamSize, totalTeams);
        }

        // Balance skills
        balanceTeamSkills(teams, teamSize);

        // Final validation
        validateTeams(teams, teamSize);

        return teams;
    }

    /**
     * Sequential team formation (original method)
     */
    private static List<Team> formTeamsSequential(List<Participant> participants, int teamSize, int totalTeams) {
        System.out.println("Using sequential processing...");

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team(i + 1));
        }

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

        return teams;
    }

    /**
     * Parallel team formation using threads
     */
    private static List<Team> formTeamsParallel(List<Participant> participants, int teamSize, int totalTeams) {
        System.out.println("Using parallel processing with " + totalTeams + " threads...");

        leftoverParticipants.clear();

        // Shuffle for fairness
        List<Participant> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants, random);

        // Calculate how many will be assigned and how many are leftovers
        int toAssignCount = totalTeams * teamSize;

        // Create a SHARED synchronized list that all threads will pull from
        List<Participant> sharedPool = Collections.synchronizedList(
                new ArrayList<>(shuffledParticipants.subList(0, Math.min(toAssignCount, shuffledParticipants.size())))
        );

        // Add leftovers
        if (shuffledParticipants.size() > toAssignCount) {
            leftoverParticipants.addAll(shuffledParticipants.subList(toAssignCount, shuffledParticipants.size()));
        }

        List<TeamFormationThread> threads = new ArrayList<>();

        // Create and start threads for each team - they all share the same pool
        for (int i = 0; i < totalTeams; i++) {
            TeamFormationThread thread = new TeamFormationThread(i + 1, teamSize, sharedPool);
            threads.add(thread);
            thread.start();

            // Small delay to stagger thread starts
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Wait for all threads to complete
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
                    // Still add it if it has members
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

        // Check if there are participants left in the shared pool (shouldn't happen but just in case)
        if (!sharedPool.isEmpty()) {
            System.out.println("Warning: " + sharedPool.size() + " participants remained unassigned");
            leftoverParticipants.addAll(sharedPool);
        }

        // Sort teams by ID
        formedTeams.sort(Comparator.comparingInt(Team::getTeamId));

        return formedTeams;
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
     * Validate teams
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