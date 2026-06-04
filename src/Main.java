import model.*;
import service.PlayerService;
import util.DataInitializer;

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
 *
 * Future: admin menu, team overview, hero details, leaderboard, etc.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    /* shared state */
    private static PlayerService playerService;

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
            System.out.println("0. Exit");
            System.out.println("============================================");
            System.out.print("Choice > ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handlePlayerLookup();
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1 or 0.");
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
}
