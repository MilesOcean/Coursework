package service;

import model.*;

import java.util.*;

/**
 * Business logic for equipment-related operations.
 *
 * Design decisions (from design.md §3.2, plan.md §2.4):
 * - Usage count is derived by scanning every player's equippedItems Map values.
 * - Ties on usage count are broken alphabetically by equipment name.
 * - getRankedEquipment() returns a list of entries sorted desc by count.
 */
public class EquipmentService {

    private final List<Equipment> equipment;

    /** equipId → usage count (cached after first computation). */
    private Map<String, Integer> usageCache;

    public EquipmentService(List<Equipment> equipment, List<Player> players) {
        this.equipment = equipment;
        this.usageCache = null;
        computeUsage(players);
    }

    /** Counts how many players equip each equipment item across all hero loadouts. */
    private void computeUsage(List<Player> players) {
        usageCache = new HashMap<>();
        // Initialise all equipment with 0
        for (Equipment e : equipment) {
            usageCache.put(e.getId(), 0);
        }
        // Walk every player's equippedItems
        for (Player p : players) {
            for (List<String> equipIds : p.getEquippedItems().values()) {
                for (String eid : equipIds) {
                    usageCache.merge(eid, 1, Integer::sum);
                }
            }
        }
    }

    /** @return usage count for a single equipment item. */
    public int getUsageCount(String equipId) {
        return usageCache.getOrDefault(equipId, 0);
    }

    /**
     * Returns all equipment sorted by usage count descending.
     * Ties are broken alphabetically by equipment name.
     */
    public List<Equipment> getRankedEquipment() {
        List<Equipment> sorted = new ArrayList<>(equipment);
        sorted.sort((a, b) -> {
            int countA = getUsageCount(a.getId());
            int countB = getUsageCount(b.getId());
            if (countB != countA) return Integer.compare(countB, countA); // desc
            return a.getName().compareTo(b.getName());                    // name asc
        });
        return Collections.unmodifiableList(sorted);
    }
}
