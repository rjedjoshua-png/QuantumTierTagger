package net.uku3lig.tiertagger.config;

import net.uku3lig.tiertagger.TierCache;
import net.uku3lig.tiertagger.TierTagger;
import net.uku3lig.tiertagger.model.TierList;
import net.uku3lig.tiertagger.tierlist.PlayerSearchScreen;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.uku3lig.ukulib.config.option.*;
import net.uku3lig.ukulib.config.option.widget.ButtonTab;
import net.uku3lig.ukulib.config.screen.TabbedConfigScreen;
import net.uku3lig.ukulib.utils.Ukutils;

import java.util.*;
import java.util.stream.Collectors;

public class TTConfigScreen extends TabbedConfigScreen<TierTaggerConfig> {
    public TTConfigScreen(Screen parent) {
        super("CommunityTierTagger Config", parent, TierTagger.getManager());
    }

    @Override
    protected Tab[] getTabs(TierTaggerConfig config) {
        return new Tab[]{new MainSettingsTab(), new ColorsTab(), new TierlistTab()};
    }

    public class MainSettingsTab extends ButtonTab<TierTaggerConfig> {
        public MainSettingsTab() {
            super("tiertagger.config", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            return new WidgetCreator[]{
                    CyclingOption.ofBoolean("tiertagger.config.enabled", config.isEnabled(), config::setEnabled),
                    new CyclingOption<>("tiertagger.config.gamemode", TierCache.getGamemodes(), config.getGameMode(), m -> config.setGameMode(m.id()), m -> Component.literal(m.title()),
                            m -> m.isNone() ? Tooltip.create(Component.translatable("tiertagger.config.gamemode.none")) : null, !config.getGameMode().isNone()),
                    CyclingOption.ofBoolean("tiertagger.config.retired", config.isShowRetired(), config::setShowRetired),
                    CyclingOption.ofTranslatableEnum("tiertagger.config.highest", TierTaggerConfig.HighestMode.class, config.getHighestMode(), config::setHighestMode, OptionInstance.cachedConstantTooltip(Component.translatable("tiertagger.config.highest.desc"))),
                    CyclingOption.ofBoolean("tiertagger.config.icons", config.isShowIcons(), config::setShowIcons),
                    CyclingOption.ofBoolean("tiertagger.config.playerList", config.isPlayerList(), config::setPlayerList),
                    new SimpleButton("tiertagger.clear", _ -> TierCache.clearCache()),
                    new ScreenOpenButton("tiertagger.config.search", PlayerSearchScreen::new)
            };
        }
    }

    public class TierlistTab extends ButtonTab<TierTaggerConfig> {
        public TierlistTab() {
            super("tiertagger.config.tierlists", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            Optional<TierList> current = TierList.findByUrl(config.getApiUrl());

            List<WidgetCreator> widgets = new ArrayList<>();

            List<TierList> cached = TierList.cached();
            if (cached.isEmpty()) {
                widgets.add(new SimpleButton(Component.literal("Loading communities... (restart the game if stuck)"), _ -> {}, false));
            } else {
                for (TierList t : cached) {
                    boolean isCurrent = current.isPresent() && current.get().getSlug().equalsIgnoreCase(t.getSlug());
                    widgets.add(new SimpleButton(Component.literal(t.styledName(isCurrent)), _ -> {
                        config.setApiUrl(t.apiUrl());
                        TierTagger.getManager().saveConfig();
                        TTConfigScreen.this.onClose();
                        TierCache.init();
                        Ukutils.sendToast(Component.literal("Switched to " + t.getName() + "!"), Component.literal("Reloading tiers..."));
                    }, !isCurrent));
                }
            }

            if (current.isEmpty()) {
                widgets.add(new SimpleButton(Component.literal("Custom (selected, " + config.getApiUrl() + ")"), _ -> {}, false));
            }

            return widgets.toArray(WidgetCreator[]::new);
        }
    }

    public class ColorsTab extends ButtonTab<TierTaggerConfig> {
        protected ColorsTab() {
            super("tiertagger.colors", TTConfigScreen.this.manager);
        }

        @Override
        protected WidgetCreator[] getWidgets(TierTaggerConfig config) {
            Comparator<Map.Entry<String, Integer>> comparator = Comparator.comparing(e -> e.getKey().charAt(2));
            comparator = comparator.thenComparing(e -> e.getKey().charAt(0));

            List<ColorOption> tiers = config.getTierColors().entrySet().stream()
                    .sorted(comparator)
                    .map(e -> new ColorOption(e.getKey(), e.getValue(), val -> config.getTierColors().put(e.getKey(), val)))
                    .collect(Collectors.toList());

            tiers.addLast(new ColorOption("tiertagger.colors.retired", config.getRetiredColor(), config::setRetiredColor));

            return tiers.toArray(WidgetCreator[]::new);
        }
    }
}
