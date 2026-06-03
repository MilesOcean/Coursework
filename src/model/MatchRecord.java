package model;

import enums.MatchResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Historical record of a single match between two teams.
 *
 * Design choices:
 * - "result" is always from team1's perspective:
 *     WIN  → team1 won, team2 lost
 *     LOSE → team1 lost, team2 won
 *     DRAW → both tied
 * - "participants" is a Map<Player, Hero> so you can look up which hero
 *   any given player used in this match.
 * - "mvp" is the most valuable player of the match (single reference).
 * - No direct reference to "the winner team" is stored; getWinner() /
 *   getLoser() compute it from result + team1 + team2.
 */
public class MatchRecord {

    private String id;
    private LocalDateTime matchDate;
    private Team team1;
    private Team team2;
    private MatchResult result;                  // relative to team1
    private int duration;                        // match length in seconds
    private Player mvp;                          // most valuable player
    private Map<Player, Hero> participants;      // which hero each player used

    /**
     * @param id        UUID assigned by the caller.
     * @param team1     First team.
     * @param team2     Second team.
     * @param result    Outcome from team1's perspective.
     * @param duration  Match length in seconds (≥ 0).
     * @param mvp       Most valuable player (can be from either team).
     */
    public MatchRecord(String id, Team team1, Team team2,
                        MatchResult result, int duration, Player mvp) {
        this.id = id;
        this.matchDate = LocalDateTime.now();
        this.team1 = team1;
        this.team2 = team2;
        this.result = result;
        this.duration = Math.max(0, duration);
        this.mvp = mvp;
        this.participants = new HashMap<>();
    }

    /* ---- computed helpers ---- */

    /**
     * @return the winning Team, or null if the match was a draw.
     */
    public Team getWinner() {
        switch (result) {
            case WIN:  return team1;
            case LOSE: return team2;
            default:   return null;   // DRAW
        }
    }

    /**
     * @return the losing Team, or null if the match was a draw.
     */
    public Team getLoser() {
        switch (result) {
            case WIN:  return team2;
            case LOSE: return team1;
            default:   return null;   // DRAW
        }
    }

    /** Looks up which hero a specific player used in this match. */
    public Hero getHeroFor(Player player) {
        return participants.get(player);
    }

    /** Convenience: records a player-hero pairing for this match. */
    public void addParticipant(Player player, Hero hero) {
        if (player != null && hero != null) {
            participants.put(player, hero);
        }
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = Math.max(0, duration); }

    public Player getMvp() { return mvp; }
    public void setMvp(Player mvp) { this.mvp = mvp; }

    public Map<Player, Hero> getParticipants() { return participants; }
    public void setParticipants(Map<Player, Hero> participants) { this.participants = participants; }

    @Override
    public String toString() {
        return String.format("Match[%s] %s vs %s | %s | MVP: %s",
                getId(),
                team1 != null ? team1.getName() : "?",
                team2 != null ? team2.getName() : "?",
                result,
                mvp != null ? mvp.getNickname() : "none");
    }
}
