package com.ordwen.odailyquests.events;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.CustomFurnaceResults;
import com.ordwen.odailyquests.configuration.integrations.ItemsAdderEnabled;
import com.ordwen.odailyquests.configuration.integrations.NexoEnabled;
import com.ordwen.odailyquests.configuration.integrations.OraxenEnabled;
import com.ordwen.odailyquests.events.listeners.crate.CrateOpenListener;
import com.ordwen.odailyquests.events.listeners.customs.CustomFurnaceExtractListener;
import com.ordwen.odailyquests.events.listeners.entity.*;
import com.ordwen.odailyquests.events.listeners.entity.custom.mobs.EliteMobDeathListener;
import com.ordwen.odailyquests.events.listeners.entity.custom.mobs.MythicMobDeathListener;
import com.ordwen.odailyquests.events.listeners.entity.custom.stackers.RoseStackerListener;
import com.ordwen.odailyquests.events.listeners.entity.custom.stackers.WildStackerListener;
import com.ordwen.odailyquests.events.listeners.global.*;
import com.ordwen.odailyquests.events.listeners.integrations.customsuite.CropBreakListener;
import com.ordwen.odailyquests.events.listeners.integrations.customsuite.FishingLootSpawnListener;
import com.ordwen.odailyquests.events.listeners.integrations.itemsadder.CustomBlockBreakListener;
import com.ordwen.odailyquests.events.listeners.integrations.itemsadder.ItemsAdderLoadDataListener;
import com.ordwen.odailyquests.events.listeners.integrations.nexo.NexoItemsLoadedListener;
import com.ordwen.odailyquests.events.listeners.integrations.npcs.CitizensHook;
import com.ordwen.odailyquests.events.listeners.integrations.npcs.FancyNpcsHook;
import com.ordwen.odailyquests.events.listeners.integrations.oraxen.OraxenItemsLoadedListener;
import com.ordwen.odailyquests.events.listeners.inventory.InventoryClickListener;
import com.ordwen.odailyquests.events.listeners.inventory.InventoryCloseListener;
import com.ordwen.odailyquests.events.listeners.item.*;
import com.ordwen.odailyquests.events.listeners.item.custom.CraftMMOItemListener;
import com.ordwen.odailyquests.events.listeners.item.custom.CustomPlayerFishListener;
import com.ordwen.odailyquests.events.listeners.item.custom.DropQueuePushListener;
import com.ordwen.odailyquests.events.listeners.vote.VotifierListener;
import com.ordwen.odailyquests.tools.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

/**
 * Centralizes the registration of all Bukkit event listeners used by the plugin.
 */
public class EventsManager {

    private final ODailyQuests oDailyQuests;

    /**
     * Creates a new events manager bound to the main plugin instance.
     *
     * @param plugin the main plugin instance used to register listeners
     */
    public EventsManager(ODailyQuests plugin) {
        this.oDailyQuests = plugin;
    }

    /**
     * Registers all event listeners used by the plugin.
     * <p>
     * This method acts as the single entry point for listener registration
     * and delegates the actual work to specialized registration methods
     * grouped by functional area.
     */
    public void registerListeners() {
        final PluginManager pm = Bukkit.getPluginManager();

        registerEntityListeners(pm);
        registerGlobalListeners(pm);
        registerItemAndInventoryListeners(pm);
        registerCustomListeners(pm);
        registerIntegrationListeners(pm);
    }

    /**
     * Registers listeners related to entity lifecycle and entity-based actions.
     * <p>
     * This includes both vanilla entity events and optional integrations
     * with external mob or stacking plugins when available.
     *
     * @param pm the Bukkit plugin manager
     */
    private void registerEntityListeners(PluginManager pm) {
        registerAll(pm,
                new EntityBreedListener(),
                new EntityTameListener(),
                new ShearEntityListener(),
                new EntityDeathListener(),
                new SpawnerSpawnListener()
        );

        registerIf(pm, "EliteMobs", new EliteMobDeathListener());
        registerIf(pm, "MythicMobs", new MythicMobDeathListener());
        registerIf(pm, "WildStacker", new WildStackerListener());
        registerIf(pm, "RoseStacker", new RoseStackerListener());
    }

    /**
     * Registers global listeners related to player state and world interactions.
     * <p>
     * These listeners are always registered and do not depend on external plugins.
     *
     * @param pm the Bukkit plugin manager
     */
    private void registerGlobalListeners(PluginManager pm) {
        registerAll(pm,
                new BucketFillListener(),
                new PlayerExpChangeListener(),
                new PlayerLevelChangeListener(),
                new PlayerInteractListener(),
                new PlayerInteractEntityListener(),
                new PlayerDeathListener(),
                new PlayerRespawnListener()
        );
    }

    /**
     * Registers listeners related to items, blocks, inventories, and crafting.
     * <p>
     * This includes both item interactions and inventory-related listeners.
     *
     * @param pm the Bukkit plugin manager
     */
    private void registerItemAndInventoryListeners(PluginManager pm) {
        registerAll(pm,
                new BlockBreakListener(),
                new BlockPlaceListener(),
                new CraftItemListener(),
                new SmithItemListener(),
                new EnchantItemListener(),
                new FurnaceExtractListener(),
                new PickupItemListener(),
                new PlayerFishListener(),
                new PlayerItemConsumeListener(),
                new ProjectileLaunchListener(),
                new InventoryClickListener(oDailyQuests.getInterfacesManager().getPlayerQuestsInterface()),
                new BlockDropItemListener(),
                new PlayerHarvestBlockListener(),
                new PlayerDropItemListener(),
                new StructureGrowListener(),
                new InventoryCloseListener()
        );
    }

    /**
     * Registers listeners for custom internal features.
     * <p>
     * These listeners are only registered when at least one related
     * custom integration or configuration flag is enabled.
     *
     * @param pm the Bukkit plugin manager
     */
    private void registerCustomListeners(PluginManager pm) {
        if (ItemsAdderEnabled.isEnabled()
                || OraxenEnabled.isEnabled()
                || NexoEnabled.isEnabled()
                || CustomFurnaceResults.isEnabled()) {
            pm.registerEvents(new CustomFurnaceExtractListener(), oDailyQuests);
        }
    }

    /**
     * Registers listeners related to third-party plugin integrations.
     * <p>
     * Each listener is conditionally registered depending on the presence
     * of the corresponding external plugin.
     *
     * @param pm the Bukkit plugin manager
     */
    private void registerIntegrationListeners(PluginManager pm) {
        if (ItemsAdderEnabled.isEnabled()) {
            registerAll(pm,
                    new ItemsAdderLoadDataListener(oDailyQuests),
                    new CustomBlockBreakListener()
            );
        }

        if (OraxenEnabled.isEnabled()) {
            pm.registerEvents(new OraxenItemsLoadedListener(oDailyQuests), oDailyQuests);
        }

        if (NexoEnabled.isEnabled()) {
            pm.registerEvents(new NexoItemsLoadedListener(oDailyQuests), oDailyQuests);
        }

        registerIf(pm, "CustomCrops", new CropBreakListener());
        registerIf(pm, "CustomFishing", new FishingLootSpawnListener());
        registerIf(pm, "Votifier", new VotifierListener());
        registerIf(pm, "ExcellentCrates", new CrateOpenListener());
        registerIf(pm, "Citizens", new CitizensHook(oDailyQuests.getInterfacesManager()));
        registerIf(pm, "FancyNpcs", new FancyNpcsHook(oDailyQuests.getInterfacesManager()));
        registerIf(pm, "eco", new DropQueuePushListener());
        registerIf(pm, "MMOCore", new CustomPlayerFishListener());
        registerIf(pm, "MMOItems", new CraftMMOItemListener());
    }

    /**
     * Registers a listener only if the given plugin is enabled on the server.
     *
     * @param pm         the Bukkit plugin manager
     * @param pluginName the name of the plugin to check
     * @param listener   the listener to register if the plugin is enabled
     */
    private void registerIf(PluginManager pm, String pluginName, Listener listener) {
        if (PluginUtils.isPluginEnabled(pluginName)) {
            pm.registerEvents(listener, oDailyQuests);
        }
    }

    /**
     * Registers multiple listeners in a single call.
     * <p>
     * This utility method exists to reduce boilerplate and keep
     * registration code concise and readable.
     *
     * @param pm        the Bukkit plugin manager
     * @param listeners the listeners to register
     */
    private void registerAll(PluginManager pm, Listener... listeners) {
        for (Listener listener : listeners) {
            pm.registerEvents(listener, oDailyQuests);
        }
    }
}
