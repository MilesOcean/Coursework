package interfaces;

/**
 * Contract for any class that can authenticate.
 * Implemented by Person — so both Player and Admin inherit it.
 */
public interface Authenticatable {
    /**
     * Called by AuthService after external credential verification.
     * @return true once the session flag is set.
     */
    boolean login(String username, String password);

    /** Clears the in-memory session flag. */
    void logout();

    /** @return true if the user is currently logged in. */
    boolean isAuthenticated();
}
