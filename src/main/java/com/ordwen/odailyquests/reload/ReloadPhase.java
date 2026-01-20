package com.ordwen.odailyquests.reload;

/**
 * Represents the different phases of an ODailyQuests reload cycle.
 * <p>
 * This enum is used by {@link ODQReloadEvent} to indicate which step of the reload
 * has just been reached, allowing external plugins/integrations to react at the right time
 * (e.g. reloading their own caches once configs are loaded, reconnecting after the database
 * is initialized, refreshing GUI content once quests/interfaces are ready, etc.).
 *
 * @since 3.0.0
 */
public enum ReloadPhase {

    /**
     * Plugin files have been loaded (or reloaded) by the FilesManager.
     * <p>
     * Typically includes reading YAML/JSON files and preparing resources required by the plugin.
     */
    FILES_LOADED,

    /**
     * Configurations have been registered and loaded.
     * <p>
     * At this stage, configuration values can safely be read by components.
     */
    CONFIGS_LOADED,

    /**
     * Database layer has been initialized or refreshed.
     * <p>
     * At this stage, database queries should be available again.
     */
    DATABASE_LOADED,

    /**
     * Quests content (categories) and interfaces have been (re)initialized.
     * <p>
     * This phase is triggered only if required content dependencies are enabled/loaded
     * (ItemsAdder/Oraxen/Nexo checks).
     */
    CONTENT_LOADED,

    /**
     * Currently connected players' quests have been saved prior to reloading player data.
     * <p>
     * Useful for integrations that want to persist additional per-player state during reload.
     */
    PLAYERS_SAVED,

    /**
     * Connected players' quests have been loaded back after the reload.
     * <p>
     * In the default reload flow, this happens after a short delay.
     */
    PLAYERS_LOADED,

    /**
     * Reload sequence is complete.
     * <p>
     * This is the final phase of the reload lifecycle.
     */
    RELOAD_COMPLETE
}
