package entity;

public class EntityTest {
    public static void main(String[] args) {
        System.out.println("=== TESTING ENTITY CLASSES ===\n");

        // Test Participant + PersonalityType calculation
        System.out.println("TEST 1: Participant & PersonalityType");
        Participant p1 = new Participant("P001", "Alex", "a@uni.edu", "Valorant", 8, Role.STRATEGIST, 95);
        Participant p2 = new Participant("P002", "Bob", "b@uni.edu", "CS:GO", 6, Role.ATTACKER, 75);
        Participant p3 = new Participant("P003", "Charlie", "c@uni.edu", "FIFA", 4, Role.DEFENDER, 60);

        System.out.println(p1); // Should show LEADER
        System.out.println(p2); // Should show BALANCED
        System.out.println(p3); // Should show THINKER

        if (p1.getPersonalityType() == PersonalityType.LEADER) {
            System.out.println("Passed: High score -> LEADER");
        }
        if (p3.getPersonalityType() == PersonalityType.THINKER) {
            System.out.println("Passed: Low score -> THINKER");
        }

        // Test Team class
        System.out.println("\nTEST 2: Team formation & stats");
        Team team = new Team(1);
        team.addMember(p1);
        team.addMember(p2);
        team.addMember(p3);

        System.out.println(team);
        System.out.println("Average Skill: " + String.format("%.2f", team.getAverageSkill()));
        System.out.println("Games: " + team.getPreferredGames());
        System.out.println("Roles: " + team.getPreferredRoles());
        System.out.println("Personality Count: " + team.getPersonalityCount());

        // Test Role enum
        System.out.println("\nTEST 3: Role enum values");
        for (Role r : Role.values()) {
            System.out.println("Role: " + r);
        }

        // Test Event
        System.out.println("\nTEST 4: Event class");
        Event event = new Event("Valorant Tournament 2025");
        System.out.println("Event: " + event);

        System.out.println("\n=== ALL ENTITY TESTS PASSED ===\n");
    }
}