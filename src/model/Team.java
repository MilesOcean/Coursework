package model;

import enums.Rank;
import interfaces.CsvPersistable;
import interfaces.Rankable;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of players who compete together.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - All relationships use String IDs (memberIds, captainId), not object refs.
 *   This keeps the model independent of the service layer.
 * - "rank" is a stored field (persisted in CSV), not computed from members.
 * - getAverageLevel() and getTopPlayer() accept a List<Player> parameter
 *   because the Team only stores IDs — the caller (TeamService) provides
 *   the actual Player objects for computation.
 */
public class Team implements Rankable, CsvPersistable {

    private String id;
    private String name;
    private String captainId;           // Player.id
    private Rank rank;
    private List<String> memberIds;     // Player.id references

    public Team(String id, String name, String captainId, Rank rank) {
        this.id = id;
        this.name = name;
        this.captainId = captainId;
        this.rank = rank;
        this.memberIds = new ArrayList<>();
        // Captain is also a member.
        if (captainId != null && !captainId.isBlank()) {
            memberIds.add(captainId);
        }
    }

    /* ---- membership ---- */
    public boolean addMember(String playerId) {
        if (playerId == null || isFull() || memberIds.contains(playerId)) {
            return false;
        }
        memberIds.add(playerId);
        return true;
    }

    public boolean removeMember(String playerId) {
        if (playerId == null || !memberIds.contains(playerId)) {
            return false;
        }
        // Cannot remove the captain unless they are the last member.
        if (playerId.equals(captainId) && memberIds.size() > 1) {
            return false;   // transfer captaincy first
        }
        memberIds.remove(playerId);
        if (memberIds.isEmpty()) {
            captainId = null;
        } else if (playerId.equals(captainId)) {
            captainId = memberIds.get(0);
        }
        return true;
    }

    public boolean setCaptain(String playerId) {
        if (playerId != null && memberIds.contains(playerId)) {
            this.captainId = playerId;
            return true;
        }
        return false;
    }

    public boolean isFull() {
        return memberIds.size() >= 5;
    }

    /* ---- computed (require Player data from caller) ---- */

    /**
     * Computes average level of the given members.
     * @param members Player objects matching this team's memberIds (provided by TeamService).
     */
    public double getAverageLevel(List<Player> members) {
        if (members == null || members.isEmpty()) return 0.0;
        int sum = 0;
        for (Player p : members) {
            sum += p.getLevel();
        }
        return (double) sum / members.size();
    }

    /**
     * Finds the top player among the given members.
     * Tie-breaking: win rate desc → level desc → nickname asc.
     * @param members Player objects matching this team's memberIds.
     * @return the top Player, or null if the list is empty.
     */
    public Player getTopPlayer(List<Player> members) {
        if (members == null || members.isEmpty()) return null;
        Player top = members.get(0);
        for (Player p : members) {
            if (p.getWinRate() > top.getWinRate()
                    || (p.getWinRate() == top.getWinRate() && p.getLevel() > top.getLevel())
                    || (p.getWinRate() == top.getWinRate() && p.getLevel() == top.getLevel()
                        && p.getNickname().compareToIgnoreCase(top.getNickname()) < 0)) {
                top = p;
            }
        }
        return top;
    }

    /* ---- Rankable implementation ---- */
    @Override
    public int getRankValue() {
        return rank.getLevel();
    }

    @Override
    public Rank getRank() {
        return rank;
    }

    @Override
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    /* ---- CsvPersistable ---- */
    @Override
    public String toCsvRow() {
        return String.join(",",
                id,
                name,
                captainId != null ? captainId : "",
                rank.name(),
                String.join(";", memberIds));
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCaptainId() { return captainId; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    @Override
    public String toString() {
        return String.format("Team[%s] %s | Rank: %s | Members: %d/5 | Captain: %s",
                id, name, rank, memberIds.size(),
                captainId != null ? captainId : "none");
    }
}
