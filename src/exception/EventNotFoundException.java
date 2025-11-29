package exception;

/**
 * Exception thrown when an event cannot be found in the system
 */
public class EventNotFoundException extends Exception {
    private String eventName;

    public EventNotFoundException(String eventName) {
        super("Event '" + eventName + "' not found.");
        this.eventName = eventName;
    }

    public EventNotFoundException(String eventName, String message) {
        super(message);
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}