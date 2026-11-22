package com.org.gigscore.security;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiration) {
        if (token == null || token.isBlank()) {
            return;
        }
        long expiryTime = (expiration != null) ? expiration.getTime() : System.currentTimeMillis() + 3_600_000L;
        blacklist.put(token, expiryTime);
        purgeExpired();
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Long expiry = blacklist.get(token);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        if (blacklist.size() > 500) {
            long now = System.currentTimeMillis();
            blacklist.entrySet().removeIf(entry -> now > entry.getValue());
        }
    }
}
