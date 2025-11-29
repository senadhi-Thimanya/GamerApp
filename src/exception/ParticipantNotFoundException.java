package exception;

/**
 * Exception thrown when a participant cannot be found in the system
 */
public class ParticipantNotFoundException extends Exception {
    private String participantId;

    public ParticipantNotFoundException(String participantId) {
        super("Participant with ID '" + participantId + "' not found.");
        this.participantId = participantId;
    }

    public ParticipantNotFoundException(String participantId, String message) {
        super(message);
        this.participantId = participantId;
    }

    public String getParticipantId() {
        return participantId;
    }
}