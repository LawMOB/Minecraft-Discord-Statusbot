package mopsy.productions.discord.statusbot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles automatically detecting the server's public IP address, so the
 * admin doesn't have to enter it manually in the config.
 */
public class IpLookup {
    private static volatile String cachedPublicIp = null;
    private static volatile long lastFetchMillis = 0;
    private static final long CACHE_DURATION_MILLIS = 5 * 60 * 1000L; // 5 minutes
    private static final AtomicBoolean lookupInProgress = new AtomicBoolean(false);

    /**
     * Returns the IP that should be shown to users, taking the
     * 'auto_detect_server_ip' config option into account.
     */
    public static String getServerIp() {
        boolean autoDetect = !ConfigManager.initialized || ConfigManager.getBool("auto_detect_server_ip");

        if (!autoDetect) {
            return manualIpOrPlaceholder();
        }

        if (cachedPublicIp == null || System.currentTimeMillis() - lastFetchMillis > CACHE_DURATION_MILLIS) {
            refreshAsync();
        }

        if (cachedPublicIp != null) {
            return cachedPublicIp;
        }

        // First lookup hasn't returned yet, fall back to whatever is configured manually (if it looks valid)
        return manualIpOrPlaceholder();
    }

    private static String manualIpOrPlaceholder() {
        String manual = ConfigManager.initialized ? ConfigManager.getStr("server_ip") : null;
        if (manual == null || manual.isBlank() || manual.contains("$") || manual.equalsIgnoreCase("play.example.com")) {
            return "detecting IP...";
        }
        return manual;
    }

    /**
     * Kicks off a background lookup of the public IP so the calling thread (e.g. the server's main thread) never blocks on network I/O.
     */
    public static void refreshAsync() {
        if (!lookupInProgress.compareAndSet(false, true)) {
            return; // a lookup is already running
        }
        Thread thread = new Thread(() -> {
            try {
                fetchPublicIp();
            } catch (Exception e) {
                LogUtils.log("Could not auto-detect the server's public IP address: " + e.getMessage(), true);
            } finally {
                lookupInProgress.set(false);
            }
        }, "statusbot-ip-lookup");
        thread.setDaemon(true);
        thread.start();
    }

    private static void fetchPublicIp() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.ipify.org"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            String ip = response.body() == null ? "" : response.body().trim();
            if (!ip.isEmpty()) {
                cachedPublicIp = ip;
                lastFetchMillis = System.currentTimeMillis();
                LogUtils.log("Detected public server IP: " + ip);
                return;
            }
        }
        LogUtils.log("Auto-detecting the public IP failed with response code " + response.statusCode(), true);
    }
}
