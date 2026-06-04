import model.*;
import service.PlayerService;
import service.TeamService;
import util.DataInitializer;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point — console-based menu for the Honor of Kings system.
 *
 * Architecture (from design.md §4):
 *   Main (CLI) → Service layer → Model layer
 *
 * Currently implemented:
 *   - Player lookup (by ID or name) — plan.md §2.1
 *   - Team overview (list all + roster detail) — plan.md §2.2
 *
 * Future: admin menu, team overview, hero details, leaderboard, etc.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    /* shared state */
    private static PlayerService playerService;
    private static TeamService   teamService;

    public static void main(String[] args) {
        System.out.println("Loading data...");
        loadData();

        System.out.println("Welcome to the Honor of Kings Management System!");
        runMainMenu();
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
        System.out.println("  Loaded: " + init.getPlayers().size() + " players, "
                + init.getHeroes().size() + " heroes, "
                + init.getEquipment().size() + " equipment, "
                + init.getTeams().size() + " teams, "
                + init.getMatches().size() + " matches.");
    }

    /* ---- main menu ---- */
    private static void runMainMenu() {
        while (true) {
            System.out.println();
            System.out.println("===== Honor of Kings Management System =====");
            System.out.println("1. Player Lookup");
            System.out.println("2. Team Overview");
            System.out.println("0. Exit");
            System.out.println("============================================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handlePlayerLookup();
                    break;
                case "2":
                    handleTeamOverview();
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, or 0.");
            }
        }
    }

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

        // Try ID first (UUID), then name (nickname)
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

    /* ---- display ---- */
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

        // Owned heroes
        System.out.println("  Owned Heroes: " + playerService.formatHeroPool(p.getHeroPool()));

        // Equipped items per hero
        System.out.println("  Equipped Items:");
        System.out.print(playerService.formatEquippedItems(p.getEquippedItems()));
    }

    /* ---- team overview (plan.md §2.2) ---- */
    private static void handleTeamOverview() {
        System.out.println();

        // Step 1 — list all teams
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

        // Step 2 — select one team
        System.out.print("Enter team name or # to view details (0 to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return;
        if (input.isEmpty()) {
            System.out.println("Error: input cannot be empty.");
            return;
        }

        Team selected = null;

        // Try selection by number
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < allTeams.size()) {
                selected = allTeams.get(index);
            }
        } catch (NumberFormatException ignored) {
            // not a number — try name search
        }

        // Try by ID or name
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

        // Member roster
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

        // Top player
        Player top = t.getTopPlayer(members);
        if (top != null) {
            System.out.println();
            System.out.println("  Top Player: " + top.getNickname()
                    + " (WinRate: " + String.format("%.1f%%", top.getWinRate())
                    + ", Level: " + top.getLevel() + ")");
        }
    }
}
