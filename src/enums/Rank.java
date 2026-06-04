package enums;

/**
 * Represents a player's (or team's) competitive rank tier.
 * Each rank has an integer level for easy comparison (higher = better).
 */
public enum Rank {
    BRONZE(0),
    SILVER(1),
    GOLD(2),
    PLATINUM(3),
    DIAMOND(4),
    MASTER(5),
    GRANDMASTER(6),
    LEGEND(7);

    private final int level;

    /**
     * @param level  Numeric tier used for comparison (0 = lowest, 7 = highest).
     */
    Rank(int level) {
        this.level = level;
    }

    /** @return The numeric tier (0–7), useful for sorting leaderboards. */
    public int getLevel() {
        return level;
    }
}
