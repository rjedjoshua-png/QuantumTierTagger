package net.uku3lig.tiertagger.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.uku3lig.tiertagger.TierTagger;
import net.uku3lig.tiertagger.config.UkulibIntegration;
import net.uku3lig.ukulib.neoforge.UkulibNFProvider;

@Mod(value = TierTagger.MOD_ID, dist = Dist.CLIENT)
public class TierTaggerNeoForge {
    public TierTaggerNeoForge(ModContainer container) {
        TierTagger.onInitialize();
        container.registerExtensionPoint(UkulibNFProvider.class, UkulibIntegration::new);
    }
}
