package model;

import enums.Role;
import interfaces.CsvPersistable;

import java.util.ArrayList;
import java.util.List;

/**
 * System administrator.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - managedTeamIds stores Team.id references (Strings), not Team objects.
 * - Admins are stored in admins.csv (separate from players.csv).
 * - Admin does NOT implement Rankable — only Players and Teams have ranks.
 */
public class Admin extends Person implements CsvPersistable {

    private List<String> managedTeamIds;   // Team.id references

    public Admin(String id, String username, String passwordHash,
                 String salt, String nickname) {
        super(id, username, passwordHash, salt, nickname);
        this.managedTeamIds = new ArrayList<>();
    }

    /* ---- role ---- */
    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    /* ---- helpers ---- */
    public void addManagedTeam(String teamId) {
        if (teamId != null && !managedTeamIds.contains(teamId)) {
            managedTeamIds.add(teamId);
        }
    }

    public void removeManagedTeam(String teamId) {
        managedTeamIds.remove(teamId);
    }

    /* ---- CsvPersistable ---- */
    @Override
    public String toCsvRow() {
        return String.join(",",
                getId(),
                getUsername(),
                getPasswordHash(),
                getSalt(),
                getNickname(),
                String.join(";", managedTeamIds));
    }

    /* ---- getters / setters ---- */
    public List<String> getManagedTeamIds() { return managedTeamIds; }
    public void setManagedTeamIds(List<String> managedTeamIds) { this.managedTeamIds = managedTeamIds; }

    @Override
    public String toString() {
        return String.format("Admin[%s] %s | Teams managed: %d",
                getId(), getNickname(), managedTeamIds.size());
    }
}
