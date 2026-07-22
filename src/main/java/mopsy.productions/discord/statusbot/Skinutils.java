package mopsy.productions.discord.statusbot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkinUtils {
    private SkinUtils() {}

    private static final Map<String, CacheEntry> skinUrlCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MILLIS = 10 * 60 * 1000L; // 10 minutes

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
    private static final Pattern TEXTURES_PROPERTY_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"textures\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SKIN_URL_PATTERN = Pattern.compile("\"SKIN\"\\s*:\\s*\\{\\s*\"url\"\\s*:\\s*\"([^\"]+)\"");

    public static final class HeadIcon {
        public final byte[] bytes;
        public final String fileName;

        public HeadIcon(byte[] bytes, String fileName) {
            this.bytes = bytes;
            this.fileName = fileName;
        }
    }

    private static final class CacheEntry {
        final String skinUrl;
        final long timestamp;

        CacheEntry(String skinUrl, long timestamp) {
            this.skinUrl = skinUrl;
            this.timestamp = timestamp;
        }
    }

    public static HeadIcon fetchHeadIcon(String playerName) {
        try {
            String skinUrl = resolveSkinUrl(playerName);
            if (skinUrl == null)
                return null;

            byte[] skinPng = downloadBytes(skinUrl);
            if (skinPng == null)
                return null;

            BufferedImage composed = composeHead(skinPng);
            if (composed == null)
                return null;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(composed, "png", out);
            return new HeadIcon(out.toByteArray(), "head-" + sanitizeFileName(playerName) + ".png");
        } catch (Exception e) {
            LogUtils.log("Could not fetch a real skin for player '" + playerName + "', falling back to a generic avatar: " + e.getMessage(), true);
            return null;
        }
    }

    private static String sanitizeFileName(String playerName) {
        return playerName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static String resolveSkinUrl(String playerName) throws Exception {
        String key = playerName.toLowerCase(Locale.ROOT);
        CacheEntry cached = skinUrlCache.get(key);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MILLIS) {
            return cached.skinUrl;
        }

        String skinUrl = lookupSkinUrlFromMojang(playerName);
        skinUrlCache.put(key, new CacheEntry(skinUrl, System.currentTimeMillis()));
        return skinUrl;
    }

    private static String lookupSkinUrlFromMojang(String playerName) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest uuidRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + URLEncoder.encode(playerName, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> uuidResponse = client.send(uuidRequest, HttpResponse.BodyHandlers.ofString());
        if (uuidResponse.statusCode() != 200 || uuidResponse.body() == null || uuidResponse.body().isBlank())
            return null;

        Matcher idMatcher = ID_PATTERN.matcher(uuidResponse.body());
        if (!idMatcher.find())
            return null;
        String uuid = idMatcher.group(1);

        HttpRequest profileRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> profileResponse = client.send(profileRequest, HttpResponse.BodyHandlers.ofString());
        if (profileResponse.statusCode() != 200)
            return null;

        Matcher texturesMatcher = TEXTURES_PROPERTY_PATTERN.matcher(profileResponse.body());
        if (!texturesMatcher.find())
            return null;

        String decoded = new String(Base64.getDecoder().decode(texturesMatcher.group(1)), StandardCharsets.UTF_8);
        Matcher skinUrlMatcher = SKIN_URL_PATTERN.matcher(decoded);
        if (!skinUrlMatcher.find())
            return null;

        return skinUrlMatcher.group(1).replace("\\/", "/");
    }

    private static byte[] downloadBytes(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200)
            return null;
        return response.body();
    }

    private static BufferedImage composeHead(byte[] skinPngBytes) throws Exception {
        BufferedImage skin = ImageIO.read(new ByteArrayInputStream(skinPngBytes));
        if (skin == null || skin.getWidth() < 64 || skin.getHeight() < 32)
            return null;

        int scale = 12;
        int size = 8 * scale;
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            g.drawImage(skin, 0, 0, size, size, 8, 8, 16, 16, null);
            g.drawImage(skin, 0, 0, size, size, 40, 8, 48, 16, null);
        } finally {
            g.dispose();
        }
        return result;
    }
}
