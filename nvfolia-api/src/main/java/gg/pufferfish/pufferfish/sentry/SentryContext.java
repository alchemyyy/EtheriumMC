package gg.pufferfish.pufferfish.sentry;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.ThreadContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jspecify.annotations.Nullable;

/**
 * Stores plugin, sender and event details in the log4j {@link ThreadContext} so Sentry reports can include them.
 */
public final class SentryContext {
    private static final Gson GSON = new Gson();

    private SentryContext() {
    }

    /**
     * Records the plugin currently being executed.
     *
     * @param plugin the plugin, ignored if {@code null}
     */
    public static void setPluginContext(@Nullable Plugin plugin) {
        if (plugin != null) {
            ThreadContext.put("pufferfishsentry_pluginname", plugin.getName());
            ThreadContext.put("pufferfishsentry_pluginversion", plugin.getPluginMeta().getVersion());
        }
    }

    /**
     * Clears the recorded plugin.
     */
    public static void removePluginContext() {
        ThreadContext.remove("pufferfishsentry_pluginname");
        ThreadContext.remove("pufferfishsentry_pluginversion");
    }

    /**
     * Records the command sender currently being handled.
     *
     * @param sender the sender, ignored if {@code null}
     */
    public static void setSenderContext(@Nullable CommandSender sender) {
        if (sender != null) {
            ThreadContext.put("pufferfishsentry_playername", sender.getName());
            if (sender instanceof Player player) {
                ThreadContext.put("pufferfishsentry_playerid", player.getUniqueId().toString());
            }
        }
    }

    /**
     * Clears the recorded command sender.
     */
    public static void removeSenderContext() {
        ThreadContext.remove("pufferfishsentry_playername");
        ThreadContext.remove("pufferfishsentry_playerid");
    }

    /**
     * Records the event being dispatched, the listener's plugin and any player involved.
     *
     * @param event        the event being dispatched
     * @param registration the listener receiving the event
     */
    public static void setEventContext(Event event, RegisteredListener registration) {
        setPluginContext(registration.getPlugin());

        try {
            // Find the player that was involved with this event
            Player player = null;
            if (event instanceof PlayerEvent) {
                player = ((PlayerEvent) event).getPlayer();
            } else {
                Class<? extends Event> eventClass = event.getClass();

                Field playerField = null;

                for (Field field : eventClass.getDeclaredFields()) {
                    if (field.getType().equals(Player.class)) {
                        playerField = field;
                        break;
                    }
                }

                if (playerField != null) {
                    playerField.setAccessible(true);
                    player = (Player) playerField.get(event);
                }
            }

            if (player != null) {
                setSenderContext(player);
            }
        } catch (Exception _) {
        } // We can't really safely log exceptions.

        ThreadContext.put("pufferfishsentry_eventdata", GSON.toJson(serializeFields(event)));
    }

    /**
     * Clears the recorded event, plugin and sender.
     */
    public static void removeEventContext() {
        removePluginContext();
        removeSenderContext();
        ThreadContext.remove("pufferfishsentry_eventdata");
    }

    private static Map<String, String> serializeFields(Object object) {
        Map<String, String> fields = new TreeMap<>();
        fields.put("_class", object.getClass().getName());
        for (Field declaredField : object.getClass().getDeclaredFields()) {
            try {
                if (Modifier.isStatic(declaredField.getModifiers())) {
                    continue;
                }

                String fieldName = declaredField.getName();
                if (fieldName.equals("handlers")) {
                    continue;
                }
                declaredField.setAccessible(true);
                Object value = declaredField.get(object);
                if (value != null) {
                    fields.put(fieldName, value.toString());
                } else {
                    fields.put(fieldName, "<null>");
                }
            } catch (Exception _) {
            } // We can't really safely log exceptions.
        }
        return fields;
    }

    /**
     * Holds the context of whatever is currently being executed. Every field is unset
     * until the corresponding setter is called, so all getters may return {@code null}.
     */
    public static class State {

        private @Nullable Plugin plugin;
        private @Nullable Command command;
        private @Nullable String commandLine;
        private @Nullable Event event;
        private @Nullable RegisteredListener registeredListener;

        /**
         * Gets the plugin being executed.
         *
         * @return the plugin, or {@code null} if unset
         */
        public @Nullable Plugin getPlugin() {
            return this.plugin;
        }

        /**
         * Sets the plugin being executed.
         *
         * @param plugin the plugin
         */
        public void setPlugin(@Nullable Plugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Gets the command being executed.
         *
         * @return the command, or {@code null} if unset
         */
        public @Nullable Command getCommand() {
            return this.command;
        }

        /**
         * Sets the command being executed.
         *
         * @param command the command
         */
        public void setCommand(@Nullable Command command) {
            this.command = command;
        }

        /**
         * Gets the raw command line being executed.
         *
         * @return the command line, or {@code null} if unset
         */
        public @Nullable String getCommandLine() {
            return this.commandLine;
        }

        /**
         * Sets the raw command line being executed.
         *
         * @param commandLine the command line
         */
        public void setCommandLine(@Nullable String commandLine) {
            this.commandLine = commandLine;
        }

        /**
         * Gets the event being dispatched.
         *
         * @return the event, or {@code null} if unset
         */
        public @Nullable Event getEvent() {
            return this.event;
        }

        /**
         * Sets the event being dispatched.
         *
         * @param event the event
         */
        public void setEvent(@Nullable Event event) {
            this.event = event;
        }

        /**
         * Gets the listener receiving the event.
         *
         * @return the listener, or {@code null} if unset
         */
        public @Nullable RegisteredListener getRegisteredListener() {
            return this.registeredListener;
        }

        /**
         * Sets the listener receiving the event.
         *
         * @param registeredListener the listener
         */
        public void setRegisteredListener(@Nullable RegisteredListener registeredListener) {
            this.registeredListener = registeredListener;
        }
    }
}
