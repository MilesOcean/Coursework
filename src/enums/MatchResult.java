package enums;

/**
 * Represents the outcome of a match from the perspective of team1.
 * Stored inside MatchRecord.
 */
public enum MatchResult {
    WIN,   // team1 won the match
    LOSE,  // team1 lost the match
    DRAW   // The match ended in a tie (rare)
}
