package net.uku3lig.tiertagger.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.uku3lig.tiertagger.TierTagger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Getter
@AllArgsConstructor
public class TierList {
    private final String slug;
    private final String name;
    private final String iconUrl;

    private static final String DEFAULT_BASE = "https://quantum-tierlist.vercel.app";
    private static final List<TierList> CACHE = new ArrayList<>();

    public String apiBase() {
        return DEFAULT_BASE + "/api/v2";
    }

    public String apiUrl() {
        return apiBase() + "?slug=" + slug;
    }

    public static CompletableFuture<List<TierList>> fetchAll(HttpClient client) {
        String endpoint = DEFAULT_BASE + "/api/tenants";
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    JsonObject root = TierTagger.GSON.fromJson(r.body(), JsonObject.class);
                    JsonArray arr = root.getAsJsonArray("tenants");
                    List<TierList> out = new ArrayList<>();
                    if (arr != null) {
                        for (var el : arr) {
                            JsonObject t = el.getAsJsonObject();
                            String slug = t.get("slug").getAsString();
                            String name = t.has("name") ? t.get("name").getAsString() : slug;
                            String iconUrl = (t.has("icon_url") && !t.get("icon_url").isJsonNull())
                                    ? t.get("icon_url").getAsString() : null;
                            out.add(new TierList(slug, name, iconUrl));
                        }
                    }
                    synchronized (CACHE) {
                        CACHE.clear();
                        CACHE.addAll(out);
                    }

                    // Auto-select first tenant if none chosen yet
                    if (!out.isEmpty()) {
                        String current = TierTagger.getManager().getConfig().getApiUrl();
                        boolean needsDefault = current == null
                                || !current.contains("slug=")
                                || current.endsWith("slug=")
                                || findByUrl(current).isEmpty();
                        if (needsDefault) {
                            TierTagger.getManager().getConfig().setApiUrl(out.get(0).apiUrl());
                            TierTagger.getManager().saveConfig();
                            TierTagger.getLogger().info("Auto-selected tenant: {}", out.get(0).getSlug());
                        }
                    }

                    return out;
                })
                .whenComplete((_, t) -> {
                    if (t != null) TierTagger.getLogger().warn("Failed to fetch tenants", t);
                });
    }

    public static List<TierList> cached() {
        synchronized (CACHE) {
            return new ArrayList<>(CACHE);
        }
    }

    public static Optional<TierList> findBySlug(String slug) {
        if (slug == null) return Optional.empty();
        synchronized (CACHE) {
            return CACHE.stream().filter(t -> t.slug.equalsIgnoreCase(slug)).findFirst();
        }
    }

    public static Optional<TierList> findByUrl(String url) {
        if (url == null) return Optional.empty();
        String slug = null;
        int idx = url.indexOf("slug=");
        if (idx >= 0) {
            slug = url.substring(idx + 5);
            int amp = slug.indexOf('&');
            if (amp >= 0) slug = slug.substring(0, amp);
        }
        if (slug == null || slug.isBlank()) return Optional.empty();
        return findBySlug(slug);
    }

    public String styledName(boolean current) {
        String s = name;
        if (current) s += " (selected)";
        return s;
    }
}
