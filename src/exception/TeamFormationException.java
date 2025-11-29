package exception;

/**
 * Exception thrown when team formation fails
 */
public class TeamFormationException extends Exception {
    private int availableParticipants;
    private int requiredTeamSize;

    public TeamFormationException(String message) {
        super(message);
        this.availableParticipants = 0;
        this.requiredTeamSize = 0;
    }

    public TeamFormationException(int availableParticipants, int requiredTeamSize) {
        super("Cannot form teams: " + availableParticipants + " participants available, but team size of " + requiredTeamSize + " required.");
        this.availableParticipants = availableParticipants;
        this.requiredTeamSize = requiredTeamSize;
    }

    public TeamFormationException(String message, Throwable cause) {
        super(message, cause);
        this.availableParticipants = 0;
        this.requiredTeamSize = 0;
    }

    public int getAvailableParticipants() {
        return availableParticipants;
    }

    public int getRequiredTeamSize() {
        return requiredTeamSize;
    }
}