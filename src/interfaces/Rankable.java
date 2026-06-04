package interfaces;

import enums.Rank;

/**
 * Contract for any class that participates in ranked ordering (leaderboards).
 * Implemented by Player and Team.
 */
public interface Rankable {
    /** @return numeric rank level for sorting (higher = better). */
    int getRankValue();

    /** @return the current Rank enum value. */
    Rank getRank();

    /** Replaces the current rank. */
    void setRank(Rank rank);
}
