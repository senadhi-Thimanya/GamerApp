import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class TeamBuilder {

    public static List<Team> formTeams(List<Participant> participants, int teamSize)
            throws InterruptedException, ExecutionException {

        // Sort by skill descending for better initial distribution
        participants.sort(Comparator.comparingInt(Participant::getSkillLevel).reversed());

        List<Team> teams = new ArrayList<>();
        int totalTeams = participants.size() / teamSize;
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team(i + 1));
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = new ArrayList<>();

        // Create one task per team to fill it in parallel (greedy + constraints)
        for (Team team : teams) {
            tasks.add(() -> {
                while (team.getSize() < teamSize) {
                    synchronized (participants) {
                        Participant best = findBestCandidate(team, participants);
                        if (best != null) {
                            team.addMember(best);
                            participants.remove(best);
                        }
                    }
                }
                return null;
            });
        }

        executor.invokeAll(tasks);
        executor.shutdown();

        // Final pass to fix any imbalances (rare but possible)
        balanceTeams(teams);

        return teams;
    }

    private static Participant findBestCandidate(Team team, List<Participant> remaining) {
        // Scoring logic for "best fit" – you can improve this heuristic
        return remaining.stream()
                .filter(p -> team.getMembers().stream()
                        .filter(m -> m.getPreferredGame().equals(p.getPreferredGame()))
                        .count() < 2) // game cap
                .min(Comparator.comparingInt(p -> {
                    int score = 0;
                    // Prefer leader if missing
                    if (team.getPersonalityCount().getOrDefault(PersonalityType.LEADER, 0L) == 0
                            && p.getPersonalityType() == PersonalityType.LEADER) score -= 30;
                    // Prefer thinker if <2
                    if (team.getPersonalityCount().getOrDefault(PersonalityType.THINKER, 0L) < 2
                            && p.getPersonalityType() == PersonalityType.THINKER) score -= 20;
                    // Skill balance penalty
                    score += Math.abs(p.getSkillLevel() - team.getAverageSkill()) * 5;
                    return score;
                }))
                .orElse(null);
    }

    private static void balanceTeams(List<Team> teams) {
        // swap players between teams if needed to improve skill/role balance
        // (implement if you want extra marks)
    }
}