package model;

import enums.Role;
import interfaces.Authenticatable;

/**
 * Abstract base class for all human actors.
 *
 * Design decisions (from design.md §2.1, §6):
 * - passwordHash + salt are stored; AuthService handles verification.
 * - "authenticated" is a transient in-memory flag — never written to CSV.
 * - login() is called by AuthService AFTER external credential check.
 */
public abstract class Person implements Authenticatable {

    private String id;
    private String username;
    private String passwordHash;
    private String salt;
    private String nickname;

    /** Transient session flag — true after AuthService calls login(). */
    private boolean authenticated;

    public Person(String id, String username, String passwordHash,
                  String salt, String nickname) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.nickname = nickname;
        this.authenticated = false;
    }

    /* ---- abstract ---- */
    public abstract Role getRole();

    /* ---- Authenticatable implementation ---- */
    /**
     * Sets the session flag. AuthService MUST verify the password hash
     * against the stored salt+hash BEFORE calling this method.
     */
    @Override
    public boolean login(String username, String password) {
        this.authenticated = true;
        return true;
    }

    @Override
    public void logout() {
        this.authenticated = false;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    /* ---- getters / setters ---- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
