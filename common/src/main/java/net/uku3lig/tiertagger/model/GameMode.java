package net.uku3lig.tiertagger.model;

import com.google.gson.JsonObject;
import net.uku3lig.tiertagger.TierTagger;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record GameMode(String id, String title) {
    public static final GameMode NONE = new GameMode("annoying_long_id_that_no_one_will_ever_use_just_to_make_sure", "§cNone§r");

    public static CompletableFuture<List<GameMode>> fetchGamemodes(HttpClient client) {
        // The base URL is like "https://quantum-tierlist.vercel.app/api/v2?slug=pvt"
        String base = TierTagger.getManager().getConfig().getApiUrl();
        String cleanBase;
        String slugParam = "";
        int q = base.indexOf('?');
        if (q >= 0) {
            slugParam = base.substring(q);
            cleanBase = base.substring(0, q);
        } else {
            cleanBase = base;
        }
        String endpoint = cleanBase + "/mode/list" + slugParam;
        final HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint)).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    JsonObject obj = TierTagger.GSON.fromJson(r.body(), JsonObject.class);

                    return obj.entrySet().stream().map(e -> {
                        String title = e.getValue().getAsJsonObject().get("title").getAsString();
                        return new GameMode(e.getKey(), title);
                    }).toList();
                });
    }

    public boolean isNone() {
        return this.id.equals(NONE.id);
    }

    private Pair<Character, TextColor> iconAndColor() {
        return switch (this.id) {
            case "axe" -> Pair.of('\uE701', TextColor.fromLegacyFormat(ChatFormatting.GREEN));
            case "mace" -> Pair.of('\uE702', TextColor.fromLegacyFormat(ChatFormatting.GRAY));
            case "nethop", "neth_pot" -> Pair.of('\uE703', TextColor.fromRgb(0x7d4a40));
            case "pot" -> Pair.of('\uE704', TextColor.fromRgb(0xff0000));
            case "smp" -> Pair.of('\uE705', TextColor.fromRgb(0xeccb45));
            case "sword" -> Pair.of('\uE706', TextColor.fromRgb(0xa4fdf0));
            case "uhc" -> Pair.of('\uE707', TextColor.fromLegacyFormat(ChatFormatting.RED));
            case "crystal" -> Pair.of('\uE708', TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE));
            case "dia_smp" -> Pair.of('\uE806', TextColor.fromRgb(0x8c668b));
            case "cart" -> Pair.of('\uE809', TextColor.fromLegacyFormat(ChatFormatting.GRAY));
            default -> Pair.of('•', TextColor.fromLegacyFormat(ChatFormatting.WHITE));
        };
    }

    public Optional<Character> icon() {
        Pair<Character, TextColor> pair = this.iconAndColor();
        return pair.right().getValue() == 0xFFFFFF ? Optional.empty() : Optional.of(pair.left());
    }

    public Component asStyled(boolean withDefaultDot) {
        Pair<Character, TextColor> pair = this.iconAndColor();

        if (pair.right().getValue() == 0xFFFFFF && !withDefaultDot) {
            return Component.literal(this.title);
        } else {
            Component name = Component.literal(this.title).withStyle(s -> s.withColor(pair.right()));
            return Component.literal(pair.left() + " ").append(name);
        }
    }
}
