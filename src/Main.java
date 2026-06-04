import enums.Role;
import enums.HeroType;
import enums.EquipmentType;
import enums.Rank;
import enums.MatchResult;
import model.*;
import service.*;
import util.DataInitializer;
import util.PasswordHasher;

import java.util.*;
import java.time.LocalDate;

/**
 * Entry point — console-based menu for the Honor of Kings system.

 * Architecture (from design.md §4):
 *   Main (CLI) → Service layer → Model layer

 * Features:
 *   - Login with 3-attempt limit, role-based menus
 *   - Admin: full CRUD access (player, team, hero, equipment, match, leaderboard)
 *   - Player: read-only views + edit own nickname
 *   - Data persisted to data/*.csv on logout/exit; loaded from CSV on startup
 *     with automatic fallback to DataInitializer on first run.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    /* shared state */
    private static PlayerService        playerService;
    private static TeamService          teamService;
    private static HeroService          heroService;
    private static EquipmentService     equipService;
    private static MatchService         matchService;
    private static LeaderboardService   leaderboardService;
    private static AuthenticationService authService;

    /* raw entity lists — held for saveData() after services are built */
    private static List<Player>      playerList;
    private static List<Admin>       adminList;
    private static List<Hero>        heroList;
    private static List<Equipment>   equipList;
    private static List<Team>        teamList;
    private static List<MatchRecord> matchList;

    public static void main(String[] args) {
        System.out.println("Loading data...");
        loadData();

        System.out.println("Welcome to the Honor of Kings Management System!");
        runLoginLoop();

        // Final save before JVM exit
        saveData();
        System.out.println("Goodbye!");
    }

    /* ---- data loading ---- */
    private static void loadData() {
        // 1. Try CSV files first
        FileService.Data csv = FileService.loadAll();

        if (FileService.hasData(csv)) {
            System.out.println("  Loaded from CSV: " + csv.players.size() + " players, "
                    + csv.heroes.size() + " heroes, "
                    + csv.equipment.size() + " equipment, "
                    + csv.teams.size() + " teams, "
                    + csv.matches.size() + " matches.");

            playerList = csv.players;
            heroList   = csv.heroes;
            equipList  = csv.equipment;
            teamList   = csv.teams;
            matchList  = csv.matches;
            adminList  = new ArrayList<>(csv.admins);
        } else {
            // 2. Fallback: generate sample data
            System.out.println("  No CSV data found — generating sample data.");
            DataInitializer init = new DataInitializer();
            playerList = new ArrayList<>(init.getPlayers());
            heroList   = new ArrayList<>(init.getHeroes());
            equipList  = new ArrayList<>(init.getEquipment());
            teamList   = new ArrayList<>(init.getTeams());
            matchList  = new ArrayList<>(init.getMatches());
            adminList  = new ArrayList<>();

            System.out.println("  Loaded: " + playerList.size() + " players, "
                    + heroList.size() + " heroes, "
                    + equipList.size() + " equipment, "
                    + teamList.size() + " teams, "
                    + matchList.size() + " matches.");
        }

        // 3. Always seed the default admin if no admin exists
        if (adminList.isEmpty()) {
            String adminSalt = UUID.randomUUID().toString().substring(0, 16);
            String adminHash = PasswordHasher.hash(adminSalt, "admin");
            Admin admin = new Admin(UUID.randomUUID().toString(), "admin",
                    adminHash, adminSalt, "Administrator");
            adminList.add(admin);
            System.out.println("  Seeded admin account: admin / admin");
        }

        // 4. Build services
        playerService      = new PlayerService(playerList, heroList, equipList, teamList);
        teamService        = new TeamService(teamList, playerList);
        heroService        = new HeroService(heroList, playerList, equipList);
        equipService       = new EquipmentService(equipList, playerList);
        matchService       = new MatchService(matchList, teamList, playerList, heroList);
        leaderboardService = new LeaderboardService(playerList, teamList);
        authService        = new AuthenticationService(adminList, playerList);
    }

    private static void saveData() {
        FileService.saveAll(playerList, adminList, heroList, equipList, teamList, matchList);
    }

    /* ================================================================
     *  LOGIN LOOP
     * ================================================================ */
    private static void runLoginLoop() {
        while (true) {
            if (attemptLogin()) {
                Person user = authService.getCurrentUser();
                if (user.getRole() == Role.ADMIN) {
                    runAdminMenu();
                } else {
                    runPlayerMenu();
                }
                // logout returns here — loop back to log in
            } else {
                System.out.println("Too many failed attempts. Exiting.");
                return;
            }
        }
    }

    private static boolean attemptLogin() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.println();
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if (authService.login(username, password)) {
                Person user = authService.getCurrentUser();
                System.out.println("Login successful! Welcome, "
                        + user.getNickname() + " (" + user.getRole() + ").");
                return true;
            }

            int remaining = MAX_LOGIN_ATTEMPTS - attempt;
            if (remaining > 0) {
                System.out.println("Invalid credentials. " + remaining
                        + " attempt(s) remaining.");
            }
        }
        return false;
    }

    /* ================================================================
     *  ADMIN MENU — full access to all features
     * ================================================================ */
    private static void runAdminMenu() {
        while (true) {
            System.out.println();
            System.out.println("===== Admin Menu =====");
            System.out.println("1. Player Lookup");
            System.out.println("2. Team Overview");
            System.out.println("3. Hero Details");
            System.out.println("4. Equipment Statistics");
            System.out.println("5. Match History");
            System.out.println("6. Leaderboard");
            System.out.println("7. Data Management");
            System.out.println("8. Logout");
            System.out.println("======================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": handlePlayerLookup();    break;
                case "2": handleTeamOverview();     break;
                case "3": handleHeroDetails();      break;
                case "4": handleEquipmentStats();   break;
                case "5": handleMatchHistory();     break;
                case "6": handleLeaderboard();      break;
                case "7": handleDataManagement();   break;
                case "8":
                    System.out.println("Logging out...");
                    saveData();
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1–8.");
            }
        }
    }

    /* ================================================================
     *  PLAYER MENU — read-only views + edit own nickname
     * ================================================================ */
    private static void runPlayerMenu() {
        while (true) {
            System.out.println();
            System.out.println("===== Player Menu =====");
            System.out.println("1. View My Profile");
            System.out.println("2. View Heroes");
            System.out.println("3. View Teams");
            System.out.println("4. View Leaderboard");
            System.out.println("5. View Match History");
            System.out.println("6. Edit My Nickname");
            System.out.println("7. Logout");
            System.out.println("=======================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": handleViewMyProfile();    break;
                case "2": handleHeroDetails();      break;
                case "3": handleTeamOverview();      break;
                case "4": handleLeaderboard();       break;
                case "5": handleMatchHistory();      break;
                case "6": handleEditOwnNickname();   break;
                case "7":
                    System.out.println("Logging out...");
                    saveData();
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1–7.");
            }
        }
    }

    private static void handleViewMyProfile() {
        Person user = authService.getCurrentUser();
        if (!(user instanceof Player)) {
            System.out.println("Only players can view their profile.");
            return;
        }
        Player self = (Player) user;
        System.out.println();
        System.out.println("--- My Profile ---");
        displayPlayer(self);
    }

    private static void handleEditOwnNickname() {
        Person user = authService.getCurrentUser();
        if (!(user instanceof Player)) {
            System.out.println("Only players can edit their nickname.");
            return;
        }
        Player self = (Player) user;

        System.out.println();
        System.out.println("--- Edit Nickname ---");
        System.out.println("Current nickname: " + self.getNickname());
        System.out.print("New nickname (or 0 to cancel): ");
        String newNick = scanner.nextLine().trim();

        if (newNick.equals("0")) return;
        if (newNick.isEmpty()) {
            System.out.println("Error: nickname cannot be empty.");
            return;
        }
        if (newNick.equals(self.getNickname())) {
            System.out.println("No change — same as current nickname.");
            return;
        }

        boolean ok = playerService.updateNickname(self.getId(), newNick);
        if (ok) {
            System.out.println("Nickname updated to: " + newNick);
        } else {
            System.out.println("Failed to update nickname.");
        }
    }

    /* ================================================================
     *  SHARED HANDLERS (used by both menus)
     * ================================================================ */

    /* ---- player lookup (plan.md §2.1) ---- */
    private static void handlePlayerLookup() {
        System.out.println();
        System.out.println("--- Player Lookup ---");
        System.out.print("Enter player ID or name (or 0 to cancel): ");
        String term = scanner.nextLine().trim();

        if (term.equals("0")) return;
        if (term.isEmpty()) {
            System.out.println("Error: search term cannot be empty.");
            return;
        }

        Optional<Player> result = playerService.findById(term);
        if (result.isEmpty()) {
            result = playerService.findByName(term);
        }

        if (result.isEmpty()) {
            System.out.println("No player found with ID or name: \"" + term + "\"");
            return;
        }

        displayPlayer(result.get());
    }

    private static void displayPlayer(Player p) {
        System.out.println();
        System.out.println("┌──────────────────────────────────────────┐");
        System.out.printf("  ID:       %s%n", p.getId());
        System.out.printf("  Name:     %s%n", p.getNickname());
        System.out.printf("  Team:     %s%n", playerService.getTeamName(p.getTeamId()));
        System.out.printf("  Level:    %d%n", p.getLevel());
        System.out.printf("  Rank:     %s%n", p.getRank());
        System.out.printf("  Win Rate: %.1f%%  (%dW / %dM)%n",
                p.getWinRate(), p.getWinCount(), p.getMatchCount());
        System.out.println("└──────────────────────────────────────────┘");

        System.out.println("  Owned Heroes: " + playerService.formatHeroPool(p.getHeroPool()));

        System.out.println("  Equipped Items:");
        System.out.print(playerService.formatEquippedItems(p.getEquippedItems()));
    }

    /* ---- team overview (plan.md §2.2) ---- */
    private static void handleTeamOverview() {
        System.out.println();

        List<Team> allTeams = teamService.listAll();
        System.out.println("--- All Teams ---");
        System.out.printf("%-4s %-16s %-8s %-10s%n", "#", "Name", "Members", "Total Wins");
        System.out.println("──────────────────────────────────────────");
        for (int i = 0; i < allTeams.size(); i++) {
            Team t = allTeams.get(i);
            System.out.printf("%-4d %-16s %-8d %-10d%n",
                    i + 1, t.getName(),
                    t.getMemberIds().size(),
                    teamService.getTotalWins(t));
        }
        System.out.println();

        System.out.print("Enter team name or # to view details (0 to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return;
        if (input.isEmpty()) {
            System.out.println("Error: input cannot be empty.");
            return;
        }

        Team selected = null;

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < allTeams.size()) {
                selected = allTeams.get(index);
            }
        } catch (NumberFormatException ignored) {}

        if (selected == null) {
            Optional<Team> result = teamService.findById(input);
            if (result.isEmpty()) {
                result = teamService.findByName(input);
            }
            selected = result.orElse(null);
        }

        if (selected == null) {
            System.out.println("No team found with: \"" + input + "\"");
            return;
        }

        displayTeamDetail(selected);
    }

    private static void displayTeamDetail(Team t) {
        List<Player> members = teamService.getMembers(t);

        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.printf("  Team:     %s%n", t.getName());
        System.out.printf("  Rank:     %s%n", t.getRank());
        System.out.printf("  Captain:  %s%n", teamService.getCaptainName(t));
        System.out.printf("  Avg Lvl:  %.1f%n", t.getAverageLevel(members));
        System.out.printf("  Matches:  %d%n", teamService.getTotalMatches(t));
        System.out.printf("  Win Rate: %.1f%%%n", teamService.getTeamWinRate(t));
        System.out.println("└──────────────────────────────────────────────────┘");

        System.out.println();
        System.out.printf("  %-4s %-10s %-6s %-10s %-7s %-8s%n",
                "#", "Name", "Level", "Rank", "WinRate", "Matches");
        System.out.println("  ──────────────────────────────────────────────");
        for (int i = 0; i < members.size(); i++) {
            Player p = members.get(i);
            String marker = p.getId().equals(t.getCaptainId()) ? " ★" : "";
            System.out.printf("  %-4d %-10s %-6d %-10s %6.1f%% %-8d%n",
                    i + 1,
                    p.getNickname() + marker,
                    p.getLevel(),
                    p.getRank(),
                    p.getWinRate(),
                    p.getMatchCount());
        }

        Player top = t.getTopPlayer(members);
        if (top != null) {
            System.out.println();
            System.out.println("  Top Player: " + top.getNickname()
                    + " (WinRate: " + String.format("%.1f%%", top.getWinRate())
                    + ", Level: " + top.getLevel() + ")");
        }
    }

    /* ---- hero details (plan.md §2.3) ---- */
    private static void handleHeroDetails() {
        System.out.println();
        System.out.println("--- Hero Details ---");
        System.out.print("Enter hero ID or name (or 0 to cancel): ");
        String term = scanner.nextLine().trim();

        if (term.equals("0")) return;
        if (term.isEmpty()) {
            System.out.println("Error: search term cannot be empty.");
            return;
        }

        Optional<Hero> result = heroService.findById(term);
        if (result.isEmpty()) {
            result = heroService.findByName(term);
        }

        if (result.isEmpty()) {
            System.out.println("No hero found with ID or name: \"" + term + "\"");
            return;
        }

        displayHero(result.get());
    }

    private static void displayHero(Hero h) {
        System.out.println();
        System.out.println("┌──────────────────────────────────────────┐");
        System.out.printf("  ID:       %s%n", h.getId());
        System.out.printf("  Name:     %s%n", h.getName());
        System.out.printf("  Type:     %s%n", h.getHeroType());
        System.out.printf("  Stats:    %s%n", heroService.formatBaseStats(h));
        System.out.println("└──────────────────────────────────────────┘");

        System.out.println("  Compatible Equipment: " + heroService.formatCompatibleEquipment(h));

        System.out.println("  Owned by: " + heroService.formatOwners(h));
    }

    /* ---- equipment statistics (plan.md §2.4) ---- */
    private static void handleEquipmentStats() {
        System.out.println();
        System.out.println("--- Equipment Usage Ranking ---");

        List<Equipment> ranked = equipService.getRankedEquipment();
        if (ranked.isEmpty()) {
            System.out.println("No equipment data available.");
            return;
        }

        System.out.printf("%-4s %-12s %-8s %-6s%n", "Rank", "Name", "Type", "Used");
        System.out.println("────────────────────────────────────");
        for (int i = 0; i < ranked.size(); i++) {
            Equipment e = ranked.get(i);
            int count = equipService.getUsageCount(e.getId());
            System.out.printf("%-4d %-12s %-8s %-6d%n",
                    i + 1, e.getName(), e.getType(), count);
        }

        long usedCount = ranked.stream()
                .filter(e -> equipService.getUsageCount(e.getId()) > 0)
                .count();
        System.out.println();
        System.out.println("Equipment in use: " + usedCount + " / " + ranked.size());
    }

    /* ---- match history (plan.md §2.5) ---- */
    private static void handleMatchHistory() {
        System.out.println();

        List<MatchRecord> allMatches = matchService.listAll();
        System.out.println("Total matches available: " + allMatches.size());

        System.out.print("Show last N matches (Enter = all, 0 to cancel): ");
        String nInput = scanner.nextLine().trim();
        if (nInput.equals("0")) return;

        List<MatchRecord> toDisplay;
        if (!nInput.isEmpty()) {
            try {
                int n = Integer.parseInt(nInput);
                if (n <= 0) { System.out.println("Invalid number."); return; }
                toDisplay = allMatches.stream().limit(n).collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
                return;
            }
        } else {
            toDisplay = allMatches;
        }

        displayMatchTable(toDisplay);

        System.out.print("Enter team name to filter (Enter to return, 0 to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;
        if (input.equals("0")) return;

        Optional<Team> team = matchService.findTeam(input);
        if (team.isEmpty()) {
            System.out.println("No team found with name: \"" + input + "\"");
            return;
        }

        Team t = team.get();
        List<MatchRecord> filtered = matchService.getByTeamId(t.getId());
        if (filtered.isEmpty()) {
            System.out.println("No matches recorded for team: " + t.getName());
            return;
        }

        System.out.println();
        System.out.println("--- Matches for " + t.getName() + " ---");
        displayMatchTable(filtered);

        // Win/Loss record
        int[] wld = matchService.countWinLoss(t.getId(), filtered);
        System.out.println("Record: " + wld[0] + "W / " + wld[1] + "L / " + wld[2] + "D");

        // Hero pick rates
        Map<String, Double> pickRates = matchService.getHeroPickRate(filtered);
        if (!pickRates.isEmpty()) {
            System.out.println();
            System.out.println("Hero Pick Rates:");
            int rank = 1;
            for (Map.Entry<String, Double> e : pickRates.entrySet()) {
                System.out.printf("  %d. %-8s  %.1f%%%n", rank++, matchService.getHeroName(e.getKey()), e.getValue());
            }
        }
    }

    private static void displayMatchTable(List<MatchRecord> matches) {
        if (matches.isEmpty()) {
            System.out.println("No matches to display.");
            return;
        }

        System.out.printf("%-12s %-22s %-22s %-6s %-10s%n",
                "Date", "Teams", "Result", "MVP", "Duration");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        for (MatchRecord m : matches) {
            String teams = matchService.getTeamName(m.getTeam1Id()) + " vs "
                    + matchService.getTeamName(m.getTeam2Id());
            System.out.printf("%-12s %-22s %-22s %-6s %-10s%n",
                    m.getDate(),
                    teams,
                    matchService.formatResult(m),
                    matchService.getMvpName(m),
                    m.getDurationMinutes() + " min");
            System.out.println("  Picks: " + matchService.formatHeroPicks(m));
        }

        System.out.println();
        System.out.println("Total: " + matches.size() + " match(es)");
    }

    /* ---- leaderboard (plan.md §2.6) ---- */
    private static void handleLeaderboard() {
        System.out.println();
        System.out.println("--- Player Leaderboard ---");
        System.out.println("Rank by:");
        System.out.println("  1. Win Rate");
        System.out.println("  2. Total Wins");
        System.out.println("  3. Player Level");
        System.out.print("Choice (0 to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.equals("0")) return;

        List<Player> top;
        String dimension;
        switch (input) {
            case "1":
                top = leaderboardService.topByWinRate(10);
                dimension = "Win Rate";
                break;
            case "2":
                top = leaderboardService.topByWins(10);
                dimension = "Total Wins";
                break;
            case "3":
                top = leaderboardService.topByLevel(10);
                dimension = "Level";
                break;
            default:
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                return;
        }

        if (top.isEmpty()) {
            System.out.println("No player data available.");
            return;
        }

        System.out.println();
        System.out.println("Top " + top.size() + " Players by " + dimension);
        System.out.printf("%-4s %-10s %-14s %-12s%n",
                "Rank", "Name", "Team", dimension);
        System.out.println("────────────────────────────────────────");

        for (int i = 0; i < top.size(); i++) {
            Player p = top.get(i);
            String teamName = leaderboardService.getTeamName(p.getTeamId());
            String value = "";
            switch (input) {
                case "1": value = String.format("%.1f%%", p.getWinRate()); break;
                case "2": value = String.valueOf(p.getWinCount());         break;
                case "3": value = String.valueOf(p.getLevel());           break;
            }
            System.out.printf("%-4d %-10s %-14s %-12s%n",
                    i + 1, p.getNickname(), teamName, value);
        }
    }

    /* ================================================================
     *  DATA MANAGEMENT SUBMENU (Admin only — §5.7)
     * ================================================================ */
    private static void handleDataManagement() {
        while (true) {
            System.out.println();
            System.out.println("--- Data Management ---");
            System.out.println("  1. Add Player        2. Delete Player");
            System.out.println("  3. Add Hero          4. Delete Hero");
            System.out.println("  5. Add Equipment     6. Delete Equipment");
            System.out.println("  7. Add Team          8. Delete Team");
            System.out.println("  9. Add Match        10. Delete Match");
            System.out.println("  0. Back to Admin Menu");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":  handleAddPlayer();      break;
                case "2":  handleDeletePlayer();   break;
                case "3":  handleAddHero();        break;
                case "4":  handleDeleteHero();     break;
                case "5":  handleAddEquipment();   break;
                case "6":  handleDeleteEquipment();break;
                case "7":  handleAddTeam();        break;
                case "8":  handleDeleteTeam();     break;
                case "9":  handleAddMatch();       break;
                case "10": handleDeleteMatch();    break;
                case "0":  return;
                default:
                    System.out.println("Invalid option. Please enter 0–10.");
            }
        }
    }

    /* ---- add player ---- */
    private static void handleAddPlayer() {
        System.out.println();
        System.out.println("--- Add Player ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) { System.out.println("Cancelled."); return; }

        System.out.print("Nickname: ");
        String nickname = scanner.nextLine().trim();
        if (nickname.isEmpty()) { System.out.println("Cancelled."); return; }

        // Check duplicates
        if (playerService.findByName(nickname).isPresent()) {
            System.out.println("Error: a player with that nickname already exists.");
            return;
        }

        System.out.print("Password (default 123456): ");
        String pw = scanner.nextLine().trim();
        if (pw.isEmpty()) pw = "123456";

        String id   = UUID.randomUUID().toString();
        String salt = UUID.randomUUID().toString().substring(0, 16);
        String hash = PasswordHasher.hash(salt, pw);

        Player p = new Player(id, username, hash, salt, nickname);
        playerService.addPlayer(p);
        playerList.add(p);
        System.out.println("Player added. ID: " + id);
    }

    /* ---- delete player ---- */
    private static void handleDeletePlayer() {
        System.out.println();
        System.out.println("--- Delete Player ---");
        List<Player> all = playerService.listAll();
        for (int i = 0; i < all.size(); i++) {
            Player p = all.get(i);
            System.out.printf("  %d. %s  (%s)%n", i + 1, p.getNickname(), p.getUsername());
        }
        System.out.print("Enter name or ID of player to delete (0 to cancel): ");
        String term = scanner.nextLine().trim();
        if (term.equals("0")) return;

        Optional<Player> opt = playerService.findById(term);
        if (opt.isEmpty()) opt = playerService.findByName(term);
        if (opt.isEmpty()) {
            System.out.println("Player not found.");
            return;
        }
        Player p = opt.get();
        System.out.print("Delete " + p.getNickname() + " (" + p.getUsername()
                + ")? Type YES to confirm: ");
        if (!scanner.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        playerService.deletePlayer(p.getId());
        playerList.remove(p);
        System.out.println("Player deleted.");
    }

    /* ---- add hero ---- */
    private static void handleAddHero() {
        System.out.println();
        System.out.println("--- Add Hero ---");
        System.out.print("Hero name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Cancelled."); return; }

        if (heroService.findByName(name).isPresent()) {
            System.out.println("Error: a hero with that name already exists.");
            return;
        }

        System.out.println("Types: WARRIOR, MAGE, ASSASSIN, MARKSMAN, SUPPORT, TANK");
        System.out.print("Hero type: ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        HeroType type;
        try { type = HeroType.valueOf(typeStr); } catch (IllegalArgumentException e) {
            System.out.println("Invalid hero type.");
            return;
        }

        System.out.print("Base Attack: ");
        int atk = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Base Defense: ");
        int def = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Base HP: ");
        int hp = Integer.parseInt(scanner.nextLine().trim());

        Hero h = new Hero(UUID.randomUUID().toString(), name, type, atk, def, hp);
        heroService.addHero(h);
        heroList.add(h);
        System.out.println("Hero added. ID: " + h.getId());
    }

    /* ---- delete hero ---- */
    private static void handleDeleteHero() {
        System.out.println();
        System.out.println("--- Delete Hero ---");
        List<Hero> all = heroService.listAll();
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("  %d. %s  (%s)%n", i + 1, all.get(i).getName(), all.get(i).getHeroType());
        }
        System.out.print("Enter name or ID of hero to delete (0 to cancel): ");
        String term = scanner.nextLine().trim();
        if (term.equals("0")) return;

        Optional<Hero> opt = heroService.findById(term);
        if (opt.isEmpty()) opt = heroService.findByName(term);
        if (opt.isEmpty()) {
            System.out.println("Hero not found.");
            return;
        }
        Hero h = opt.get();
        System.out.print("Delete " + h.getName() + "? Type YES to confirm: ");
        if (!scanner.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        heroService.deleteHero(h.getId());
        heroList.remove(h);
        System.out.println("Hero deleted.");
    }

    /* ---- add equipment ---- */
    private static void handleAddEquipment() {
        System.out.println();
        System.out.println("--- Add Equipment ---");
        System.out.print("Equipment name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Cancelled."); return; }

        System.out.println("Types: ATTACK, DEFENSE, MAGIC, MOVEMENT, JUNGLE, SUPPORT");
        System.out.print("Equipment type: ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        EquipmentType type;
        try { type = EquipmentType.valueOf(typeStr); } catch (IllegalArgumentException e) {
            System.out.println("Invalid equipment type.");
            return;
        }

        System.out.print("Price: ");
        int price = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Attack bonus: ");
        int atk = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Defense bonus: ");
        int def = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Magic bonus: ");
        int mag = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();

        Equipment e = new Equipment(UUID.randomUUID().toString(), name, type,
                price, atk, def, mag, desc);
        equipService.addEquipment(e);
        equipList.add(e);
        System.out.println("Equipment added. ID: " + e.getId());
    }

    /* ---- delete equipment ---- */
    private static void handleDeleteEquipment() {
        System.out.println();
        System.out.println("--- Delete Equipment ---");
        List<Equipment> ranked = equipService.getRankedEquipment();
        for (int i = 0; i < ranked.size(); i++) {
            Equipment e = ranked.get(i);
            System.out.printf("  %d. %s  (%s, used %d)%n",
                    i + 1, e.getName(), e.getType(), equipService.getUsageCount(e.getId()));
        }
        System.out.print("Enter equipment ID to delete (0 to cancel): ");
        String term = scanner.nextLine().trim();
        if (term.equals("0")) return;

        boolean removed = equipService.deleteEquipment(term);
        if (!removed) {
            System.out.println("Equipment not found.");
            return;
        }
        equipList.removeIf(e -> e.getId().equals(term));
        System.out.println("Equipment deleted.");
    }

    /* ---- add team ---- */
    private static void handleAddTeam() {
        System.out.println();
        System.out.println("--- Add Team ---");
        System.out.print("Team name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Cancelled."); return; }

        if (teamService.findByName(name).isPresent()) {
            System.out.println("Error: a team with that name already exists.");
            return;
        }

        System.out.print("Captain player ID or name: ");
        String capTerm = scanner.nextLine().trim();
        Optional<Player> cap = playerService.findById(capTerm);
        if (cap.isEmpty()) cap = playerService.findByName(capTerm);
        if (cap.isEmpty()) {
            System.out.println("Captain not found.");
            return;
        }

        Team t = new Team(UUID.randomUUID().toString(), name,
                cap.get().getId(), Rank.BRONZE);
        cap.get().setTeamId(t.getId());

        teamService.addTeam(t);
        teamList.add(t);
        System.out.println("Team added. ID: " + t.getId());
        System.out.println("Use 'Delete Team' option to add/remove members later, "
                + "or add members via team ID editing.");
    }

    /* ---- delete team ---- */
    private static void handleDeleteTeam() {
        System.out.println();
        System.out.println("--- Delete Team ---");
        List<Team> all = teamService.listAll();
        for (int i = 0; i < all.size(); i++) {
            Team t = all.get(i);
            System.out.printf("  %d. %s  (%d members)%n",
                    i + 1, t.getName(), t.getMemberIds().size());
        }
        System.out.print("Enter name or ID of team to delete (0 to cancel): ");
        String term = scanner.nextLine().trim();
        if (term.equals("0")) return;

        Optional<Team> opt = teamService.findById(term);
        if (opt.isEmpty()) opt = teamService.findByName(term);
        if (opt.isEmpty()) {
            System.out.println("Team not found.");
            return;
        }
        Team t = opt.get();
        System.out.print("Delete " + t.getName() + "? Type YES to confirm: ");
        if (!scanner.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        teamService.deleteTeam(t.getId());
        teamList.remove(t);
        System.out.println("Team deleted.");
    }

    /* ---- add match ---- */
    private static void handleAddMatch() {
        System.out.println();
        System.out.println("--- Add Match ---");

        // List teams for reference
        List<Team> allTeams = teamService.listAll();
        System.out.println("Available teams:");
        for (Team t : allTeams) {
            System.out.printf("  %s  →  %s%n", t.getId(), t.getName());
        }
        System.out.println();

        System.out.print("Team 1 ID: ");
        String t1Id = scanner.nextLine().trim();
        if (teamService.findById(t1Id).isEmpty()) {
            System.out.println("Team 1 not found.");
            return;
        }
        System.out.print("Team 2 ID: ");
        String t2Id = scanner.nextLine().trim();
        if (teamService.findById(t2Id).isEmpty()) {
            System.out.println("Team 2 not found.");
            return;
        }

        System.out.print("Result (WIN / LOSE / DRAW): ");
        MatchResult result;
        try { result = MatchResult.valueOf(scanner.nextLine().trim().toUpperCase()); }
        catch (IllegalArgumentException e) {
            System.out.println("Invalid result.");
            return;
        }

        System.out.print("MVP player ID: ");
        String mvpId = scanner.nextLine().trim();
        System.out.print("Duration (minutes): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());

        MatchRecord m = new MatchRecord(UUID.randomUUID().toString(),
                LocalDate.now(), t1Id, t2Id, result, mvpId, duration);

        // Optional hero picks
        System.out.println("Add hero picks? (playerId:heroId pairs, empty line to finish):");
        while (true) {
            System.out.print("  playerId:heroId (or Enter to finish): ");
            String pair = scanner.nextLine().trim();
            if (pair.isEmpty()) break;
            String[] parts = pair.split(":");
            if (parts.length == 2) m.addHeroPick(parts[0].trim(), parts[1].trim());
        }

        matchService.addMatch(m);
        matchList.add(m);
        System.out.println("Match added. ID: " + m.getId());
    }

    /* ---- delete match ---- */
    private static void handleDeleteMatch() {
        System.out.println();
        System.out.println("--- Delete Match ---");
        List<MatchRecord> all = matchService.listAll();
        displayMatchTable(all);

        System.out.print("Enter match ID to delete (0 to cancel): ");
        String term = scanner.nextLine().trim();
        if (term.equals("0")) return;

        if (matchService.findById(term).isEmpty()) {
            System.out.println("Match not found.");
            return;
        }
        System.out.print("Delete this match? Type YES to confirm: ");
        if (!scanner.nextLine().trim().equals("YES")) {
            System.out.println("Cancelled.");
            return;
        }
        matchService.deleteMatch(term);
        matchList.removeIf(m -> m.getId().equals(term));
        System.out.println("Match deleted.");
    }
}
