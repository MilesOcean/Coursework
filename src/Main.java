import enums.Role;
import model.*;
import service.*;
import util.DataInitializer;
import util.PasswordHasher;

import java.util.*;

/**
 * Entry point — console-based menu for the Honor of Kings system.
 *
 * Architecture (from design.md §4):
 *   Main (CLI) → Service layer → Model layer
 *
 * Features:
 *   - Login with 3-attempt limit, role-based menus
 *   - Admin: full CRUD access (player, team, hero, equipment, match, leaderboard)
 *   - Player: read-only views + edit own nickname
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

    public static void main(String[] args) {
        System.out.println("Loading data...");
        loadData();

        System.out.println("Welcome to the Honor of Kings Management System!");
        runLoginLoop();
    }

    /* ---- data loading ---- */
    private static void loadData() {
        DataInitializer init = new DataInitializer();
        playerService = new PlayerService(
                init.getPlayers(),
                init.getHeroes(),
                init.getEquipment(),
                init.getTeams());
        teamService = new TeamService(init.getTeams(), init.getPlayers());
        heroService = new HeroService(init.getHeroes(), init.getPlayers(),
                init.getEquipment());
        equipService = new EquipmentService(init.getEquipment(), init.getPlayers());
        matchService = new MatchService(init.getMatches(), init.getTeams(),
                init.getPlayers(), init.getHeroes());
        leaderboardService = new LeaderboardService(init.getPlayers(), init.getTeams());

        // Seed one admin account
        String adminSalt = UUID.randomUUID().toString().substring(0, 16);
        String adminHash = PasswordHasher.hash(adminSalt, "admin");
        Admin admin = new Admin(UUID.randomUUID().toString(), "admin",
                adminHash, adminSalt, "Administrator");
        List<Admin> admins = List.of(admin);

        authService = new AuthenticationService(admins, init.getPlayers());

        System.out.println("  Loaded: " + init.getPlayers().size() + " players, "
                + init.getHeroes().size() + " heroes, "
                + init.getEquipment().size() + " equipment, "
                + init.getTeams().size() + " teams, "
                + init.getMatches().size() + " matches.");
        System.out.println("  Admin account: admin / admin");
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
                // logout returns here — loop back to login
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
            System.out.println("7. Logout");
            System.out.println("======================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": handlePlayerLookup();  break;
                case "2": handleTeamOverview();   break;
                case "3": handleHeroDetails();    break;
                case "4": handleEquipmentStats(); break;
                case "5": handleMatchHistory();   break;
                case "6": handleLeaderboard();    break;
                case "7":
                    System.out.println("Logging out...");
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1–7.");
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
            System.out.println("1. View Heroes");
            System.out.println("2. View Teams");
            System.out.println("3. View Leaderboard");
            System.out.println("4. View Match History");
            System.out.println("5. Edit My Nickname");
            System.out.println("6. Logout");
            System.out.println("=======================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": handleHeroDetails();    break;
                case "2": handleTeamOverview();     break;
                case "3": handleLeaderboard();      break;
                case "4": handleMatchHistory();     break;
                case "5": handleEditOwnNickname();  break;
                case "6":
                    System.out.println("Logging out...");
                    authService.logout();
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1–6.");
            }
        }
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
        displayMatchTable(allMatches);

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
}
