package com.ordwen.odailyquests.quests.types.item;

import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.shared.BasicQuest;
import com.ordwen.odailyquests.quests.types.shared.ItemQuest;
import com.ordwen.odailyquests.tools.PluginUtils;
import net.Indyuce.mmocore.api.event.CustomPlayerFishEvent;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Quest implementation triggered when a player catches an item while fishing.
 * <p>
 * This quest supports multiple fishing sources depending on enabled integrations:
 * - vanilla Bukkit fishing events
 * - CustomFishing loot spawn events
 * - MMOCore custom fishing events
 * <p>
 * The quest progresses when the caught item matches the required quest item.
 */
public class FishQuest extends ItemQuest {

    /**
     * Creates a new fishing quest.
     *
     * @param base the base quest configuration used to initialize this quest
     */
    public FishQuest(BasicQuest base) {
        super(base);
    }

    /**
     * Returns the unique quest type identifier.
     *
     * @return the quest type string, always "FISH"
     */
    @Override
    public String getType() {
        return "FISH";
    }

    /**
     * Determines whether this quest can progress for the given event.
     * <p>
     * Progression is allowed when one of the following conditions is met:
     * - the event is a vanilla PlayerFishEvent with a caught item
     * - the CustomFishing plugin is enabled and the event is a FishingLootSpawnEvent
     * - the MMOCore plugin is enabled and the event is a CustomPlayerFishEvent
     * <p>
     * In all cases, the caught item must match the required quest item.
     *
     * @param provided    the event triggering the progression check
     * @param progression the player's current quest progression
     * @return true if the quest should progress, false otherwise
     */
    @Override
    public boolean canProgress(Event provided, Progression progression) {
        if (provided instanceof PlayerFishEvent event) {
            final Item item = (Item) event.getCaught();
            if (item == null) return false;
            return super.isRequiredItem(item.getItemStack(), progression);
        }

        if (PluginUtils.isPluginEnabled("CustomFishing")
                && provided instanceof FishingLootSpawnEvent event
                && event.getEntity() instanceof Item item) {
            return super.isRequiredItem(item.getItemStack(), progression);
        }

        if (PluginUtils.isPluginEnabled("MMOCore")
                && provided instanceof CustomPlayerFishEvent event
                && event.getCaught() != null) {
            return super.isRequiredItem(event.getCaught(), progression);
        }

        return false;
    }
}
