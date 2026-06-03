package model;

import enums.Rank;
import enums.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * A game player who owns heroes, may join a team, and plays matches.
 *
 * Design choices:
 * - Collections (heroPool, ownedEquipment, matchHistory) start as empty
 *   ArrayLists — no null checks needed later.
 * - "team" is nullable because a player may not be in any team.
 * - "level" represents account progression (earned through play),
 *   separate from competitive "rank".
 * - "banned" flag lets Admin disable an account without deleting data.
 */
public class Player extends Person {

    private Rank rank;
    private int level;                          // account level (1+)
    private List<Hero> heroPool;                // heroes the player owns
    private List<Equipment> ownedEquipment;     // equipment inventory
    private List<MatchRecord> matchHistory;     // all past matches
    private Team team;                          // current team (nullable)
    private boolean banned;
    private int winCount;
    private int totalGames;

    /**
     * Creates a new Player with default values (BRONZE, level 1, empty pools).
     * Supplied fields come from AuthService after password hashing.
     */
    public Player(String id, String username, String salt,
                  String hashedPassword, String nickname) {
        super(id, username, salt, hashedPassword, nickname);

        this.rank = Rank.BRONZE;
        this.level = 1;
        this.heroPool = new ArrayList<>();
        this.ownedEquipment = new ArrayList<>();
        this.matchHistory = new ArrayList<>();
        this.team = null;
        this.banned = false;
        this.winCount = 0;
        this.totalGames = 0;
    }

    /* ---- role ---- */
    @Override
    public Role getRole() {
        return Role.PLAYER;
    }

    /* ---- hero management ---- */
    /** Adds a hero to the player's pool (e.g. after purchase). */
    public void addHero(Hero hero) {
        if (hero != null && !heroPool.contains(hero)) {
            heroPool.add(hero);
        }
    }

    /** Removes a hero from the player's pool. */
    public void removeHero(Hero hero) {
        heroPool.remove(hero);
    }

    /* ---- team management ---- */
    /** Assigns the player to a team. The team itself manages its member list. */
    public void joinTeam(Team team) {
        this.team = team;
    }

    /** Removes the player from their current team. */
    public void leaveTeam() {
        this.team = null;
    }

    /* ---- stats ---- */
    /**
     * @return win rate as a percentage (0–100). Returns 0 if no games played.
     */
    public double getWinRate() {
        if (totalGames == 0) return 0.0;
        return (double) winCount / totalGames * 100.0;
    }

    /** Records a win — called by MatchService when a match is recorded. */
    public void addWin() {
        winCount++;
        totalGames++;
    }

    /** Records a loss — called by MatchService. */
    public void addLoss() {
        totalGames++;
    }

    /** For Rankable / leaderboard sorting. */
    public int getRankValue() {
        return rank.getLevel();
    }

    /* ---- ban ---- */
    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    /* ---- getters / setters ---- */
    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public List<Hero> getHeroPool() { return heroPool; }
    public void setHeroPool(List<Hero> heroPool) { this.heroPool = heroPool; }

    public List<Equipment> getOwnedEquipment() { return ownedEquipment; }
    public void setOwnedEquipment(List<Equipment> ownedEquipment) { this.ownedEquipment = ownedEquipment; }

    public List<MatchRecord> getMatchHistory() { return matchHistory; }
    public void setMatchHistory(List<MatchRecord> matchHistory) { this.matchHistory = matchHistory; }

    public Team getTeam() { return team; }

    public int getWinCount() { return winCount; }
    public void setWinCount(int winCount) { this.winCount = winCount; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    /** Friendly display. */
    @Override
    public String toString() {
        return String.format("Player[%s] %s | Level %d | %s | WinRate %.1f%%",
                getId(), getNickname(), level, rank, getWinRate());
    }
}
