package service;

import model.Admin;
import model.Person;
import model.Player;

import java.util.*;

/**
 * Manages login sessions — a single currentUser at a time.
 *
 * Design decisions (from plan.md §4.3):
 * - currentUser is a Person reference so it can hold either a Player or Admin.
 * - login() searches admins first, then players, by username.
 * - logout() clears the session flag on the Person and sets currentUser to null.
 */
public class AuthenticationService {

    private Person currentUser;

    private final Map<String, Admin>  adminMap;   // username (lowercase) → Admin
    private final Map<String, Player> playerMap;  // username (lowercase) → Player

    public AuthenticationService(List<Admin> admins, List<Player> players) {
        this.adminMap = new HashMap<>();
        if (admins != null) {
            for (Admin a : admins) {
                adminMap.put(a.getUsername().toLowerCase(), a);
            }
        }
        this.playerMap = new HashMap<>();
        if (players != null) {
            for (Player p : players) {
                playerMap.put(p.getUsername().toLowerCase(), p);
            }
        }
        this.currentUser = null;
    }

    /**
     * Attempts login by username+password. Searches admins first, then players.
     *
     * @return true if credentials are valid and the session is now active
     */
    public boolean login(String username, String password) {
        if (username == null || username.isBlank()) return false;
        if (password == null) return false;

        String key = username.trim().toLowerCase();

        // Try admins first
        Person person = adminMap.get(key);
        if (person == null) {
            // Then players
            person = playerMap.get(key);
        }
        if (person == null) {
            return false;
        }

        if (person.login(person.getUsername(), password)) {
            this.currentUser = person;
            return true;
        }
        return false;
    }

    /** Ends the current session. */
    public void logout() {
        if (currentUser != null) {
            currentUser.logout();
            currentUser = null;
        }
    }

    /** @return the currently authenticated user, or null */
    public Person getCurrentUser() {
        return currentUser;
    }

    /** @return true if a user is currently logged in */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
