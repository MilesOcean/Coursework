package model;

import enums.HeroType;
import interfaces.CsvPersistable;

import java.util.ArrayList;
import java.util.List;

/**
 * A playable hero character.
 *
 * Design decisions (from design.md §2.1, plan.md §4.4, §6):
 * - Stats are flat int fields (baseAttack, baseDefense, baseHp), not a Map —
 *   this matches the CSV columns directly and keeps the class simple.
 * - compatibleEquipmentIds is a List of Equipment.id Strings (not objects).
 * - No difficulty or skills fields — those are not in the CSV/design spec.
 */
public class Hero implements CsvPersistable {

    private String id;
    private String name;
    private HeroType heroType;
    private int baseAttack;
    private int baseDefense;
    private int baseHp;
    private List<String> compatibleEquipmentIds;   // Equipment.id references

    public Hero(String id, String name, HeroType heroType,
                int baseAttack, int baseDefense, int baseHp) {
        this.id = id;
        this.name = name;
        this.heroType = heroType;
        this.baseAttack = Math.max(0, baseAttack);
        this.baseDefense = Math.max(0, baseDefense);
        this.baseHp = Math.max(1, baseHp);
        this.compatibleEquipmentIds = new ArrayList<>();
    }

    /* ---- helpers ---- */
    public void addCompatibleEquipment(String equipmentId) {
        if (equipmentId != null && !compatibleEquipmentIds.contains(equipmentId)) {
            compatibleEquipmentIds.add(equipmentId);
        }
    }

    /* ---- CsvPersistable ---- */
    @Override
    public String toCsvRow() {
        return String.join(",",
                id,
                name,
                heroType.name(),
                String.valueOf(baseAttack),
                String.valueOf(baseDefense),
                String.valueOf(baseHp),
                String.join(";", compatibleEquipmentIds));
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HeroType getHeroType() { return heroType; }
    public void setHeroType(HeroType heroType) { this.heroType = heroType; }

    public int getBaseAttack() { return baseAttack; }
    public void setBaseAttack(int baseAttack) { this.baseAttack = Math.max(0, baseAttack); }

    public int getBaseDefense() { return baseDefense; }
    public void setBaseDefense(int baseDefense) { this.baseDefense = Math.max(0, baseDefense); }

    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = Math.max(1, baseHp); }

    public List<String> getCompatibleEquipmentIds() { return compatibleEquipmentIds; }
    public void setCompatibleEquipmentIds(List<String> equipmentIds) { this.compatibleEquipmentIds = equipmentIds; }

    @Override
    public String toString() {
        return String.format("Hero[%s] %s | %s | ATK:%d DEF:%d HP:%d",
                id, name, heroType, baseAttack, baseDefense, baseHp);
    }
}
