package helper;

import entity.Participant;
import entity.PersonalityType;
import entity.Team;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread for forming a single team from a shared pool of participants
 * Uses locks to ensure thread-safe participant selection
 */
public class TeamFormationThread extends Thread {
    private int teamId;
    private int teamSize;
    private List<Participant> sharedParticipantPool; // Shared reference
    private List<Participant> usedParticipants;      // Local tracking
    private Team formedTeam;
    private boolean success;
    private static final Lock poolLock = new ReentrantLock(); // Shared lock across all threads

    public TeamFormationThread(int teamId, int teamSize, List<Participant> sharedParticipantPool) {
        this.teamId = teamId;
        this.teamSize = teamSize;
        this.sharedParticipantPool = sharedParticipantPool; // Reference to shared list
        this.usedParticipants = new ArrayList<>();
        this.success = false;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Thread-" + Thread.currentThread().getId() + "] Forming Team " + teamId);

            formedTeam = new Team(teamId);

            // Build team by selecting from shared pool with proper locking
            buildBalancedTeam();

            // Simulate processing time
            Thread.sleep(50);

            success = (formedTeam.getSize() == teamSize);

            if (success) {
                System.out.println("[Thread-" + Thread.currentThread().getId() + "] Team " + teamId +
                        " formed successfully with " + formedTeam.getSize() + " members");
            } else {
                System.out.println("[Thread-" + Thread.currentThread().getId() + "] Team " + teamId +
                        " only formed with " + formedTeam.getSize() + " members (expected " + teamSize + ")");
            }

        } catch (Exception e) {
            System.err.println("[Thread-" + Thread.currentThread().getId() + "] Error forming team " + teamId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        }
    }

    private void buildBalancedTeam() {
        // Keep trying to add participants until team is full
        while (formedTeam.getSize() < teamSize) {
            Participant selectedParticipant = null;

            // Lock the shared pool while selecting a participant
            poolLock.lock();
            try {
                if (sharedParticipantPool.isEmpty()) {
                    System.out.println("[Thread-" + Thread.currentThread().getId() + "] No more participants available");
                    break;
                }

                // Try to find best participant considering constraints
                selectedParticipant = selectBestParticipant();

                if (selectedParticipant != null) {
                    // Remove from shared pool immediately
                    sharedParticipantPool.remove(selectedParticipant);
                }

            } finally {
                poolLock.unlock();
            }

            // If we found a participant, add to team
            if (selectedParticipant != null) {
                formedTeam.addMember(selectedParticipant);
                usedParticipants.add(selectedParticipant);
            } else {
                // No suitable participant found
                break;
            }
        }
    }

    private Participant selectBestParticipant() {
        // Priority 1: Try to get a leader if we don't have one
        Map<PersonalityType, Long> currentPersonalities = formedTeam.getPersonalityCount();
        long leaderCount = currentPersonalities.getOrDefault(PersonalityType.LEADER, 0L);

        if (leaderCount == 0) {
            for (Participant p : sharedParticipantPool) {
                if (p.getPersonalityType() == PersonalityType.LEADER && canAddToTeam(formedTeam, p)) {
                    return p;
                }
            }
        }

        // Priority 2: Try to get a thinker if we need one
        long thinkerCount = currentPersonalities.getOrDefault(PersonalityType.THINKER, 0L);
        int maxThinkers = teamSize == 3 ? 1 : 2;

        if (thinkerCount < maxThinkers) {
            for (Participant p : sharedParticipantPool) {
                if (p.getPersonalityType() == PersonalityType.THINKER && canAddToTeam(formedTeam, p)) {
                    return p;
                }
            }
        }

        // Priority 3: Get any participant that fits constraints
        for (Participant p : sharedParticipantPool) {
            if (canAddToTeam(formedTeam, p)) {
                return p;
            }
        }

        // Priority 4: If no participant fits constraints, take first available (relaxed constraints)
        if (!sharedParticipantPool.isEmpty()) {
            return sharedParticipantPool.get(0);
        }

        return null;
    }

    private boolean canAddToTeam(Team team, Participant p) {
        if (team.getSize() >= teamSize) {
            return false;
        }

        // Check game diversity: max 2 per game
        long sameGameCount = team.getMembers().stream()
                .filter(m -> m.getPreferredGame().equalsIgnoreCase(p.getPreferredGame()))
                .count();

        if (sameGameCount >= 2) {
            return false;
        }

        // Check personality caps
        Map<PersonalityType, Long> personalityCount = team.getPersonalityCount();
        long leaderCount = personalityCount.getOrDefault(PersonalityType.LEADER, 0L);
        long thinkerCount = personalityCount.getOrDefault(PersonalityType.THINKER, 0L);

        if (p.getPersonalityType() == PersonalityType.LEADER && leaderCount >= 2) {
            return false;
        }

        int maxThinkers = teamSize == 3 ? 2 : 3;
        if (p.getPersonalityType() == PersonalityType.THINKER && thinkerCount >= maxThinkers) {
            return false;
        }

        return true;
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