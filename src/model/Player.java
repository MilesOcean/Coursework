package model;

import enums.Rank;
import enums.Role;
import interfaces.CsvPersistable;
import interfaces.Rankable;

import java.util.*;

/**
 * A game player.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - heroPool stores hero IDs (Strings), not Hero objects — avoids coupling.
 * - equippedItems maps heroId → equipmentIds for the loadout system.
 * - teamId is a String reference to Team.id.
 * - matchCount replaces the old "totalGames"; winCount ≤ matchCount always.
 */
public class Player extends Person implements Rankable, CsvPersistable {

    private int level;
    private Rank rank;
    private int winCount;
    private int matchCount;
    private List<String> heroPool;                      // hero IDs
    private Map<String, List<String>> equippedItems;    // heroId → equipmentIds
    private String teamId;                              // Team.id, nullable

    public Player(String id, String username, String passwordHash,
                  String salt, String nickname) {
        super(id, username, passwordHash, salt, nickname);
        this.level = 1;
        this.rank = Rank.BRONZE;
        this.winCount = 0;
        this.matchCount = 0;
        this.heroPool = new ArrayList<>();
        this.equippedItems = new HashMap<>();
        this.teamId = null;
    }

    /* ---- role ---- */
    @Override
    public Role getRole() {
        return Role.PLAYER;
    }

    /* ---- computed ---- */
    /** @return win rate as percentage (0–100). Returns 0 if no matches played. */
    public double getWinRate() {
        if (matchCount == 0) return 0.0;
        return (double) winCount / matchCount * 100.0;
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

    /* ---- helpers ---- */
    public void addHero(String heroId) {
        if (heroId != null && !heroPool.contains(heroId)) {
            heroPool.add(heroId);
        }
    }

    public void removeHero(String heroId) {
        heroPool.remove(heroId);
    }

    /** Equip items to a specific hero. Replaces any previous loadout for that hero. */
    public void setEquippedItems(String heroId, List<String> equipmentIds) {
        if (heroId != null && equipmentIds != null) {
            equippedItems.put(heroId, equipmentIds);
        }
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
                String.valueOf(level),
                rank.name(),
                String.valueOf(winCount),
                String.valueOf(matchCount),
                teamId != null ? teamId : "");
    }

    /* ---- getters / setters ---- */
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public int getWinCount() { return winCount; }
    public void setWinCount(int winCount) { this.winCount = winCount; }

    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }

    public List<String> getHeroPool() { return heroPool; }
    public void setHeroPool(List<String> heroPool) { this.heroPool = heroPool; }

    public Map<String, List<String>> getEquippedItems() { return equippedItems; }
    public void setEquippedItems(Map<String, List<String>> equippedItems) { this.equippedItems = equippedItems; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    @Override
    public String toString() {
        return String.format("Player[%s] %s | Lv.%d | %s | WinRate %.1f%% | Team: %s",
                getId(), getNickname(), level, rank, getWinRate(),
                teamId != null ? teamId : "none");
    }
}
