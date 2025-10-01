package nro.models.network;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class AntiDDoSService {

    private static final Map<String, Deque<Long>> ipRequestLog = new ConcurrentHashMap<>();
    private static final Map<String, Integer> ipStrikeCount = new ConcurrentHashMap<>();
    private static final Map<String, Long> ipBlockExpiry = new ConcurrentHashMap<>();
    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();
    private static final Set<String> permanentlyBlockedIps = ConcurrentHashMap.newKeySet();

    private static final Set<String> whitelistedIps = ConcurrentHashMap.newKeySet();

    private static final int MAX_REQUESTS_PER_SECOND = 10;
    private static final int BLOCK_TIME_MS = 60_000;
    private static final int MAX_STRIKES_BEFORE_PERM_BLOCK = 5;

    private static final long CLEANUP_INTERVAL_MS = 5 * 60 * 1000;

    static {
        whitelistedIps.add("127.0.0.1");

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                AntiDDoSService::cleanupOldLogs,
                CLEANUP_INTERVAL_MS,
                CLEANUP_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    public static boolean isBlocked(String ip) {
        if (whitelistedIps.contains(ip)) {
            return false;
        }
        if (permanentlyBlockedIps.contains(ip)) {
            return true;
        }

        long now = System.currentTimeMillis();

        if (blockedIps.contains(ip)) {
            long expiry = ipBlockExpiry.getOrDefault(ip, 0L);
            if (now < expiry) {
                return true;
            }

            blockedIps.remove(ip);
            ipBlockExpiry.remove(ip);
        }

        ipRequestLog.putIfAbsent(ip, new ArrayDeque<>());
        Deque<Long> times = ipRequestLog.get(ip);

        synchronized (times) {
            times.addLast(now);
            while (!times.isEmpty() && now - times.peekFirst() > 1000) {
                times.pollFirst();
            }

            if (times.size() > MAX_REQUESTS_PER_SECOND) {
                int strikes = ipStrikeCount.merge(ip, 1, Integer::sum);

                if (strikes >= MAX_STRIKES_BEFORE_PERM_BLOCK) {
                    permanentlyBlockedIps.add(ip);
                    logToFile("[PERM BLOCK] " + ip);
                    System.out.println("[AntiDDoS] Permanently blocked IP: " + ip);
                } else {
                    blockedIps.add(ip);
                    ipBlockExpiry.put(ip, now + BLOCK_TIME_MS);
                    System.out.println("[AntiDDoS] Temporarily blocked IP: " + ip + " (strike " + strikes + ")");
                }
                return true;
            }
        }

        return false;
    }

    private static void logToFile(String msg) {
        try (FileWriter fw = new FileWriter("blocked_ips.log", true)) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + time + "] " + msg + "\n");
        } catch (IOException ignored) {
        }
    }

    private static void cleanupOldLogs() {
        long now = System.currentTimeMillis();

        ipRequestLog.entrySet().removeIf(e -> {
            Deque<Long> times = e.getValue();
            synchronized (times) {
                return times.isEmpty() || now - times.peekLast() > CLEANUP_INTERVAL_MS;
            }
        });

        ipStrikeCount.entrySet().removeIf(e -> !ipRequestLog.containsKey(e.getKey()));
    }
}
