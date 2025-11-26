package helper;

import entity.Participant;
import entity.Role;
import java.io.IOException;

/**
 * Thread for processing individual survey responses
 * This allows multiple surveys to be processed concurrently
 */
public class SurveyProcessingThread extends Thread {
    private Participant participant;
    private String filePath;
    private boolean success;
    private Exception error;

    public SurveyProcessingThread(Participant participant, String filePath) {
        this.participant = participant;
        this.filePath = filePath;
        this.success = false;
        this.error = null;
    }

    @Override
    public void run() {
        try {
            // Simulate survey data processing (could include validation, calculations, etc.)
            System.out.println("[Thread-" + Thread.currentThread().getId() + "] Processing survey for: " + participant.getName());

            // Perform any survey-specific calculations or validations
            validateSurveyData();

            // Save the participant data
            synchronized (CSVDataHandler.class) {
                // Synchronize file access to prevent concurrent write issues
                CSVDataHandler.updateParticipant(participant, filePath);
            }

            this.success = true;
            System.out.println("[Thread-" + Thread.currentThread().getId() + "] Successfully processed survey for: " + participant.getName());

        } catch (Exception e) {
            this.error = e;
            this.success = false;
            System.err.println("[Thread-" + Thread.currentThread().getId() + "] Error processing survey: " + e.getMessage());
        }
    }

    private void validateSurveyData() {
        // Validate personality score range
        if (participant.getPersonalityScore() < 20 || participant.getPersonalityScore() > 100) {
            throw new IllegalArgumentException("Invalid personality score");
        }

        // Validate skill level
        if (participant.getSkillLevel() < 1 || participant.getSkillLevel() > 10) {
            throw new IllegalArgumentException("Invalid skill level");
        }

        // Add small delay to simulate processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }

    public Participant getParticipant() {
        return participant;
    }
}