package service;

import model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for leaderboard operations.
 *
 * Design decisions (from design.md §3.1, plan.md §2.6):
 * - Three ranking dimensions are supported: win rate, total wins, level.
 * - Each sort uses a composite comparator so ties are deterministic.
 * - The caller specifies how many results to return (e.g. top 10).
 */
public class LeaderboardService {

    private final List<Player> players;
    private final Map<String, Team> teamMap;

    public LeaderboardService(List<Player> players, List<Team> teams) {
        this.players = new ArrayList<>(players);
        this.teamMap = new HashMap<>();
        for (Team t : teams) teamMap.put(t.getId(), t);
    }

    /** @return team name, or "—" if player has no team. */
    public String getTeamName(String teamId) {
        if (teamId == null) return "—";
        Team t = teamMap.get(teamId);
        return t != null ? t.getName() : "—";
    }

    /* ---- ranking methods ---- */

    /**
     * Top N players by win rate.
     * Tie-breaking: win rate desc → level desc → nickname asc.
     */
    public List<Player> topByWinRate(int n) {
        return players.stream()
                .sorted(Comparator
                        .comparingDouble(Player::getWinRate).reversed()
                        .thenComparing(Comparator.comparingInt(Player::getLevel).reversed())
                        .thenComparing(p -> p.getNickname().toLowerCase()))
                .limit(Math.max(0, n))
                .collect(Collectors.toList());
    }

    /**
     * Top N players by total wins (winCount).
     * Tie-breaking: wins desc → win rate desc → nickname asc.
     */
    public List<Player> topByWins(int n) {
        return players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getWinCount).reversed()
                        .thenComparing(Comparator.comparingDouble(Player::getWinRate).reversed())
                        .thenComparing(p -> p.getNickname().toLowerCase()))
                .limit(Math.max(0, n))
                .collect(Collectors.toList());
    }

    /**
     * Top N players by account level.
     * Tie-breaking: level desc → win rate desc → nickname asc.
     */
    public List<Player> topByLevel(int n) {
        return players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getLevel).reversed()
                        .thenComparing(Comparator.comparingDouble(Player::getWinRate).reversed())
                        .thenComparing(p -> p.getNickname().toLowerCase()))
                .limit(Math.max(0, n))
                .collect(Collectors.toList());
    }
}
