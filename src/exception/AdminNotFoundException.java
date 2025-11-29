package exception;

/**
 * Exception thrown when an admin cannot be found in the system
 */
public class AdminNotFoundException extends Exception {
    private String adminId;

    public AdminNotFoundException(String adminId) {
        super("Admin with ID '" + adminId + "' not found.");
        this.adminId = adminId;
    }

    public AdminNotFoundException(String adminId, String message) {
        super(message);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }
}