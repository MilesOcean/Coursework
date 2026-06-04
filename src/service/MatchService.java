package service;

import model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for match history operations.
 *
 * Design decisions (from design.md §3.4, plan.md §2.5):
 * - getByTeamId() returns matches where the given team is team1 OR team2.
 * - Matches are always sorted by date descending (most recent first).
 * - All ID-to-name resolution uses lookup maps built at construction.
 */
public class MatchService {

    private final List<MatchRecord> matches;
    private final Map<String, Team>   teamMap;     // teamId   → Team
    private final Map<String, Player> playerMap;   // playerId → Player
    private final Map<String, Hero>   heroMap;     // heroId   → Hero

    public MatchService(List<MatchRecord> matches, List<Team> teams,
                        List<Player> players, List<Hero> heroes) {
        this.matches = new ArrayList<>(matches);
        // Sort most recent first
        this.matches.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        this.teamMap = new HashMap<>();
        for (Team t : teams) teamMap.put(t.getId(), t);

        this.playerMap = new HashMap<>();
        for (Player p : players) playerMap.put(p.getId(), p);

        this.heroMap = new HashMap<>();
        for (Hero h : heroes) heroMap.put(h.getId(), h);
    }

    /* ---- queries ---- */

    /** All matches, most recent first. */
    public List<MatchRecord> listAll() {
        return Collections.unmodifiableList(matches);
    }

    /** Exact match on match ID. */
    public Optional<MatchRecord> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        for (MatchRecord m : matches) {
            if (m.getId().equals(id.trim())) return Optional.of(m);
        }
        return Optional.empty();
    }

    /** Adds a match and re-sorts descending by date. */
    public void addMatch(MatchRecord m) {
        if (m != null) {
            matches.add(m);
            matches.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        }
    }

    /** Removes a match by ID. @return true if found and removed. */
    public boolean deleteMatch(String matchId) {
        if (matchId == null) return false;
        Optional<MatchRecord> opt = findById(matchId);
        if (opt.isEmpty()) return false;
        matches.remove(opt.get());
        return true;
    }

    /** Matches where the given team participated (as team1 or team2). */
    public List<MatchRecord> getByTeamId(String teamId) {
        if (teamId == null || teamId.isBlank()) return listAll();
        return matches.stream()
                .filter(m -> teamId.equals(m.getTeam1Id()) || teamId.equals(m.getTeam2Id()))
                .collect(Collectors.toList());
    }

    /** Matches where the given player participated (via heroPicks key set). */
    public List<MatchRecord> getByPlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) return listAll();
        return matches.stream()
                .filter(m -> m.getHeroPicks().containsKey(playerId))
                .collect(Collectors.toList());
    }

    /** Convenience: search team by name, return its matches. */
    public List<MatchRecord> getByTeamName(String name) {
        Team t = findTeamByName(name);
        if (t == null) return Collections.emptyList();
        return getByTeamId(t.getId());
    }

    /* ---- search helpers ---- */
    private Team findTeamByName(String name) {
        if (name == null || name.isBlank()) return null;
        String lower = name.trim().toLowerCase();
        for (Team t : teamMap.values()) {
            if (t.getName().toLowerCase().equals(lower)) return t;
        }
        return null;
    }

    public Optional<Team> findTeam(String name) {
        return Optional.ofNullable(findTeamByName(name));
    }

    /* ---- ID → name resolution ---- */

    public String getTeamName(String teamId) {
        Team t = teamMap.get(teamId);
        return t != null ? t.getName() : teamId;
    }

    public String getPlayerName(String playerId) {
        Player p = playerMap.get(playerId);
        return p != null ? p.getNickname() : playerId;
    }

    public String getHeroName(String heroId) {
        Hero h = heroMap.get(heroId);
        return h != null ? h.getName() : heroId;
    }

    public String getMvpName(MatchRecord m) {
        return m.getMvpPlayerId() != null ? getPlayerName(m.getMvpPlayerId()) : "—";
    }

    /** @return the winning team name, or "DRAW" if tied. */
    public String getWinnerName(MatchRecord m) {
        String wid = m.getWinnerId();
        return wid != null ? getTeamName(wid) : "DRAW";
    }

    /* ---- formatting ---- */

    /** Formats heroPicks as "PlayerName→HeroName" pairs. */
    public String formatHeroPicks(MatchRecord m) {
        if (m.getHeroPicks().isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : m.getHeroPicks().entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(getPlayerName(e.getKey()))
              .append("→")
              .append(getHeroName(e.getValue()));
        }
        return sb.toString();
    }

    /**
     * Calculates hero pick rate across a set of matches.
     * @return map of heroId → pick rate (0.0 – 100.0).
     */
    public Map<String, Double> getHeroPickRate(List<MatchRecord> matches) {
        Map<String, Integer> pickCounts = new HashMap<>();
        int totalPicks = 0;
        for (MatchRecord m : matches) {
            for (String heroId : m.getHeroPicks().values()) {
                pickCounts.merge(heroId, 1, Integer::sum);
                totalPicks++;
            }
        }
        Map<String, Double> rates = new LinkedHashMap<>();
        if (totalPicks == 0) return rates;
        // Sort by count desc, then hero name asc
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(pickCounts.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            if (cmp != 0) return cmp;
            return getHeroName(a.getKey()).compareTo(getHeroName(b.getKey()));
        });
        for (Map.Entry<String, Integer> e : sorted) {
            rates.put(e.getKey(), e.getValue() * 100.0 / totalPicks);
        }
        return rates;
    }

    /**
     * Counts wins and losses for a given team across a set of matches.
     * @return int[]{wins, losses, draws}
     */
    public int[] countWinLoss(String teamId, List<MatchRecord> matches) {
        int wins = 0, losses = 0, draws = 0;
        for (MatchRecord m : matches) {
            if (m.getResult() == enums.MatchResult.DRAW) {
                draws++;
            } else if (teamId.equals(m.getWinnerId())) {
                wins++;
            } else {
                losses++;
            }
        }
        return new int[]{wins, losses, draws};
    }

    /** Summarises result as "TeamA W — TeamB" or "DRAW". */
    public String formatResult(MatchRecord m) {
        switch (m.getResult()) {
            case WIN:  return getTeamName(m.getTeam1Id()) + " W — " + getTeamName(m.getTeam2Id()) + " L";
            case LOSE: return getTeamName(m.getTeam2Id()) + " W — " + getTeamName(m.getTeam1Id()) + " L";
            default:   return getTeamName(m.getTeam1Id()) + " — " + getTeamName(m.getTeam2Id()) + " DRAW";
        }
    }
}
