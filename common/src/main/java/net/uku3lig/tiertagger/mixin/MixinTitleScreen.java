package net.uku3lig.tiertagger.mixin;

import com.llamalad7.mixinextras.lib.semver.Version;
import com.mojang.blaze3d.Blaze3D;
import net.uku3lig.tiertagger.TierTagger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    @Unique
    private static final AtomicBoolean hasCheckedVersion = new AtomicBoolean(false);

    protected MixinTitleScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void showUpdateScreen(CallbackInfo ci) {
        if (hasCheckedVersion.get()) return;

        Version currentVersion = TierTagger.getCurrentVersion();
        Version latestVersion = TierTagger.getLatestVersion();

        if (TierTagger.isObsolete()) {
            Minecraft.getInstance().gui.setScreen(new ConfirmScreen(
                    b -> {
                        if (b) {
                            Minecraft.getInstance().stop();
                        } else {
                            Minecraft.getInstance().gui.setScreen(this);
                        }
                    },
                    Component.translatable("tiertagger.obsolete.title"),
                    Component.translatable("tiertagger.obsolete.desc"),
                    Component.translatable("menu.quit"),
                    Component.translatable("tiertagger.outdated.ignore")
            ));
        } else if (currentVersion != null && latestVersion != null && latestVersion.isHigherThan(currentVersion)) {
            Component newVersion = Component.literal(latestVersion.toString()).withStyle(ChatFormatting.GREEN);

            Minecraft.getInstance().gui.setScreen(new ConfirmScreen(
                    b -> {
                        if (b) {
                            URI uri = URI.create("https://modrinth.com/mod/tiertagger/version/" + latestVersion);
                            Blaze3D.openUri(uri);
                        }

                        Minecraft.getInstance().gui.setScreen(this);
                    },
                    Component.translatable("tiertagger.outdated.title"),
                    Component.translatable("tiertagger.outdated.desc", newVersion),
                    Component.translatable("tiertagger.outdated.download"),
                    Component.translatable("tiertagger.outdated.ignore")
            ));
        }

        hasCheckedVersion.set(true);
    }
}
