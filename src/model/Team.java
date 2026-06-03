package model;

import enums.Rank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A group of up to 5 players who compete together.
 *
 * Design choices:
 * - "members" is an ArrayList so order is preserved (join order).
 * - Team.rank is computed on the fly from member ranks — not stored in CSV
 *   because it can always be recalculated.
 * - "captain" must be a member of the team. Changing the captain does not
 *   remove them from the members list.
 * - "isFull()" enforces the 5-player limit before addMember().
 * - The constructor automatically adds the captain to the members list.
 */
public class Team {

    private String id;
    private String name;
    private List<Player> members;
    private Player captain;
    private LocalDate creationDate;

    /**
     * @param id      UUID assigned by the caller.
     * @param name    Unique team name.
     * @param captain Founding captain — automatically joins as a member.
     */
    public Team(String id, String name, Player captain) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.creationDate = LocalDate.now();

        // Captain must be a member.
        if (captain != null) {
            this.captain = captain;
            this.members.add(captain);
        }
    }

    /* ---- membership ---- */
    /**
     * Adds a player to the team.
     * @return true if added, false if team is full or player is already in.
     */
    public boolean addMember(Player player) {
        if (player == null || isFull() || members.contains(player)) {
            return false;
        }
        members.add(player);
        player.joinTeam(this);
        return true;
    }

    /**
     * Removes a player from the team.
     * If the player was captain, the first remaining member becomes captain.
     * If the last member leaves, captain becomes null.
     */
    public boolean removeMember(Player player) {
        if (player == null || !members.contains(player)) {
            return false;
        }
        members.remove(player);
        player.leaveTeam();

        // Reassign captain if needed
        if (player.equals(captain)) {
            captain = members.isEmpty() ? null : members.get(0);
        }
        return true;
    }

    /**
     * Changes the team captain. The new captain must already be a member.
     * @return true if the change succeeded.
     */
    public boolean setCaptain(Player player) {
        if (player != null && members.contains(player)) {
            this.captain = player;
            return true;
        }
        return false;
    }

    /* ---- computed values ---- */

    /** @return true if the team has 5 members (max size). */
    public boolean isFull() {
        return members.size() >= 5;
    }

    /**
     * Computes the average rank level of all members, rounded down.
     * @return integer rank level (0–7), or 0 if the team has no members.
     */
    public int getAverageRankValue() {
        if (members.isEmpty()) return 0;
        int sum = 0;
        for (Player p : members) {
            sum += p.getRankValue();
        }
        return sum / members.size();
    }

    /**
     * Converts the average rank value back to a Rank enum.
     * Returns BRONZE if the team is empty.
     */
    public Rank getRank() {
        int avg = getAverageRankValue();
        for (Rank r : Rank.values()) {
            if (r.getLevel() == avg) return r;
        }
        return Rank.BRONZE;
    }

    /** Computes the average account level of all members. */
    public double getAverageLevel() {
        if (members.isEmpty()) return 0.0;
        int sum = 0;
        for (Player p : members) {
            sum += p.getLevel();
        }
        return (double) sum / members.size();
    }

    /** @return total matches played by all members combined. */
    public int getTotalMatches() {
        int total = 0;
        for (Player p : members) {
            total += p.getTotalGames();
        }
        return total;
    }

    /** @return the member with the highest win rate (ties broken by level, then name). */
    public Player getTopPlayer() {
        if (members.isEmpty()) return null;
        Player top = members.get(0);
        for (Player p : members) {
            if (p.getWinRate() > top.getWinRate()
                    || (p.getWinRate() == top.getWinRate()
                        && p.getLevel() > top.getLevel())
                    || (p.getWinRate() == top.getWinRate()
                        && p.getLevel() == top.getLevel()
                        && p.getNickname().compareTo(top.getNickname()) < 0)) {
                top = p;
            }
        }
        return top;
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Player> getMembers() { return members; }
    public void setMembers(List<Player> members) { this.members = members; }

    public Player getCaptain() { return captain; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    @Override
    public String toString() {
        return String.format("Team[%s] %s | Members: %d/5 | Rank: %s | Captain: %s",
                getId(), name, members.size(), getRank(),
                captain != null ? captain.getNickname() : "none");
    }
}
