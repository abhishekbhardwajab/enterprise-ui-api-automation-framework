package com.automation.framework.context;

import com.automation.framework.model.Account;
import com.automation.framework.model.Title;
import com.automation.framework.utils.CsvUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user store, seeded once from data/users.csv on class
 * initialization and keyed by email. Accounts created at runtime (e.g. via
 * the signup flow) can be registered here too, so later steps or scenarios
 * can look them up the same way as a seeded record.
 */
public final class UserRepository {

    private static final Logger log = LogManager.getLogger(UserRepository.class);
    private static final Map<String, Account> USERS = new ConcurrentHashMap<>();

    static {
        loadSeedUsers();
    }

    private UserRepository() {
    }

    private static void loadSeedUsers() {
        for (Map<String, String> row : CsvUtils.readRows(Constants.USERS_CSV_PATH)) {
            Account account = new Account(
                    Title.valueOf(row.getOrDefault("title", "MR").toUpperCase()),
                    row.get("firstName"),
                    row.get("lastName"),
                    row.get("email"),
                    row.get("password"),
                    row.get("address"),
                    row.get("state"),
                    row.get("city"),
                    row.get("zipcode"),
                    row.get("mobileNumber"));
            USERS.put(account.email, account);
        }
        log.info("UserRepository seeded with {} account(s) from {}", USERS.size(), Constants.USERS_CSV_PATH);
    }

    /** Registers a newly created account (e.g. from the signup flow) for later lookup. */
    public static void register(Account account) {
        USERS.put(account.email, account);
        log.info("Registered new account in UserRepository: {}", account.email);
    }

    public static Optional<Account> findByEmail(String email) {
        return Optional.ofNullable(USERS.get(email));
    }

    public static int size() {
        return USERS.size();
    }
}
