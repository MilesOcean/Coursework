package model;

import enums.EquipmentType;

import java.util.HashMap;
import java.util.Map;

/**
 * An equipment item that boosts hero stats.
 *
 * Design choices:
 * - statBonuses uses Map<String, Integer> for the same reason as Hero.baseStats
 *   — different items give different bonuses (ATK, DEF, SPD, HP, MANA, etc.).
 * - price is in gold (the in-game currency), always non-negative.
 * - No "ownedBy" reference back to Player because ownership is tracked
 *   from the Player side via ownedEquipment. This avoids circular lookups.
 */
public class Equipment {

    private String id;
    private String name;
    private EquipmentType type;
    private Map<String, Integer> statBonuses;   // e.g. {"ATK": +60, "SPD": +5}
    private int price;
    private String description;

    /**
     * @param id          UUID assigned by the caller.
     * @param name        Display name (e.g. "Blade of Despair").
     * @param type        Attack / Defense / Magic / Movement / Jungle / Support.
     * @param price       Gold cost (≥ 0).
     * @param description Flavour text.
     */
    public Equipment(String id, String name, EquipmentType type,
                     int price, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.statBonuses = new HashMap<>();
        this.price = Math.max(0, price);
        this.description = description;
    }

    /* ---- stat helpers ---- */
    /** Adds or updates a stat bonus (e.g. "ATK" → 60). */
    public void setBonus(String statName, int value) {
        statBonuses.put(statName, value);
    }

    /** Returns the bonus for a given stat, or 0 if not present. */
    public int getBonus(String statName) {
        return statBonuses.getOrDefault(statName, 0);
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }

    public Map<String, Integer> getStatBonuses() { return statBonuses; }
    public void setStatBonuses(Map<String, Integer> statBonuses) { this.statBonuses = statBonuses; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = Math.max(0, price); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("Equipment[%s] %s | %s | Price %d gold",
                getId(), name, type, price);
    }
}
