package enums;

/**
 * Represents the class/role of a hero in the game.
 * Each hero belongs to exactly one HeroType.
 */
public enum HeroType {
    WARRIOR,   // Melee fighter — balanced offense and defense
    MAGE,      // Magic damage dealer — ability-focused
    ASSASSIN,  // Burst damage — high mobility, flanking playstyle
    MARKSMAN,  // Ranged physical damage — sustained DPS
    SUPPORT,   // Healer / utility — enables teammates
    TANK       // Damage sponge — high HP and crowd control
}
