package model;

import enums.MatchResult;
import interfaces.CsvPersistable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Historical record of a single match between two teams.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - All references use String IDs (team1Id, team2Id, mvpPlayerId), not objects.
 * - heroPicks maps playerId → heroId for lookup of who played what.
 * - date is a LocalDate (stored as YYYY-MM-DD in CSV).
 * - durationMinutes is in minutes (not seconds).
 * - result is always from team1's perspective.
 */
public class MatchRecord implements CsvPersistable {

    private String id;
    private LocalDate date;
    private String team1Id;
    private String team2Id;
    private MatchResult result;                  // relative to team1
    private Map<String, String> heroPicks;       // playerId → heroId
    private String mvpPlayerId;
    private int durationMinutes;

    public MatchRecord(String id, LocalDate date, String team1Id, String team2Id,
                       MatchResult result, String mvpPlayerId, int durationMinutes) {
        this.id = id;
        this.date = date;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.result = result;
        this.mvpPlayerId = mvpPlayerId;
        this.durationMinutes = Math.max(0, durationMinutes);
        this.heroPicks = new HashMap<>();
    }

    /* ---- derived getters ---- */
    /** @return the winning team's ID, or null on DRAW. */
    public String getWinnerId() {
        switch (result) {
            case WIN:  return team1Id;
            case LOSE: return team2Id;
            default:   return null;
        }
    }

    /** @return the losing team's ID, or null on DRAW. */
    public String getLoserId() {
        switch (result) {
            case WIN:  return team2Id;
            case LOSE: return team1Id;
            default:   return null;
        }
    }

    /* ---- helpers ---- */
    public void addHeroPick(String playerId, String heroId) {
        if (playerId != null && heroId != null) {
            heroPicks.put(playerId, heroId);
        }
    }

    /* ---- CsvPersistable ---- */
    @Override
    public String toCsvRow() {
        // heroPicks formatted as: team1:p1:h1;p2:h2|team2:p3:h3;p4:h4
        // Simplified: just flatten to playerId:heroId pairs separated by ;
        StringBuilder picks = new StringBuilder();
        for (Map.Entry<String, String> e : heroPicks.entrySet()) {
            if (picks.length() > 0) picks.append(";");
            picks.append(e.getKey()).append(":").append(e.getValue());
        }
        return String.join(",",
                id,
                date.toString(),
                team1Id,
                team2Id,
                result.name(),
                mvpPlayerId != null ? mvpPlayerId : "",
                String.valueOf(durationMinutes),
                picks.toString());
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTeam1Id() { return team1Id; }
    public void setTeam1Id(String team1Id) { this.team1Id = team1Id; }

    public String getTeam2Id() { return team2Id; }
    public void setTeam2Id(String team2Id) { this.team2Id = team2Id; }

    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }

    public Map<String, String> getHeroPicks() { return heroPicks; }
    public void setHeroPicks(Map<String, String> heroPicks) { this.heroPicks = heroPicks; }

    public String getMvpPlayerId() { return mvpPlayerId; }
    public void setMvpPlayerId(String mvpPlayerId) { this.mvpPlayerId = mvpPlayerId; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = Math.max(0, durationMinutes); }

    @Override
    public String toString() {
        return String.format("Match[%s] %s | %s vs %s | %s | MVP: %s | %d min",
                id, date, team1Id, team2Id, result,
                mvpPlayerId != null ? mvpPlayerId : "none", durationMinutes);
    }
}
