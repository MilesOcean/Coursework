package model;

import enums.EquipmentType;
import interfaces.CsvPersistable;

/**
 * An equipment item that boosts hero stats.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - Bonuses are flat int fields (attackBonus, defenseBonus, magicBonus) —
 *   this matches the CSV columns directly.
 * - price is in gold, always ≥ 0.
 * - description is flavour text from the game.
 */
public class Equipment implements CsvPersistable {

    private String id;
    private String name;
    private EquipmentType type;
    private int price;
    private int attackBonus;
    private int defenseBonus;
    private int magicBonus;
    private String description;

    public Equipment(String id, String name, EquipmentType type,
                     int price, int attackBonus, int defenseBonus,
                     int magicBonus, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = Math.max(0, price);
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.magicBonus = magicBonus;
        this.description = description;
    }

    /* ---- CsvPersistable ---- */
    @Override
    public String toCsvRow() {
        return String.join(",",
                id,
                name,
                type.name(),
                String.valueOf(price),
                String.valueOf(attackBonus),
                String.valueOf(defenseBonus),
                String.valueOf(magicBonus),
                description);
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = Math.max(0, price); }

    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }

    public int getDefenseBonus() { return defenseBonus; }
    public void setDefenseBonus(int defenseBonus) { this.defenseBonus = defenseBonus; }

    public int getMagicBonus() { return magicBonus; }
    public void setMagicBonus(int magicBonus) { this.magicBonus = magicBonus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("Equipment[%s] %s | %s | %d gold | ATK+%d DEF+%d MAG+%d",
                id, name, type, price, attackBonus, defenseBonus, magicBonus);
    }
}
