package vn.edu.hcmus.mail.database;

import vn.edu.hcmus.mail.model.Email;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EmailCache {
    private static EmailCache instance;
    private final int MAX_CACHE_SIZE = 100;
    private final Map<String, Email> sentCache;
    private final Map<String, Email> receivedCache;
    private final LinkedHashMap<String, Long> sentAccessOrder;
    private final LinkedHashMap<String, Long> receivedAccessOrder;
    private final EmailRepository repository;

    private EmailCache() {
        this.repository = new EmailRepository();
        this.sentCache = new ConcurrentHashMap<>();
        this.receivedCache = new ConcurrentHashMap<>();
        this.sentAccessOrder = new LinkedHashMap<>(16, 0.75f, true);
        this.receivedAccessOrder = new LinkedHashMap<>(16, 0.75f, true);
        loadFromDatabase();
    }

    public static synchronized EmailCache getInstance() {
        if (instance == null) {
            instance = new EmailCache();
        }
        return instance;
    }

    private void loadFromDatabase() {
        List<Email> sentEmails = repository.getAllSentEmails();
        for (Email email : sentEmails) {
            if (sentCache.size() < MAX_CACHE_SIZE) {
                sentCache.put(email.getMsgId(), email);
                sentAccessOrder.put(email.getMsgId(), System.currentTimeMillis());
            }
        }

        List<Email> receivedEmails = repository.getAllReceivedEmails();
        for (Email email : receivedEmails) {
            if (receivedCache.size() < MAX_CACHE_SIZE) {
                receivedCache.put(email.getMsgId(), email);
                receivedAccessOrder.put(email.getMsgId(), System.currentTimeMillis());
            }
        }
    }

    public void cacheSentEmail(Email email) {
        if (sentCache.size() >= MAX_CACHE_SIZE) {
            evictOldestSent();
        }
        sentCache.put(email.getMsgId(), email);
        sentAccessOrder.put(email.getMsgId(), System.currentTimeMillis());
        repository.saveSentEmail(email);
    }

    public void cacheReceivedEmail(Email email) {
        if (receivedCache.size() >= MAX_CACHE_SIZE) {
            evictOldestReceived();
        }
        receivedCache.put(email.getMsgId(), email);
        receivedAccessOrder.put(email.getMsgId(), System.currentTimeMillis());
        repository.saveReceivedEmail(email);
    }

    public Optional<Email> getSentEmail(String msgId) {
        if (sentCache.containsKey(msgId)) {
            sentAccessOrder.put(msgId, System.currentTimeMillis());
            return Optional.of(sentCache.get(msgId));
        }
        Optional<Email> email = repository.getSentEmailByMsgId(msgId);
        email.ifPresent(e -> {
            if (sentCache.size() < MAX_CACHE_SIZE) {
                sentCache.put(msgId, e);
                sentAccessOrder.put(msgId, System.currentTimeMillis());
            }
        });
        return email;
    }

    public Optional<Email> getReceivedEmail(String msgId) {
        if (receivedCache.containsKey(msgId)) {
            receivedAccessOrder.put(msgId, System.currentTimeMillis());
            return Optional.of(receivedCache.get(msgId));
        }
        Optional<Email> email = repository.getReceivedEmailByMsgId(msgId);
        email.ifPresent(e -> {
            if (receivedCache.size() < MAX_CACHE_SIZE) {
                receivedCache.put(msgId, e);
                receivedAccessOrder.put(msgId, System.currentTimeMillis());
            }
        });
        return email;
    }

    public List<Email> getRecentSentEmails(int count) {
        return sentCache.values().stream()
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Email> getRecentReceivedEmails(int count) {
        return receivedCache.values().stream()
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Email> getAllSentEmails() {
        return new ArrayList<>(sentCache.values());
    }

    public List<Email> getAllReceivedEmails() {
        return new ArrayList<>(receivedCache.values());
    }

    public List<Email> searchSentEmails(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return sentCache.values().stream()
                .filter(e -> matchesKeyword(e, lowerKeyword))
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<Email> searchReceivedEmails(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return receivedCache.values().stream()
                .filter(e -> matchesKeyword(e, lowerKeyword))
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Email email, String keyword) {
        return (email.getSubject() != null && email.getSubject().toLowerCase().contains(keyword))
                || (email.getBody() != null && email.getBody().toLowerCase().contains(keyword))
                || (email.getToEmail() != null && email.getToEmail().toLowerCase().contains(keyword))
                || (email.getFromEmail() != null && email.getFromEmail().toLowerCase().contains(keyword));
    }

    public void removeSentEmail(String msgId) {
        sentCache.remove(msgId);
        sentAccessOrder.remove(msgId);
        repository.deleteEmailByMsgId(msgId, Email.EmailType.SENT);
    }

    public void removeReceivedEmail(String msgId) {
        receivedCache.remove(msgId);
        receivedAccessOrder.remove(msgId);
        repository.deleteEmailByMsgId(msgId, Email.EmailType.RECEIVED);
    }

    public void markReceivedAsRead(String msgId) {
        Email email = receivedCache.get(msgId);
        if (email != null) {
            email.setRead(true);
            repository.markAsRead(email.getId());
        }
    }

    public int getUnreadCount() {
        return (int) receivedCache.values().stream().filter(e -> !e.isRead()).count();
    }

    public void clearCache() {
        sentCache.clear();
        receivedCache.clear();
        sentAccessOrder.clear();
        receivedAccessOrder.clear();
    }

    public void refreshFromDatabase() {
        clearCache();
        loadFromDatabase();
    }

    private void evictOldestSent() {
        if (!sentAccessOrder.isEmpty()) {
            String oldestKey = sentAccessOrder.entrySet().iterator().next().getKey();
            sentCache.remove(oldestKey);
            sentAccessOrder.remove(oldestKey);
        }
    }

    private void evictOldestReceived() {
        if (!receivedAccessOrder.isEmpty()) {
            String oldestKey = receivedAccessOrder.entrySet().iterator().next().getKey();
            receivedCache.remove(oldestKey);
            receivedAccessOrder.remove(oldestKey);
        }
    }

    public boolean isAvailableOffline() {
        return !sentCache.isEmpty() || !receivedCache.isEmpty();
    }

    public void printCacheStats() {
        System.out.println("=== CACHE STATS ===");
        System.out.println("Sent emails in cache: " + sentCache.size() + "/" + MAX_CACHE_SIZE);
        System.out.println("Received emails in cache: " + receivedCache.size() + "/" + MAX_CACHE_SIZE);
        System.out.println("Unread count: " + getUnreadCount());
    }
}
