package enums;

/**
 * Represents the role of a user in the system.
 * Used by Person subclasses to determine permissions.
 */
public enum Role {
    PLAYER,  // Regular game player — can view own data and browse
    ADMIN    // System administrator — can manage all data
}
