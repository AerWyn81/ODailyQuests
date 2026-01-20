package com.ordwen.odailyquests.reload;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.api.events.ODQReloadEvent;
import com.ordwen.odailyquests.configuration.ConfigFactory;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.essentials.ReloadMessage;
import com.ordwen.odailyquests.configuration.integrations.ItemsAdderEnabled;
import com.ordwen.odailyquests.configuration.integrations.NexoEnabled;
import com.ordwen.odailyquests.configuration.integrations.OraxenEnabled;
import com.ordwen.odailyquests.quests.categories.CategoriesLoader;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReloadService {

    private final ODailyQuests plugin;
    private final CategoriesLoader categoriesLoader;

    /**
     * Constructor.
     *
     * @param plugin main class instance.
     */
    public ReloadService(ODailyQuests plugin) {
        this.plugin = plugin;
        this.categoriesLoader = plugin.getCategoriesLoader();
    }

    /**
     * Load all quests from connected players, to avoid errors on reload.
     */
    public void loadConnectedPlayerQuests() {
        loadConnectedPlayerQuests(true);
    }

    public void loadConnectedPlayerQuests(boolean sendStatusMessage) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!QuestsManager.getActiveQuests().containsKey(player.getName())) {
                plugin.getDatabaseManager().loadQuestsForPlayer(player.getName(), sendStatusMessage);
            }
        }
    }

    /**
     * Save all quests from connected players, to avoid errors on reload.
     */
    public void saveConnectedPlayerQuests() {
        final Map<String, PlayerQuests> activeQuests = new HashMap<>(QuestsManager.getActiveQuests());
        for (Map.Entry<String, PlayerQuests> entry : activeQuests.entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                Debugger.write("Impossible to save progression for player " + entry.getKey() + " because the player is offline.");
                PluginLogger.warn("Impossible to save progression for player " + entry.getKey() + " because the player is offline.");
                continue;
            }

            plugin.getDatabaseManager().saveProgressionForPlayer(player.getName(), player.getUniqueId().toString(), entry.getValue());
            QuestsManager.getActiveQuests().remove(entry.getKey());
        }
    }

    /**
     * Execute all required actions when the command /qadmin reload is performed.
     */
    public void reload() {
        try {
            /* load files */
            plugin.getFilesManager().load();
            ODQReloadEvent.call(plugin, ReloadPhase.FILES_LOADED);

            /* load configurations */
            ConfigFactory.registerConfigs(plugin.getFilesManager());
            ODQReloadEvent.call(plugin, ReloadPhase.CONFIGS_LOADED);

            /* load database */
            plugin.getDatabaseManager().load();
            ODQReloadEvent.call(plugin, ReloadPhase.DATABASE_LOADED);

            /* load quests & interface */
            if ((!ItemsAdderEnabled.isEnabled() || ItemsAdderEnabled.isLoaded())
                    && (!OraxenEnabled.isEnabled() || OraxenEnabled.isLoaded())
                    && (!NexoEnabled.isEnabled() || NexoEnabled.isLoaded())) {

                categoriesLoader.loadCategories();
                plugin.getInterfacesManager().initAllObjects();
                ODQReloadEvent.call(plugin, ReloadPhase.CONTENT_LOADED);
            }

            saveConnectedPlayerQuests();
            ODQReloadEvent.call(plugin, ReloadPhase.PLAYERS_SAVED);

            final boolean sendStatusOnReload = ReloadMessage.shouldSendOnReload();
            ODailyQuests.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(() -> {
                loadConnectedPlayerQuests(sendStatusOnReload);
                ODQReloadEvent.call(plugin, ReloadPhase.PLAYERS_LOADED);
            }, 20L);

            ODQReloadEvent.call(plugin, ReloadPhase.RELOAD_COMPLETE);
        } catch (Exception e) {
            PluginLogger.error("An error occurred while reloading the plugin. Please check the logs for details.");
            PluginLogger.error(e.getMessage());
        }
    }
}
