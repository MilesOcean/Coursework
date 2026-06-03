package model;

import enums.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * System administrator who manages heroes, players, and teams.
 *
 * Design choices:
 * - Admin DOES NOT own heroes or have a rank — it is purely a management role.
 * - adminLevel indicates seniority (1 = junior, 2 = senior, etc.).
 * - managedTeams tracks which teams this admin oversees.
 * - Ban/unban delegates to Player.setBanned() so the logic stays on Player.
 */
public class Admin extends Person {

    private int adminLevel;
    private List<Team> managedTeams;

    public Admin(String id, String username, String salt,
                 String hashedPassword, String nickname,
                 int adminLevel) {
        super(id, username, salt, hashedPassword, nickname);
        this.adminLevel = adminLevel;
        this.managedTeams = new ArrayList<>();
    }

    /* ---- role ---- */
    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    /* ---- player management ---- */
    /** Disables a player account. The player's data is kept. */
    public void banPlayer(Player player) {
        if (player != null) {
            player.setBanned(true);
        }
    }

    /** Re-enables a previously banned player account. */
    public void unbanPlayer(Player player) {
        if (player != null) {
            player.setBanned(false);
        }
    }

    /* ---- team oversight ---- */
    /** Assigns a team to this admin's supervision list. */
    public void manageTeam(Team team) {
        if (team != null && !managedTeams.contains(team)) {
            managedTeams.add(team);
        }
    }

    /** Removes a team from supervision. */
    public void unmanageTeam(Team team) {
        managedTeams.remove(team);
    }

    /* ---- getters / setters ---- */
    public int getAdminLevel() { return adminLevel; }
    public void setAdminLevel(int adminLevel) { this.adminLevel = Math.max(1, adminLevel); }

    public List<Team> getManagedTeams() { return managedTeams; }
    public void setManagedTeams(List<Team> managedTeams) { this.managedTeams = managedTeams; }

    @Override
    public String toString() {
        return String.format("Admin[%s] %s | Level %d | Teams: %d",
                getId(), getNickname(), adminLevel, managedTeams.size());
    }
}
