package model;

import enums.Role;

import java.time.LocalDateTime;

/**
 * Abstract base class for all human actors in the system.
 *
 * Design choices:
 * - "abstract" because a Person should never be instantiated directly —
 *   every real user is either a Player or an Admin.
 * - "authenticated" is a transient session flag. It lives in memory only
 *   (not saved to CSV) and is reset on logout or program exit.
 * - The password salt & hash are stored together so AuthService can verify
 *   credentials without exposing plain-text passwords.
 */
public abstract class Person {

    /* ---- fields ---- */
    private String id;
    private String username;
    private String salt;            // random per-user salt for password hashing
    private String hashedPassword;  // SHA-256(salt + plainPassword)
    private String nickname;
    private LocalDateTime registrationDate;
    private boolean authenticated;  // session flag, not persisted

    /* ---- constructor ---- */
    /**
     * @param id              Unique identifier (usually a UUID generated before calling).
     * @param username        Login name — must be unique across all users.
     * @param salt            Per-user salt string.
     * @param hashedPassword  Already-hashed password string.
     * @param nickname        Display name shown in the UI.
     */
    public Person(String id, String username, String salt,
                  String hashedPassword, String nickname) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.hashedPassword = hashedPassword;
        this.nickname = nickname;
        this.registrationDate = LocalDateTime.now();
        this.authenticated = false;      // must log in first
    }

    /* ---- abstract ---- */
    /**
     * Each subclass must return its Role so the menu system can branch
     * between Player actions and Admin actions without using instanceof.
     */
    public abstract Role getRole();

    /* ---- session helpers ---- */
    /** Called by AuthService after successful credential check. */
    public void login() {
        this.authenticated = true;
    }

    /** Clears the session flag. */
    public void logout() {
        this.authenticated = false;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Updates the password hash. Old-password verification is done by
     * AuthService before calling this method.
     */
    public void changePassword(String newSalt, String newHashedPassword) {
        this.salt = newSalt;
        this.hashedPassword = newHashedPassword;
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSalt() { return salt; }

    public String getHashedPassword() { return hashedPassword; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
}
