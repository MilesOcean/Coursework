package util;

import enums.*;
import model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

/**
 * Creates a complete set of sample data for the Honor of Kings system.
 *
 * Usage (from Main.java or a service):
 * <pre>
 *   DataInitializer init = new DataInitializer();
 *   List<Player>      players   = init.getPlayers();
 *   List<Hero>        heroes    = init.getHeroes();
 *   List<Equipment>   equipment = init.getEquipment();
 *   List<Team>        teams     = init.getTeams();
 *   List<MatchRecord> matches   = init.getMatches();
 * </pre>
 *
 * All passwords are "123456" (SHA-256 hashed with a per-user salt).
 *
 * Dataset counts:
 *   Players:   10   (3–4 per team)
 *   Teams:      3   (AG超玩会, 武汉eStarPro, 重庆狼队)
 *   Heroes:    15   (3 assassins, 3 mages, 2 marksmen, 3 warriors, 2 tanks, 2 supports)
 *   Equipment: 20   (5 attack, 5 defense, 5 magic, 2 movement, 1 jungle, 2 support)
 *   Matches:   10   (round-robin between the 3 teams)
 */
public class DataInitializer {

    /* ============================================================
     *  Stored collections — populated once in the constructor
     * ============================================================ */
    private final List<Player>      players   = new ArrayList<>();
    private final List<Hero>        heroes    = new ArrayList<>();
    private final List<Equipment>   equipment = new ArrayList<>();
    private final List<Team>        teams     = new ArrayList<>();
    private final List<MatchRecord> matches   = new ArrayList<>();

    /* lookup maps — keyed by name or username */
    private final Map<String, Player>    playerMap = new HashMap<>();
    private final Map<String, String>    heroId    = new HashMap<>();   // name → id
    private final Map<String, String>    equipId   = new HashMap<>();   // name → id
    private final Map<String, String>    teamId    = new HashMap<>();   // name → id

    private static final String DEFAULT_PASSWORD = "123456";

    /* ============================================================
     *  Constructor — builds everything in dependency order
     * ============================================================ */
    public DataInitializer() {
        createEquipment();          // 1. equipment (no deps)
        createHeroes();             // 2. heroes (no deps)
        assignCompatibleEquip();    // 3. wire hero ↔ compatible equipment
        createPlayers();            // 4. players (needed by teams)
        assignHeroesToPlayers();    // 5. player heroPool
        createTeams();              // 6. teams (needs player IDs)
        assignTeamIdsToPlayers();   // 7. player.teamId back-refs
        assignLoadouts();           // 8. player.equippedItems
        createMatches();            // 9. matches (needs team IDs + player IDs)
    }

    /* ============================================================
     *  Password helper — SHA-256(salt + password)
     * ============================================================ */
    private static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "hash_error";
        }
    }

    private static String[] credentials(String username) {
        String id   = UUID.randomUUID().toString();
        String salt = UUID.randomUUID().toString().substring(0, 16);
        String hash = hash(DEFAULT_PASSWORD, salt);
        return new String[]{id, salt, hash};
    }

    /* ---- convenience lookups (return IDs) ---- */
    private String pid(String username) { return playerMap.get(username).getId(); }
    private String hid(String heroName) { return heroId.get(heroName); }
    private String eid(String equipName){ return equipId.get(equipName); }
    private String tid(String teamName) { return teamId.get(teamName); }

    /* ---- convenience lookups (return objects) ---- */
    private Hero hero(String name) {
        for (Hero h : heroes) { if (h.getName().equals(name)) return h; }
        return null;
    }

    /* ============================================================
     *  1. EQUIPMENT — 20 items
     *     Constructor: (id, name, type, price, atk, def, mag, desc)
     *     Bonuses are flat ints per the CSV design (plan.md §6).
     * ============================================================ */
    private void createEquipment() {
        // ---- ATTACK (5) ----
        addEquip("无尽战刃",   EquipmentType.ATTACK,   2140, 120,   0,   0, "暴击效果+40%，暴击率+20%");
        addEquip("破军",       EquipmentType.ATTACK,   2950, 180,   0,   0, "对生命值低于50%的敌人造成额外30%伤害");
        addEquip("暗影战斧",   EquipmentType.ATTACK,   2090,  85,   0,   0, "物理穿透+50，冷却缩减+15%");
        addEquip("宗师之力",   EquipmentType.ATTACK,   2100,  80,   0,   0, "使用技能后下次普攻额外造成80%物理伤害");
        addEquip("破晓",       EquipmentType.ATTACK,   3400,  50,   0,   0, "物理穿透+40%，攻速+35%，暴击率+15%");

        // ---- DEFENSE (5) ----
        addEquip("不祥征兆",   EquipmentType.DEFENSE,  2180,   0, 270,   0, "受到攻击减少攻击者30%攻速和15%移速");
        addEquip("反伤刺甲",   EquipmentType.DEFENSE,  1910,  40, 360,   0, "反弹25%受到的物理伤害给攻击者");
        addEquip("霸者重装",   EquipmentType.DEFENSE,  2070,   0, 200,   0, "脱战后每秒恢复3%最大生命值");
        addEquip("魔女斗篷",   EquipmentType.DEFENSE,  2120,   0, 100, 200, "获得吸收法术伤害的护盾");
        addEquip("极寒风暴",   EquipmentType.DEFENSE,  2100,   0, 360,   0, "受到单次伤害超过10%生命值时触发冰爆");

        // ---- MAGIC (5) ----
        addEquip("回响之杖",   EquipmentType.MAGIC,    2100,   0,   0, 240, "技能命中触发小范围爆炸造成法术伤害");
        addEquip("博学者之怒", EquipmentType.MAGIC,    2300,   0,   0, 240, "法术攻击提升35%");
        addEquip("虚无法杖",   EquipmentType.MAGIC,    2110,   0,   0, 200, "法术穿透+45%");
        addEquip("贤者之书",   EquipmentType.MAGIC,    2990,   0,   0, 400, "法术攻击+400，最大生命值+10%");
        addEquip("痛苦面具",   EquipmentType.MAGIC,    2040,   0,   0, 180, "技能命中造成目标当前生命值8%的法术伤害");

        // ---- MOVEMENT (2) ----
        addEquip("急速战靴",   EquipmentType.MOVEMENT,  710,  15,   0,   0, "攻速+25%，移速+60");
        addEquip("冷静之靴",   EquipmentType.MOVEMENT,  710,   0,   0,  30, "冷却缩减+15%，移速+60");

        // ---- JUNGLE (1) ----
        addEquip("追击刀锋",   EquipmentType.JUNGLE,    750,  30,   0,   0, "对野怪伤害+50%，击杀野怪获得额外经验");

        // ---- SUPPORT (2) ----
        addEquip("近卫荣耀",   EquipmentType.SUPPORT,  2100,   0,  60,  30, "周围友军物理防御+60，法术防御+48");
        addEquip("奔狼纹章",   EquipmentType.SUPPORT,  2100,   0,  40,  50, "使用后周围友军移速+50%，持续3秒");
    }

    private void addEquip(String name, EquipmentType type, int price,
                          int atk, int def, int mag, String desc) {
        String id = UUID.randomUUID().toString();
        Equipment e = new Equipment(id, name, type, price, atk, def, mag, desc);
        equipment.add(e);
        equipId.put(name, id);
    }

    /* ============================================================
     *  2. HEROES — 15 heroes
     *     Constructor: (id, name, heroType, baseAttack, baseDefense, baseHp)
     *     compatibleEquipmentIds is set later in assignCompatibleEquip().
     * ============================================================ */
    private void createHeroes() {
        addHero("李白",   HeroType.ASSASSIN, 170,  90, 2800);
        addHero("韩信",   HeroType.ASSASSIN, 175,  85, 2600);
        addHero("兰陵王", HeroType.ASSASSIN, 180,  80, 2700);

        addHero("妲己",   HeroType.MAGE,      50,  70, 2400);
        addHero("安琪拉", HeroType.MAGE,      45,  65, 2500);
        addHero("貂蝉",   HeroType.MAGE,      55,  75, 2600);

        addHero("鲁班七号", HeroType.MARKSMAN, 160,  60, 2300);
        addHero("后羿",   HeroType.MARKSMAN, 155,  65, 2400);

        addHero("亚瑟",   HeroType.WARRIOR,  140, 120, 3200);
        addHero("关羽",   HeroType.WARRIOR,  150, 110, 3100);
        addHero("铠",     HeroType.WARRIOR,  145, 115, 3300);

        addHero("张飞",   HeroType.TANK,      80, 180, 4000);
        addHero("廉颇",   HeroType.TANK,      75, 190, 4200);

        addHero("蔡文姬", HeroType.SUPPORT,   40,  80, 2600);
        addHero("瑶",     HeroType.SUPPORT,   35,  75, 2500);
    }

    private void addHero(String name, HeroType type, int atk, int def, int hp) {
        String id = UUID.randomUUID().toString();
        Hero h = new Hero(id, name, type, atk, def, hp);
        heroes.add(h);
        heroId.put(name, id);
    }

    /* ============================================================
     *  3. COMPATIBLE EQUIPMENT — wire hero ↔ equipment
     * ============================================================ */
    private void assignCompatibleEquip() {
        hero("李白")  .addCompatibleEquipment(eid("无尽战刃"));
        hero("李白")  .addCompatibleEquipment(eid("暗影战斧"));
        hero("李白")  .addCompatibleEquipment(eid("宗师之力"));
        hero("李白")  .addCompatibleEquipment(eid("急速战靴"));
        hero("李白")  .addCompatibleEquipment(eid("追击刀锋"));

        hero("韩信")  .addCompatibleEquipment(eid("无尽战刃"));
        hero("韩信")  .addCompatibleEquipment(eid("暗影战斧"));
        hero("韩信")  .addCompatibleEquipment(eid("宗师之力"));
        hero("韩信")  .addCompatibleEquipment(eid("急速战靴"));
        hero("韩信")  .addCompatibleEquipment(eid("追击刀锋"));

        hero("兰陵王").addCompatibleEquipment(eid("暗影战斧"));
        hero("兰陵王").addCompatibleEquipment(eid("破军"));
        hero("兰陵王").addCompatibleEquipment(eid("宗师之力"));
        hero("兰陵王").addCompatibleEquipment(eid("急速战靴"));

        hero("妲己")  .addCompatibleEquipment(eid("回响之杖"));
        hero("妲己")  .addCompatibleEquipment(eid("博学者之怒"));
        hero("妲己")  .addCompatibleEquipment(eid("虚无法杖"));
        hero("妲己")  .addCompatibleEquipment(eid("冷静之靴"));

        hero("安琪拉").addCompatibleEquipment(eid("回响之杖"));
        hero("安琪拉").addCompatibleEquipment(eid("博学者之怒"));
        hero("安琪拉").addCompatibleEquipment(eid("痛苦面具"));
        hero("安琪拉").addCompatibleEquipment(eid("冷静之靴"));

        hero("貂蝉")  .addCompatibleEquipment(eid("回响之杖"));
        hero("貂蝉")  .addCompatibleEquipment(eid("博学者之怒"));
        hero("貂蝉")  .addCompatibleEquipment(eid("贤者之书"));
        hero("貂蝉")  .addCompatibleEquipment(eid("冷静之靴"));

        hero("鲁班七号").addCompatibleEquipment(eid("无尽战刃"));
        hero("鲁班七号").addCompatibleEquipment(eid("破晓"));
        hero("鲁班七号").addCompatibleEquipment(eid("宗师之力"));
        hero("鲁班七号").addCompatibleEquipment(eid("急速战靴"));

        hero("后羿")  .addCompatibleEquipment(eid("无尽战刃"));
        hero("后羿")  .addCompatibleEquipment(eid("破晓"));
        hero("后羿")  .addCompatibleEquipment(eid("暗影战斧"));
        hero("后羿")  .addCompatibleEquipment(eid("急速战靴"));

        hero("亚瑟")  .addCompatibleEquipment(eid("暗影战斧"));
        hero("亚瑟")  .addCompatibleEquipment(eid("不祥征兆"));
        hero("亚瑟")  .addCompatibleEquipment(eid("反伤刺甲"));
        hero("亚瑟")  .addCompatibleEquipment(eid("霸者重装"));

        hero("关羽")  .addCompatibleEquipment(eid("暗影战斧"));
        hero("关羽")  .addCompatibleEquipment(eid("破军"));
        hero("关羽")  .addCompatibleEquipment(eid("不祥征兆"));
        hero("关羽")  .addCompatibleEquipment(eid("极寒风暴"));

        hero("铠")    .addCompatibleEquipment(eid("暗影战斧"));
        hero("铠")    .addCompatibleEquipment(eid("宗师之力"));
        hero("铠")    .addCompatibleEquipment(eid("不祥征兆"));
        hero("铠")    .addCompatibleEquipment(eid("反伤刺甲"));

        hero("张飞")  .addCompatibleEquipment(eid("不祥征兆"));
        hero("张飞")  .addCompatibleEquipment(eid("霸者重装"));
        hero("张飞")  .addCompatibleEquipment(eid("魔女斗篷"));
        hero("张飞")  .addCompatibleEquipment(eid("极寒风暴"));

        hero("廉颇")  .addCompatibleEquipment(eid("不祥征兆"));
        hero("廉颇")  .addCompatibleEquipment(eid("反伤刺甲"));
        hero("廉颇")  .addCompatibleEquipment(eid("霸者重装"));
        hero("廉颇")  .addCompatibleEquipment(eid("极寒风暴"));

        hero("蔡文姬").addCompatibleEquipment(eid("近卫荣耀"));
        hero("蔡文姬").addCompatibleEquipment(eid("冷静之靴"));
        hero("蔡文姬").addCompatibleEquipment(eid("魔女斗篷"));

        hero("瑶")    .addCompatibleEquipment(eid("近卫荣耀"));
        hero("瑶")    .addCompatibleEquipment(eid("奔狼纹章"));
        hero("瑶")    .addCompatibleEquipment(eid("冷静之靴"));
    }

    /* ============================================================
     *  4. PLAYERS — 10 players
     *     Stores: username, nickname, rank, level, matchCount, winCount.
     * ============================================================ */
    private void createPlayers() {
        addPlayer("menglei",  "梦泪", Rank.LEGEND,       30, 1200, 680);
        addPlayer("feiniu",   "飞牛", Rank.GRANDMASTER,  28, 1100, 630);
        addPlayer("maoshen",  "猫神", Rank.GRANDMASTER,  27, 1050, 580);
        addPlayer("jiucheng", "久诚", Rank.MASTER,       24,  950, 510);
        addPlayer("nuanyang", "暖阳", Rank.MASTER,       25,  900, 480);
        addPlayer("yinuo",    "一诺", Rank.DIAMOND,      22,  800, 440);
        addPlayer("qingqing", "清清", Rank.DIAMOND,      21,  750, 420);
        addPlayer("huahai",   "花海", Rank.GRANDMASTER,  26, 1000, 550);
        addPlayer("wuwei",    "无畏", Rank.PLATINUM,     19,  600, 350);
        addPlayer("tanran",   "坦然", Rank.PLATINUM,     18,  550, 320);
    }

    private void addPlayer(String username, String nickname, Rank rank,
                           int level, int matchCount, int winCount) {
        String[] cred = credentials(username);
        Player p = new Player(cred[0], username, cred[1], cred[2], nickname);
        p.setRank(rank);
        p.setLevel(level);
        p.setMatchCount(matchCount);
        p.setWinCount(winCount);
        players.add(p);
        playerMap.put(username, p);
    }

    /* ============================================================
     *  5. HERO POOL — assign hero IDs to each player (2–4 each)
     * ============================================================ */
    private void assignHeroesToPlayers() {
        giveHeroes("menglei",  "李白", "韩信", "兰陵王", "铠");
        giveHeroes("feiniu",   "关羽", "亚瑟", "铠", "张飞");
        giveHeroes("maoshen",  "貂蝉", "妲己", "安琪拉", "瑶");
        giveHeroes("jiucheng", "安琪拉", "鲁班七号", "后羿");
        giveHeroes("nuanyang", "韩信", "李白", "关羽");
        giveHeroes("yinuo",    "后羿", "鲁班七号", "亚瑟");
        giveHeroes("qingqing", "廉颇", "张飞", "亚瑟");
        giveHeroes("huahai",   "兰陵王", "李白", "后羿", "鲁班七号");
        giveHeroes("wuwei",    "铠", "关羽", "廉颇");
        giveHeroes("tanran",   "蔡文姬", "瑶", "妲己", "安琪拉");
    }

    private void giveHeroes(String username, String... heroNames) {
        Player p = playerMap.get(username);
        for (String h : heroNames) p.addHero(hid(h));
    }

    /* ============================================================
     *  6. TEAMS — 3 teams, 3–4 members each
     *     Constructor: (id, name, captainId, rank)
     * ============================================================ */
    private void createTeams() {
        // AG超玩会 — 3 members, captain = menglei
        Team ag = new Team(UUID.randomUUID().toString(), "AG超玩会",
                pid("menglei"), Rank.LEGEND);
        ag.addMember(pid("yinuo"));
        ag.addMember(pid("jiucheng"));
        teams.add(ag);
        teamId.put("AG超玩会", ag.getId());

        // 武汉eStarPro — 3 members, captain = huahai
        Team es = new Team(UUID.randomUUID().toString(), "武汉eStarPro",
                pid("huahai"), Rank.GRANDMASTER);
        es.addMember(pid("tanran"));
        es.addMember(pid("maoshen"));
        teams.add(es);
        teamId.put("武汉eStarPro", es.getId());

        // 重庆狼队 — 4 members, captain = feiniu
        Team wolves = new Team(UUID.randomUUID().toString(), "重庆狼队",
                pid("feiniu"), Rank.GRANDMASTER);
        wolves.addMember(pid("wuwei"));
        wolves.addMember(pid("qingqing"));
        wolves.addMember(pid("nuanyang"));
        teams.add(wolves);
        teamId.put("重庆狼队", wolves.getId());
    }

    /* ============================================================
     *  7. TEAM BACK-REFS — set player.teamId
     * ============================================================ */
    private void assignTeamIdsToPlayers() {
        playerMap.get("menglei") .setTeamId(tid("AG超玩会"));
        playerMap.get("yinuo")   .setTeamId(tid("AG超玩会"));
        playerMap.get("jiucheng").setTeamId(tid("AG超玩会"));

        playerMap.get("huahai")  .setTeamId(tid("武汉eStarPro"));
        playerMap.get("tanran")  .setTeamId(tid("武汉eStarPro"));
        playerMap.get("maoshen") .setTeamId(tid("武汉eStarPro"));

        playerMap.get("feiniu")  .setTeamId(tid("重庆狼队"));
        playerMap.get("wuwei")   .setTeamId(tid("重庆狼队"));
        playerMap.get("qingqing").setTeamId(tid("重庆狼队"));
        playerMap.get("nuanyang").setTeamId(tid("重庆狼队"));
    }

    /* ============================================================
     *  8. LOADOUTS — equippedItems (heroId → equipmentIds)
     * ============================================================ */
    private void assignLoadouts() {
        // menglei: 李白 deck + 韩信 deck
        loadout("menglei", "李白",
                eid("无尽战刃"), eid("暗影战斧"), eid("宗师之力"), eid("急速战靴"));
        loadout("menglei", "韩信",
                eid("无尽战刃"), eid("暗影战斧"), eid("追击刀锋"));

        // feiniu: 关羽 deck
        loadout("feiniu", "关羽",
                eid("暗影战斧"), eid("破军"), eid("不祥征兆"));

        // maoshen: 貂蝉 deck
        loadout("maoshen", "貂蝉",
                eid("回响之杖"), eid("博学者之怒"), eid("冷静之靴"));

        // jiucheng: 鲁班七号 deck
        loadout("jiucheng", "鲁班七号",
                eid("无尽战刃"), eid("破晓"), eid("急速战靴"));

        // nuanyang: 韩信 + 李白 decks
        loadout("nuanyang", "韩信",
                eid("无尽战刃"), eid("暗影战斧"), eid("急速战靴"));
        loadout("nuanyang", "李白",
                eid("无尽战刃"), eid("宗师之力"));

        // yinuo: 后羿 deck
        loadout("yinuo", "后羿",
                eid("无尽战刃"), eid("破晓"), eid("急速战靴"));

        // qingqing: 廉颇 deck
        loadout("qingqing", "廉颇",
                eid("不祥征兆"), eid("反伤刺甲"), eid("霸者重装"));

        // huahai: 兰陵王 + 后羿 decks
        loadout("huahai", "兰陵王",
                eid("暗影战斧"), eid("破军"));
        loadout("huahai", "后羿",
                eid("无尽战刃"), eid("破晓"));

        // wuwei: 铠 deck
        loadout("wuwei", "铠",
                eid("暗影战斧"), eid("不祥征兆"), eid("反伤刺甲"));

        // tanran: 蔡文姬 + 瑶 decks
        loadout("tanran", "蔡文姬",
                eid("近卫荣耀"), eid("冷静之靴"));
        loadout("tanran", "瑶",
                eid("近卫荣耀"), eid("奔狼纹章"));
    }

    private void loadout(String username, String heroName, String... equipIds) {
        playerMap.get(username).setEquippedItems(hid(heroName), List.of(equipIds));
    }

    /* ============================================================
     *  9. MATCHES — 10 records
     *     Constructor: (id, date, team1Id, team2Id, result, mvpPlayerId, durationMinutes)
     *     hero picks via addHeroPick(playerId, heroId)
     * ============================================================ */
    private void createMatches() {
        String ag     = tid("AG超玩会");
        String es     = tid("武汉eStarPro");
        String wolves = tid("重庆狼队");

        // AG vs eStarPro (4 matches)
        addMatch(ag, es, MatchResult.WIN,  24, pid("menglei"),
                Map.of(pid("menglei"), hid("李白"),   pid("yinuo"), hid("后羿"),
                       pid("jiucheng"), hid("安琪拉"), pid("huahai"), hid("兰陵王"),
                       pid("tanran"), hid("瑶"),       pid("maoshen"), hid("貂蝉")));
        addMatch(ag, es, MatchResult.LOSE, 23, pid("maoshen"),
                Map.of(pid("menglei"), hid("韩信"),   pid("yinuo"), hid("鲁班七号"),
                       pid("jiucheng"), hid("后羿"),   pid("huahai"), hid("李白"),
                       pid("tanran"), hid("安琪拉"),   pid("maoshen"), hid("妲己")));
        addMatch(ag, es, MatchResult.WIN,  26, pid("yinuo"),
                Map.of(pid("menglei"), hid("兰陵王"), pid("yinuo"), hid("后羿"),
                       pid("jiucheng"), hid("鲁班七号"), pid("huahai"), hid("兰陵王"),
                       pid("tanran"), hid("蔡文姬"), pid("maoshen"), hid("安琪拉")));
        addMatch(ag, es, MatchResult.WIN,  20, pid("menglei"),
                Map.of(pid("menglei"), hid("铠"),     pid("yinuo"), hid("鲁班七号"),
                       pid("jiucheng"), hid("安琪拉"), pid("huahai"), hid("后羿"),
                       pid("tanran"), hid("妲己"),     pid("maoshen"), hid("貂蝉")));

        // eStarPro vs Wolves (3 matches)
        addMatch(es, wolves, MatchResult.LOSE, 28, pid("feiniu"),
                Map.of(pid("huahai"), hid("李白"),    pid("tanran"), hid("瑶"),
                       pid("maoshen"), hid("貂蝉"),    pid("feiniu"), hid("关羽"),
                       pid("wuwei"), hid("铠"),       pid("qingqing"), hid("张飞"),
                       pid("nuanyang"), hid("韩信")));
        addMatch(es, wolves, MatchResult.WIN,  22, pid("huahai"),
                Map.of(pid("huahai"), hid("兰陵王"),  pid("tanran"), hid("安琪拉"),
                       pid("maoshen"), hid("妲己"),    pid("feiniu"), hid("亚瑟"),
                       pid("wuwei"), hid("关羽"),     pid("qingqing"), hid("廉颇"),
                       pid("nuanyang"), hid("李白")));
        addMatch(es, wolves, MatchResult.LOSE, 25, pid("qingqing"),
                Map.of(pid("huahai"), hid("后羿"),    pid("tanran"), hid("蔡文姬"),
                       pid("maoshen"), hid("安琪拉"),  pid("feiniu"), hid("关羽"),
                       pid("wuwei"), hid("廉颇"),     pid("qingqing"), hid("张飞"),
                       pid("nuanyang"), hid("韩信")));

        // Wolves vs AG (3 matches)
        addMatch(wolves, ag, MatchResult.WIN,  23, pid("feiniu"),
                Map.of(pid("feiniu"), hid("铠"),      pid("wuwei"), hid("关羽"),
                       pid("qingqing"), hid("张飞"),   pid("nuanyang"), hid("李白"),
                       pid("menglei"), hid("韩信"),   pid("yinuo"), hid("后羿"),
                       pid("jiucheng"), hid("安琪拉")));
        addMatch(wolves, ag, MatchResult.LOSE, 22, pid("menglei"),
                Map.of(pid("feiniu"), hid("关羽"),    pid("wuwei"), hid("铠"),
                       pid("qingqing"), hid("廉颇"),   pid("nuanyang"), hid("韩信"),
                       pid("menglei"), hid("李白"),   pid("yinuo"), hid("鲁班七号"),
                       pid("jiucheng"), hid("后羿")));
        addMatch(wolves, ag, MatchResult.WIN,  25, pid("nuanyang"),
                Map.of(pid("feiniu"), hid("铠"),      pid("wuwei"), hid("关羽"),
                       pid("qingqing"), hid("张飞"),   pid("nuanyang"), hid("李白"),
                       pid("menglei"), hid("兰陵王"), pid("yinuo"), hid("后羿"),
                       pid("jiucheng"), hid("鲁班七号")));
    }

    private void addMatch(String t1Id, String t2Id, MatchResult result,
                          int durationMinutes, String mvpId,
                          Map<String, String> picks) {
        // spread dates over the last 30 days
        LocalDate date = LocalDate.now().minusDays(1 + (int)(Math.random() * 30));
        MatchRecord m = new MatchRecord(UUID.randomUUID().toString(), date,
                t1Id, t2Id, result, mvpId, durationMinutes);
        picks.forEach(m::addHeroPick);
        matches.add(m);
    }

    /* ============================================================
     *  Public getters (immutable views)
     * ============================================================ */
    public List<Player>      getPlayers()   { return Collections.unmodifiableList(players); }
    public List<Hero>        getHeroes()    { return Collections.unmodifiableList(heroes); }
    public List<Equipment>   getEquipment() { return Collections.unmodifiableList(equipment); }
    public List<Team>        getTeams()     { return Collections.unmodifiableList(teams); }
    public List<MatchRecord> getMatches()   { return Collections.unmodifiableList(matches); }
}
