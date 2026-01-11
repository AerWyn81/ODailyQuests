package com.ordwen.odailyquests.externs;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.externs.hooks.Protection;
import com.ordwen.odailyquests.externs.hooks.eco.VaultHook;
import com.ordwen.odailyquests.externs.hooks.items.KGeneratorsHook;
import com.ordwen.odailyquests.externs.hooks.placeholders.PAPIExpansion;
import com.ordwen.odailyquests.externs.hooks.points.PlayerPointsHook;
import com.ordwen.odailyquests.externs.hooks.points.TokenManagerHook;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.PluginUtils;

public class IntegrationsManager {

    private final ODailyQuests oDailyQuests;

    public IntegrationsManager(ODailyQuests oDailyQuests) {
        this.oDailyQuests = oDailyQuests;
    }

    /**
     * Load all dependencies.
     */
    public void loadAllDependencies() {
        safeHook("Vault", this::loadVault);
        safeHook("PlayerPoints/TokenManager", this::loadPointsPlugin);
        safeHook("PlaceholderAPI", this::loadPAPI);
        safeHook("KGenerators", this::loadKGenerators);
        safeHook("Protection", () -> new Protection().load());
    }

    private void safeHook(String name, Runnable hook) {
        try {
            hook.run();
        } catch (Exception err) {
            PluginLogger.warn("Failed to hook into " + name + ". Is the plugin installed and up to date?");
        }
    }

    /**
     * Hook - TokenManager / PlayerPoints
     */
    private void loadPointsPlugin() {
        TokenManagerHook.setupTokenManager();
        PlayerPointsHook.setupPlayerPointsAPI();
    }

    /**
     * Hook - Vault
     */
    private void loadVault() {
        VaultHook.setupEconomy();
    }

    /**
     * Hook - PlaceholderAPI
     */
    private void loadPAPI() {
        if (PluginUtils.isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion(oDailyQuests.getInterfacesManager().getPlayerQuestsInterface()).register();
        }
    }

    /**
     * Hook - KGenerators
     */
    private void loadKGenerators() {
        KGeneratorsHook.setupKGeneratorsAPI();
    }
}
