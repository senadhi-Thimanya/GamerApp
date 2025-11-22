package entity;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    private int teamId;
    private List<Participant> members = new ArrayList<>();

    public Team(int teamId) {
        this.teamId = teamId;
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() { return members; }
    public int getSize() { return members.size(); }
    public int getTeamId() { return teamId; }

    public double getAverageSkill() {
        return members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
    }

    public Set<String> getPreferredGames() {
        return members.stream()
                .map(Participant::getPreferredGame)
                .collect(Collectors.toSet());
    }

    public Set<Role> getPreferredRoles() {
        return members.stream()
                .map(Participant::getPreferredRole)
                .collect(Collectors.toSet());
    }

    public Map<PersonalityType, Long> getPersonalityCount() {
        return members.stream()
                .collect(Collectors.groupingBy(Participant::getPersonalityType, Collectors.counting()));
    }

    // Validation methods used by the algorithm
    public boolean hasGoodGameDiversity() {
        return members.stream()
                .collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()))
                .values()
                .stream()
                .noneMatch(count -> count > 2);   // max 2 per game
    }

    public boolean hasRoleDiversity(int teamSize) {
        int requiredRoles = teamSize > 5 ? 4 : 3;
        return getPreferredRoles().size() >= requiredRoles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== entity.Team ").append(teamId).append(" (Avg Skill: ")
                .append(String.format("%.2f", getAverageSkill())).append(")\n");
        members.forEach(p -> sb.append("  - ").append(p.toString()).append("\n"));
        return sb.toString();
    }
}