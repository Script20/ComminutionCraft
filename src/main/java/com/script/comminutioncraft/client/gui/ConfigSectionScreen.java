package com.script.comminutioncraft.client.gui;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.jspecify.annotations.Nullable;

/**
 * Our config screens (Mods > Comminution Craft > Config): NeoForge's, except section titles show just their name
 * (owner, 2026-11-16) - NeoForge writes them as "Name..." through a translation every mod shares, so only our
 * screens change.
 */
public class ConfigSectionScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    public ConfigSectionScreen(Screen parent, ModConfig.Type type, ModConfig config, Component title) {
        super(parent, type, config, title);
    }

    @Override
    protected @Nullable Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
        Element element = super.createSection(key, subconfig, subsection);
        return element == null ? null
                : new Element(getTranslationComponent(key), element.tooltip(), element.widget(), element.option(), element.undoable());
    }
}
