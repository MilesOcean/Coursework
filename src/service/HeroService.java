package service;

import model.*;

import java.util.*;

/**
 * Business logic for hero-related operations.
 *
 * Design decisions (from design.md §2.4, plan.md §2.3):
 * - getOwners() scans all players' heroPools to find who owns a given hero.
 * - Equipment and player names are resolved from lookup maps built at construction.
 */
public class HeroService {

    private final List<Hero> heroes;
    private final Map<String, Player>    playerMap;   // playerId → Player
    private final Map<String, Equipment> equipMap;    // equipId  → Equipment

    public HeroService(List<Hero> heroes, List<Player> players,
                       List<Equipment> equipment) {
        this.heroes = new ArrayList<>(heroes);
        this.playerMap = new HashMap<>();
        for (Player p : players) playerMap.put(p.getId(), p);
        this.equipMap = new HashMap<>();
        for (Equipment e : equipment) equipMap.put(e.getId(), e);
    }

    /* ---- search ---- */

    public Optional<Hero> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        for (Hero h : heroes) {
            if (h.getId().equals(id.trim())) return Optional.of(h);
        }
        return Optional.empty();
    }

    /** Case-insensitive match on hero name. */
    public Optional<Hero> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.trim().toLowerCase();
        for (Hero h : heroes) {
            if (h.getName().toLowerCase().equals(lower)) return Optional.of(h);
        }
        return Optional.empty();
    }

    public List<Hero> listAll() {
        return Collections.unmodifiableList(heroes);
    }

    /** Adds a hero to the internal list. */
    public void addHero(Hero h) {
        if (h != null) heroes.add(h);
    }

    /**
     * Removes a hero by ID. Also removes the hero from all players' heroPools
     * and equippedItems.
     * @return true if the hero was found and removed.
     */
    public boolean deleteHero(String heroId) {
        if (heroId == null) return false;
        Optional<Hero> opt = findById(heroId);
        if (opt.isEmpty()) return false;
        // Remove from all players' hero pools and loadouts
        for (Player p : playerMap.values()) {
            p.removeHero(heroId);
            p.removeEquippedHero(heroId);
        }
        heroes.remove(opt.get());
        return true;
    }

    /* ---- cross-reference: who owns this hero ---- */

    /**
     * Returns all players whose heroPool contains the given heroId.
     * Demonstrates association: Hero → Player (object reference).
     */
    public List<Player> getOwners(String heroId) {
        List<Player> owners = new ArrayList<>();
        for (Player p : playerMap.values()) {
            if (p.getHeroPool().contains(heroId)) {
                owners.add(p);
            }
        }
        return owners;
    }

    /**
     * Resolves a player's heroPool IDs to actual Hero objects.
     * Demonstrates association: Player → Hero (object reference).
     */
    public List<Hero> getHeroesOfPlayer(String playerId) {
        Player p = playerMap.get(playerId);
        if (p == null) return Collections.emptyList();
        List<Hero> result = new ArrayList<>();
        for (String hid : p.getHeroPool()) {
            for (Hero h : heroes) {
                if (h.getId().equals(hid)) {
                    result.add(h);
                    break;
                }
            }
        }
        return result;
    }

    /* ---- ID → name resolution ---- */

    public String getPlayerName(String playerId) {
        Player p = playerMap.get(playerId);
        return p != null ? p.getNickname() : playerId;
    }

    public String getEquipmentName(String equipId) {
        Equipment e = equipMap.get(equipId);
        return e != null ? e.getName() : equipId;
    }

    /* ---- formatting helpers ---- */

    /** Formats base stats as a readable string. */
    public String formatBaseStats(Hero h) {
        return String.format("ATK: %d  |  DEF: %d  |  HP: %d",
                h.getBaseAttack(), h.getBaseDefense(), h.getBaseHp());
    }

    /** Formats compatible equipment IDs to a comma-separated name list. */
    public String formatCompatibleEquipment(Hero h) {
        List<String> ids = h.getCompatibleEquipmentIds();
        if (ids == null || ids.isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (String eid : ids) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(getEquipmentName(eid));
        }
        return sb.toString();
    }

    /** Formats the owner list as a comma-separated nickname list. */
    public String formatOwners(Hero h) {
        List<Player> owners = getOwners(h.getId());
        if (owners.isEmpty()) return "(no players own this hero)";
        StringBuilder sb = new StringBuilder();
        for (Player p : owners) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getNickname());
        }
        return sb.toString();
    }
}
