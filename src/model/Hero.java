package model;

import enums.HeroType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A playable hero character in the game.
 *
 * Design choices:
 * - skills is a simple List<String> (skill names only). A more advanced
 *   version could use a Skill class, but for a coursework project strings
 *   are clear and sufficient.
 * - baseStats uses Map<String, Integer> for flexibility — each hero type
 *   may have different stat keys (HP, ATK, DEF, SPD, MANA, etc.).
 * - winRate is a computed aggregate across all players who own this hero;
 *   it gets updated by HeroService after matches are recorded.
 */
public class Hero {

    private String id;
    private String name;
    private HeroType heroType;
    private int difficulty;                   // 1–10
    private List<String> skills;              // skill names (usually 4)
    private Map<String, Integer> baseStats;   // e.g. {"HP": 3000, "ATK": 120}
    private double winRate;                   // aggregate, updated by service

    /**
     * @param id         UUID assigned by the caller.
     * @param name       Unique hero name.
     * @param heroType   The hero's class.
     * @param difficulty 1–10 scale.
     */
    public Hero(String id, String name, HeroType heroType, int difficulty) {
        this.id = id;
        this.name = name;
        this.heroType = heroType;
        this.difficulty = Math.max(1, Math.min(10, difficulty));  // clamp 1–10
        this.skills = new ArrayList<>();
        this.baseStats = new HashMap<>();
        this.winRate = 0.0;
    }

    /* ---- skill management ---- */
    /** Adds a skill name to this hero (max 4, enforced by HeroService). */
    public void addSkill(String skillName) {
        if (skillName != null && !skillName.isBlank() && skills.size() < 4) {
            skills.add(skillName);
        }
    }

    /* ---- stat helpers ---- */
    /** Sets a single stat (e.g. "HP" → 3000). */
    public void setStat(String key, int value) {
        baseStats.put(key, value);
    }

    /** Returns a stat value, or 0 if the key is missing. */
    public int getStat(String key) {
        return baseStats.getOrDefault(key, 0);
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HeroType getHeroType() { return heroType; }
    public void setHeroType(HeroType heroType) { this.heroType = heroType; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
    }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Map<String, Integer> getBaseStats() { return baseStats; }
    public void setBaseStats(Map<String, Integer> baseStats) { this.baseStats = baseStats; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    @Override
    public String toString() {
        return String.format("Hero[%s] %s | %s | Difficulty %d | Skills: %d",
                getId(), name, heroType, difficulty, skills.size());
    }
}
