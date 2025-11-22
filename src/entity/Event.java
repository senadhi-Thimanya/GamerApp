package entity;

public class Event {
    private String eventName;
    private String eventDate; // Format: YYYY-MM-DD

    public Event(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public String toString() {
        return String.format(eventName);
    }
}
