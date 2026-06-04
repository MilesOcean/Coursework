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
                teamId != null ? teamId : "",
                String.join(";", heroPool));
    }

    /**
     * Parses a CSV row into a Player.
     * Format: id,username,passwordHash,salt,nickname,level,rank,winCount,matchCount,teamId,heroPool
     * @return Player or null if the row is malformed.
     */
    public static Player fromCsvRow(String[] fields) {
        try {
            if (fields.length < 10) return null;
            String id = fields[0];
            String username = fields[1];
            String passwordHash = fields[2];
            String salt = fields[3];
            String nickname = fields[4];
            int level = Integer.parseInt(fields[5]);
            Rank rank = Rank.valueOf(fields[6]);
            int winCount = Integer.parseInt(fields[7]);
            int matchCount = Integer.parseInt(fields[8]);
            String teamId = fields[9].isEmpty() ? null : fields[9];

            Player p = new Player(id, username, passwordHash, salt, nickname);
            p.setLevel(level);
            p.setRank(rank);
            p.setWinCount(winCount);
            p.setMatchCount(matchCount);
            p.setTeamId(teamId);

            // heroPool (field 10, optional)
            if (fields.length >= 11 && !fields[10].isEmpty()) {
                for (String hid : fields[10].split(";")) {
                    if (!hid.isEmpty()) p.addHero(hid);
                }
            }
            return p;
        } catch (Exception e) {
            return null;
        }
    }

    /* ---- getters / setters ---- */
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public int getWinCount() { return winCount; }
    public void setWinCount(int winCount) { this.winCount = winCount; }

    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }

    public List<String> getHeroPool() { return Collections.unmodifiableList(heroPool); }
    public void setHeroPool(List<String> heroPool) { this.heroPool = new ArrayList<>(heroPool); }

    public Map<String, List<String>> getEquippedItems() { return Collections.unmodifiableMap(equippedItems); }
    public void setEquippedItems(Map<String, List<String>> equippedItems) {
        this.equippedItems = new HashMap<>();
        if (equippedItems != null) {
            for (Map.Entry<String, List<String>> e : equippedItems.entrySet()) {
                this.equippedItems.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        }
    }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    @Override
    public String toString() {
        return String.format("Player[%s] %s | Lv.%d | %s | WinRate %.1f%% | Team: %s",
                getId(), getNickname(), level, rank, getWinRate(),
                teamId != null ? teamId : "none");
    }
}
