package service;

import model.*;

import java.util.*;

/**
 * Business logic for player-related operations.
 *
 * Design decisions (from design.md §2.4, plan.md §2.1):
 * - findById / findByName return Optional to force the caller to handle "not found".
 * - ID-to-name resolution lives here so the CLI never touches model internals.
 * - Constructor accepts all entity lists because the service needs cross-references
 *   (team names, hero names, equipment names) for display.
 */
public class PlayerService {

    private final List<Player> players;
    private final Map<String, Hero>      heroMap;     // heroId   → Hero
    private final Map<String, Equipment> equipMap;    // equipId  → Equipment
    private final Map<String, Team>      teamMap;     // teamId   → Team

    /**
     * @param players   All players (from DataInitializer or FileService).
     * @param heroes    All heroes — used to resolve hero IDs to names.
     * @param equipment All equipment — used to resolve equipment IDs to names.
     * @param teams     All teams — used to resolve team IDs to names.
     */
    public PlayerService(List<Player> players, List<Hero> heroes,
                         List<Equipment> equipment, List<Team> teams) {
        this.players = players;

        this.heroMap = new HashMap<>();
        for (Hero h : heroes) heroMap.put(h.getId(), h);

        this.equipMap = new HashMap<>();
        for (Equipment e : equipment) equipMap.put(e.getId(), e);

        this.teamMap = new HashMap<>();
        for (Team t : teams) teamMap.put(t.getId(), t);
    }

    /* ---- search ---- */

    /** Exact match on player ID (UUID string). */
    public Optional<Player> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        for (Player p : players) {
            if (p.getId().equals(id.trim())) return Optional.of(p);
        }
        return Optional.empty();
    }

    /** Case-insensitive match on nickname or username. */
    public Optional<Player> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.trim().toLowerCase();
        for (Player p : players) {
            if (p.getNickname().toLowerCase().equals(lower)
                    || p.getUsername().toLowerCase().equals(lower)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    /** Returns all players (unmodifiable). */
    public List<Player> listAll() {
        return Collections.unmodifiableList(players);
    }

    /** Updates a player's nickname. Returns false if player not found. */
    public boolean updateNickname(String playerId, String newNickname) {
        if (playerId == null || newNickname == null || newNickname.isBlank()) return false;
        Optional<Player> opt = findById(playerId);
        if (opt.isPresent()) {
            opt.get().setNickname(newNickname.trim());
            return true;
        }
        return false;
    }

    /* ---- ID → name resolution (for display) ---- */

    /** @return team name, or "No Team" if teamId is null/unknown. */
    public String getTeamName(String teamId) {
        if (teamId == null) return "No Team";
        Team t = teamMap.get(teamId);
        return t != null ? t.getName() : "Unknown Team";
    }

    /** @return hero name, or the ID itself if unknown. */
    public String getHeroName(String heroId) {
        Hero h = heroMap.get(heroId);
        return h != null ? h.getName() : heroId;
    }

    /** @return equipment name, or the ID itself if unknown. */
    public String getEquipmentName(String equipId) {
        Equipment e = equipMap.get(equipId);
        return e != null ? e.getName() : equipId;
    }

    /**
     * Converts a list of hero IDs to a comma-separated string of hero names.
     */
    public String formatHeroPool(List<String> heroIds) {
        if (heroIds == null || heroIds.isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (String hid : heroIds) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(getHeroName(hid));
        }
        return sb.toString();
    }

    /**
     * Converts equippedItems map to a formatted string per hero.
     * Each line: "  HeroName → Equip1, Equip2, ..."
     */
    public String formatEquippedItems(Map<String, List<String>> equippedItems) {
        if (equippedItems == null || equippedItems.isEmpty()) return "  (no loadouts)";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : equippedItems.entrySet()) {
            String heroName = getHeroName(entry.getKey());
            sb.append("  ").append(heroName).append(" → ");
            List<String> equipIds = entry.getValue();
            if (equipIds == null || equipIds.isEmpty()) {
                sb.append("(none)");
            } else {
                for (int i = 0; i < equipIds.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(getEquipmentName(equipIds.get(i)));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
