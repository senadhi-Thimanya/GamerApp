package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Team;
import java.util.*;

/**
 * Thread for forming a single team from a pool of participants
 * Allows parallel team formation for large datasets
 */
public class TeamFormationThread extends Thread {
    private int teamId;
    private int teamSize;
    private List<Participant> availableParticipants;
    private Team formedTeam;
    private List<Participant> usedParticipants;
    private boolean success;

    public TeamFormationThread(int teamId, int teamSize, List<Participant> availableParticipants) {
        this.teamId = teamId;
        this.teamSize = teamSize;
        this.availableParticipants = new ArrayList<>(availableParticipants);
        this.usedParticipants = new ArrayList<>();
        this.success = false;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Thread-" + Thread.currentThread().getId() + "] Forming Team " + teamId);

            formedTeam = new Team(teamId);

            // Separate participants by personality type
            List<Participant> leaders = new ArrayList<>();
            List<Participant> thinkers = new ArrayList<>();
            List<Participant> balanced = new ArrayList<>();

            for (Participant p : availableParticipants) {
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

            // Build team with diversity
            buildBalancedTeam(leaders, thinkers, balanced);

            // Simulate processing time
            Thread.sleep(50);

            success = (formedTeam.getSize() == teamSize);

            if (success) {
                System.out.println("[Thread-" + Thread.currentThread().getId() + "] Team " + teamId +
                        " formed successfully with " + formedTeam.getSize() + " members");
            }

        } catch (Exception e) {
            System.err.println("[Thread-" + Thread.currentThread().getId() + "] Error forming team " + teamId + ": " + e.getMessage());
            success = false;
        }
    }

    private void buildBalancedTeam(List<Participant> leaders, List<Participant> thinkers, List<Participant> balanced) {
        // Combine all participants for selection
        List<Participant> allAvailable = new ArrayList<>();
        allAvailable.addAll(leaders);
        allAvailable.addAll(thinkers);
        allAvailable.addAll(balanced);

        // Select participants with constraints
        for (Participant p : allAvailable) {
            if (formedTeam.getSize() >= teamSize) {
                break;
            }

            if (canAddToTeam(formedTeam, p)) {
                formedTeam.addMember(p);
                usedParticipants.add(p);
            }
        }

        // Fill remaining spots if needed (relaxed constraints)
        if (formedTeam.getSize() < teamSize) {
            for (Participant p : allAvailable) {
                if (formedTeam.getSize() >= teamSize) {
                    break;
                }
                if (!usedParticipants.contains(p)) {
                    formedTeam.addMember(p);
                    usedParticipants.add(p);
                }
            }
        }
    }

    private boolean canAddToTeam(Team team, Participant p) {
        if (team.getSize() >= teamSize) {
            return false;
        }

        // Check game diversity: max 2 per game
        long sameGameCount = team.getMembers().stream()
                .filter(m -> m.getPreferredGame().equalsIgnoreCase(p.getPreferredGame()))
                .count();

        return sameGameCount < 2;
    }

    public Team getFormedTeam() {
        return formedTeam;
    }

    public List<Participant> getUsedParticipants() {
        return usedParticipants;
    }

    public boolean isSuccess() {
        return success;
    }
}