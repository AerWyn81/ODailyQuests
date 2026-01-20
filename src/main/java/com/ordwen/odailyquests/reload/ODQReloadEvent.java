package com.ordwen.odailyquests.reload;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when ODailyQuests is reloaded via the plugin reload command.
 * <p>
 * This event is fired multiple times during a single reload and provides the current
 * {@link ReloadPhase} to indicate which part of the reload pipeline has been reached.
 * It allows integrations and add-ons to synchronize their own reload actions with the core plugin.
 * <p>
 * This event is not cancellable: it is purely informative.
 *
 * @since 3.0.0
 */
public class ODQReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ODailyQuests core;
    private final ReloadPhase phase;

    /**
     * Constructor for the ODQReloadEvent.
     *
     * @param core  ODailyQuests instance being reloaded
     * @param phase current phase of the reload lifecycle
     */
    public ODQReloadEvent(ODailyQuests core, ReloadPhase phase) {
        this.core = core;
        this.phase = phase;
    }

    /**
     * Get the ODailyQuests instance being reloaded.
     *
     * @return ODailyQuests instance
     */
    public ODailyQuests getCore() {
        return core;
    }

    /**
     * Get the current reload phase.
     *
     * @return ReloadPhase
     */
    public ReloadPhase getPhase() {
        return phase;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Utility method to safely call the event.
     * <p>
     * Any exception thrown by listeners is caught to avoid breaking the reload sequence.
     *
     * @param core  ODailyQuests instance being reloaded
     * @param phase current phase of the reload lifecycle
     */
    public static void call(ODailyQuests core, ReloadPhase phase) {
        final ODQReloadEvent event = new ODQReloadEvent(core, phase);
        try {
            core.getServer().getPluginManager().callEvent(event);
        } catch (Exception e) {
            PluginLogger.error("An error occurred while calling ODQReloadEvent: " + e.getMessage());
        }
    }
}
