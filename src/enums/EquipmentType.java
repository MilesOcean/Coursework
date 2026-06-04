package enums;

/**
 * Represents the category of an equipment item.
 * Determines which stat bonuses the equipment provides.
 */
public enum EquipmentType {
    ATTACK,    // Physical damage items (e.g. Blade, Claw)
    DEFENSE,   // Armour / HP items (e.g. Plate, Shield)
    MAGIC,     // Ability power items (e.g. Tome, Staff)
    MOVEMENT,  // Speed / boots items
    JUNGLE,    // Jungle-specific items
    SUPPORT    // Warding / utility items
}
