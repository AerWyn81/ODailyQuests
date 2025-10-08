package com.ordwen.odailyquests.reload;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ODQReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ODailyQuests core;
    private final ReloadPhase phase;

    public ODQReloadEvent(ODailyQuests core, ReloadPhase phase) {
        this.core = core;
        this.phase = phase;
    }

    public ODailyQuests getCore() {
        return core;
    }

    public ReloadPhase getPhase() {
        return phase;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static void call(ODailyQuests core, ReloadPhase phase) {
        final ODQReloadEvent event = new ODQReloadEvent(core, phase);
        try {
            core.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            PluginLogger.error("An error occurred while calling ODQReloadEvent: " + e.getMessage());
        }
    }
}
