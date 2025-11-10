package com.ordwen.odailyquests.events.listeners.global;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener extends PlayerProgressor implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.PUMPKIN) return;

        final Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.SHEARS) return;

        final Location loc = block.getLocation();

        ODailyQuests.morePaperLib.scheduling().regionSpecificScheduler(loc).run(() -> {
            if (loc.getBlock().getType() == Material.CARVED_PUMPKIN) {
                setPlayerQuestProgression(event, player, 1, "CARVE");
            }
        });
    }
}
