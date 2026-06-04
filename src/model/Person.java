package model;

import enums.Role;
import interfaces.Authenticatable;
import util.PasswordHasher;

/**
 * Abstract base class for all human actors.
 *
 * Design decisions (from design.md §2.1, §6):
 * - passwordHash + salt are stored.
 * - "authenticated" is a transient in-memory flag — never written to CSV.
 * - login() computes SHA-256(salt + password) and compares to passwordHash.
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
     * Verifies credentials by computing SHA-256(salt + password) and comparing
     * to the stored passwordHash. Username must also match (case-insensitive).
     * Sets the session flag on success.
     */
    @Override
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        if (!this.username.equalsIgnoreCase(username.trim())) return false;
        String computed = PasswordHasher.hash(this.salt, password);
        if (computed.equals(this.passwordHash)) {
            this.authenticated = true;
            return true;
        }
        return false;
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
