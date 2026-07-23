package com.automation.framework.context;

import com.automation.framework.model.Account;

/**
 * Holds the currently authenticated Account for the running scenario.
 *
 * Backed by a ThreadLocal rather than a plain static field so parallel
 * scenario execution (see the "parallel"/"parallelUnlimited" Maven
 * profiles) doesn't let one thread's logged-in user leak into another's
 * assertions. Hooks clears this before every scenario.
 */
public final class UserContext {

    private static final ThreadLocal<Account> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(Account account) {
        CURRENT_USER.set(account);
    }

    public static Account getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static boolean hasCurrentUser() {
        return CURRENT_USER.get() != null;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
