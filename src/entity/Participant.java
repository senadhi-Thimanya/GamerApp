package entity;

public class Participant {
    private String id;
    private String name;
    private String email;
    private String preferredGame;        // e.g., "Valorant", "FIFA", "CS:GO"
    private int skillLevel;              // 1–10
    private Role preferredRole;
    private int personalityScore;        // scaled score 20–100 (as in sample CSV)
    private PersonalityType personalityType;  // derived from score

    // Constructor (used when loading from CSV)
    public Participant(String id, String name, String email, String preferredGame,
                       int skillLevel, Role preferredRole, int personalityScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.personalityScore = personalityScore;
        this.personalityType = calculatePersonalityType(personalityScore);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPreferredGame() { return preferredGame; }
    public int getSkillLevel() { return skillLevel; }
    public Role getPreferredRole() { return preferredRole; }
    public int getPersonalityScore() { return personalityScore; }
    public PersonalityType getPersonalityType() { return personalityType; }

    // Static helper to classify type (used in constructor and survey)
    public static PersonalityType calculatePersonalityType(int score) {
        if (score >= 90) return PersonalityType.LEADER;
        else if (score >= 70) return PersonalityType.BALANCED;
        else return PersonalityType.THINKER;   // 50–69 (lower scores still Thinker)
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s - Skill:%d - %s - %s",
                name, id, preferredGame, skillLevel, preferredRole, personalityType);
    }
}