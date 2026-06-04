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

    /** Summarises result as "TeamA W — TeamB" or "DRAW". */
    public String formatResult(MatchRecord m) {
        switch (m.getResult()) {
            case WIN:  return getTeamName(m.getTeam1Id()) + " W — " + getTeamName(m.getTeam2Id()) + " L";
            case LOSE: return getTeamName(m.getTeam2Id()) + " W — " + getTeamName(m.getTeam1Id()) + " L";
            default:   return getTeamName(m.getTeam1Id()) + " — " + getTeamName(m.getTeam2Id()) + " DRAW";
        }
    }
}
