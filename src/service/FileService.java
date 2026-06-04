package service;

import model.*;
import util.CsvUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Coordinates loading all entity data from CSV files and saving it back.
 *
 * Design decisions (from design.md §2.4, §4.1):
 * - On startup, tries CSV files first; falls back to DataInitializer if files are missing.
 * - On shutdown (app exit / logout), saves everything to data/*.csv.
 * - equippedItems is stored separately in loadouts.csv (playerId, heroId, equipmentIds).
 */
public class FileService {

    /** Bundles all loaded entity collections. */
    public static class Data {
        public List<Player>      players   = new ArrayList<>();
        public List<Admin>       admins    = new ArrayList<>();
        public List<Hero>        heroes    = new ArrayList<>();
        public List<Equipment>   equipment = new ArrayList<>();
        public List<Team>        teams     = new ArrayList<>();
        public List<MatchRecord> matches   = new ArrayList<>();
    }

    private static final String DIR = "data/";

    /* ============================================================
     *  LOAD
     * ============================================================ */

    /**
     * Attempts to load all entities from CSV files.
     * @return loaded Data (may be partially or fully empty if files don't exist).
     */
    public static Data loadAll() {
        Data d = new Data();
        d.players   = CsvUtil.readCsv(DIR + "players.csv",   Player::fromCsvRow);
        d.admins    = CsvUtil.readCsv(DIR + "admins.csv",    Admin::fromCsvRow);
        d.heroes    = CsvUtil.readCsv(DIR + "heroes.csv",    Hero::fromCsvRow);
        d.equipment = CsvUtil.readCsv(DIR + "equipment.csv", Equipment::fromCsvRow);
        d.teams     = CsvUtil.readCsv(DIR + "teams.csv",     Team::fromCsvRow);
        d.matches   = CsvUtil.readCsv(DIR + "matches.csv",   MatchRecord::fromCsvRow);

        // Load equippedItems from loadouts.csv and wire into players
        loadLoadouts(d.players);

        return d;
    }

    /** Returns true if enough core data was loaded from CSV to skip DataInitializer. */
    public static boolean hasData(Data d) {
        return !d.players.isEmpty();
    }

    /* ============================================================
     *  SAVE
     * ============================================================ */

    /** Persists all entity collections. */
    public static void saveAll(List<Player> players, List<Admin> admins,
                               List<Hero> heroes, List<Equipment> equipment,
                               List<Team> teams, List<MatchRecord> matches) {
        CsvUtil.writeCsv(DIR + "players.csv", new ArrayList<>(players),
                "id,username,passwordHash,salt,nickname,level,rank,winCount,matchCount,teamId,heroPool");
        CsvUtil.writeCsv(DIR + "admins.csv", new ArrayList<>(admins),
                "id,username,passwordHash,salt,nickname,managedTeamIds");
        CsvUtil.writeCsv(DIR + "heroes.csv", new ArrayList<>(heroes),
                "id,name,heroType,baseAttack,baseDefense,baseHp,compatibleEquipmentIds");
        CsvUtil.writeCsv(DIR + "equipment.csv", new ArrayList<>(equipment),
                "id,name,type,price,attackBonus,defenseBonus,magicBonus,description");
        CsvUtil.writeCsv(DIR + "teams.csv", new ArrayList<>(teams),
                "id,name,captainId,rank,memberIds");
        CsvUtil.writeCsv(DIR + "matches.csv", new ArrayList<>(matches),
                "id,date,team1Id,team2Id,result,mvpPlayerId,durationMinutes,heroPicks");

        saveLoadouts(players);

        System.out.println("  Data saved to " + DIR);
    }

    /* ============================================================
     *  LOADOUTS — player equippedItems stored separately
     * ============================================================ */

    private static void loadLoadouts(List<Player> players) {
        File file = new File(DIR + "loadouts.csv");
        if (!file.exists()) return;

        Map<String, Player> playerMap = new HashMap<>();
        for (Player p : players) playerMap.put(p.getId(), p);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // skip header
            if (header == null) return;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] fields = line.split(",", -1);
                if (fields.length < 3) continue;

                Player p = playerMap.get(fields[0]);
                if (p == null) continue;

                List<String> equipIds = new ArrayList<>();
                if (!fields[2].isEmpty()) {
                    for (String eid : fields[2].split(";")) {
                        if (!eid.isEmpty()) equipIds.add(eid);
                    }
                }
                p.setEquippedItems(fields[1], equipIds);
            }
        } catch (IOException e) {
            System.out.println("  [FileService] Error reading loadouts.csv: " + e.getMessage());
        }
    }

    private static void saveLoadouts(List<Player> players) {
        List<String> lines = new ArrayList<>();
        lines.add("playerId,heroId,equipmentIds");
        for (Player p : players) {
            for (Map.Entry<String, List<String>> e : p.getEquippedItems().entrySet()) {
                String equip = e.getValue() != null ? String.join(";", e.getValue()) : "";
                lines.add(p.getId() + "," + e.getKey() + "," + equip);
            }
        }
        try {
            File parent = new File(DIR);
            if (!parent.exists()) parent.mkdirs();

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(DIR + "loadouts.csv"),
                            StandardCharsets.UTF_8))) {
                for (String line : lines) writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("  [FileService] Error writing loadouts.csv: " + e.getMessage());
        }
    }
}
