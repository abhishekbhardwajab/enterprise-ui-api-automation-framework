package com.automation.framework.utils;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IMAP mailbox helper: connects to an inbox, polls for the most recent
 * message matching a predicate (e.g. a verification/confirmation email),
 * parses multipart content into plain text, and extracts the first URL
 * found in the body (typically a verification link).
 *
 * Not currently wired into any feature in this repo - automationexercise.com
 * has no email-verification flow - but kept as a reusable capability for
 * applications that do gate signup/reset flows behind a mailbox check.
 */
public final class MailUtils {

    private static final Logger log = LogManager.getLogger(MailUtils.class);
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\-./?=&%#:+]+");

    private MailUtils() {
    }

    private static Store connect(String host, int port, String username, String password) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        return store;
    }

    /**
     * Polls the given folder (e.g. "INBOX") for the newest message matching
     * {@code subjectContains}, checking every {@code pollInterval} until
     * {@code timeout} elapses.
     */
    public static Optional<Message> pollForMessage(String host, int port, String username, String password,
                                                     String folderName, String subjectContains,
                                                     Duration timeout, Duration pollInterval) {
        Instant deadline = Instant.now().plus(timeout);

        Store store = null;
        Folder folder = null;
        try {
            store = connect(host, port, username, password);
            folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);

            while (Instant.now().isBefore(deadline)) {
                Message[] messages = folder.getMessages();
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    if (message.getSubject() != null && message.getSubject().contains(subjectContains)) {
                        log.info("Matched mailbox message with subject [{}]", message.getSubject());
                        return Optional.of(message);
                    }
                }
                Thread.sleep(pollInterval.toMillis());
                folder.close(false);
                folder = store.getFolder(folderName);
                folder.open(Folder.READ_ONLY);
            }
        } catch (MessagingException | InterruptedException e) {
            log.error("Failed while polling mailbox [{}] on {}", folderName, host, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } finally {
            closeQuietly(folder, store);
        }

        log.warn("No message with subject containing [{}] found within {}", subjectContains, timeout);
        return Optional.empty();
    }

    private static void closeQuietly(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (MessagingException ignored) {
            // best-effort cleanup
        }
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException ignored) {
            // best-effort cleanup
        }
    }

    /** Extracts plain-text (or HTML, as a fallback) content from a possibly-multipart message. */
    public static String extractBody(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            }
            if (content instanceof Multipart) {
                return extractFromMultipart((Multipart) content);
            }
        } catch (MessagingException | IOException e) {
            log.error("Failed to extract message body", e);
        }
        return "";
    }

    private static String extractFromMultipart(Multipart multipart) throws MessagingException, IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            Part part = multipart.getBodyPart(i);
            if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
                builder.append(part.getContent());
            } else if (part.getContent() instanceof Multipart) {
                builder.append(extractFromMultipart((Multipart) part.getContent()));
            }
        }
        return builder.toString();
    }

    /** Returns the first http(s) URL found in the given text, if any - typically a verification link. */
    public static Optional<String> extractFirstUrl(String content) {
        if (content == null) {
            return Optional.empty();
        }
        Matcher matcher = URL_PATTERN.matcher(content);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }
}
