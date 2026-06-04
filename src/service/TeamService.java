package service;

import model.*;

import java.util.*;

/**
 * Business logic for team-related operations.
 *
 * Design decisions (from design.md §2.4, plan.md §2.2):
 * - Stores a Map<String, Player> to resolve memberIds without scanning the list.
 * - Total wins / total matches are computed by summing member stats.
 * - Delegates getAverageLevel() and getTopPlayer() to the Team model
 *   (which receives the resolved Player list).
 */
public class TeamService {

    private final List<Team> teams;
    private final Map<String, Player> playerMap;   // playerId → Player

    public TeamService(List<Team> teams, List<Player> players) {
        this.teams = teams;
        this.playerMap = new HashMap<>();
        for (Player p : players) {
            playerMap.put(p.getId(), p);
        }
    }

    /* ---- search ---- */

    public Optional<Team> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        for (Team t : teams) {
            if (t.getId().equals(id.trim())) return Optional.of(t);
        }
        return Optional.empty();
    }

    /** Case-insensitive match on team name. */
    public Optional<Team> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.trim().toLowerCase();
        for (Team t : teams) {
            if (t.getName().toLowerCase().equals(lower)) return Optional.of(t);
        }
        return Optional.empty();
    }

    public List<Team> listAll() {
        return Collections.unmodifiableList(teams);
    }

    /* ---- member resolution ---- */

    /** Resolves memberIds to Player objects. */
    public List<Player> getMembers(Team team) {
        List<Player> result = new ArrayList<>();
        for (String pid : team.getMemberIds()) {
            Player p = playerMap.get(pid);
            if (p != null) result.add(p);
        }
        return result;
    }

    /** @return the captain's nickname, or "—" if unknown. */
    public String getCaptainName(Team team) {
        if (team.getCaptainId() == null) return "—";
        Player p = playerMap.get(team.getCaptainId());
        return p != null ? p.getNickname() : "—";
    }

    /** @return a player's nickname, or the ID itself if unknown. */
    public String getPlayerName(String playerId) {
        Player p = playerMap.get(playerId);
        return p != null ? p.getNickname() : playerId;
    }

    /* ---- team stats (computed from members) ---- */

    /** Sum of all member winCounts. */
    public int getTotalWins(Team team) {
        int sum = 0;
        for (Player p : getMembers(team)) sum += p.getWinCount();
        return sum;
    }

    /** Sum of all member matchCounts. */
    public int getTotalMatches(Team team) {
        int sum = 0;
        for (Player p : getMembers(team)) sum += p.getMatchCount();
        return sum;
    }

    /**
     * Team win rate = total member wins / total member matches.
     * Returns 0 if no matches have been played.
     */
    public double getTeamWinRate(Team team) {
        int wins = getTotalWins(team);
        int total = getTotalMatches(team);
        if (total == 0) return 0.0;
        return (double) wins / total * 100.0;
    }
}
