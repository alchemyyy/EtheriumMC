package dev.imanity.antixray.sdk;

import org.jspecify.annotations.Nullable;

/**
 * Holds the {@link AntiXrayAdapter} registered by the Raytrace AntiXray plugin.
 */
public final class AntiXraySDK {
    private static @Nullable AntiXrayAdapter adapter;

    private AntiXraySDK() {
    }

    /**
     * Gets the currently registered adapter.
     *
     * @return the adapter, or {@code null} if none has been registered
     */
    public static @Nullable AntiXrayAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the adapter that block changes and interactions are forwarded to.
     *
     * @param adapter the adapter to register, or null to unregister the current one
     */
    public static void setAdapter(@Nullable AntiXrayAdapter adapter) {
        AntiXraySDK.adapter = adapter;
    }
}
