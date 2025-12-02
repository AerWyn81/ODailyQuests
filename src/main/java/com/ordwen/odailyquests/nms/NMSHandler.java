package com.ordwen.odailyquests.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public final class NMSHandler {

    private static final UUID DUMMY_UUID = UUID.randomUUID();
    private static final String VERSION = Bukkit.getBukkitVersion().split("-")[0];

    // null = unknown yet; true = supported; false = not supported
    private static volatile Boolean hasSetItemModel = null;

    private NMSHandler() {
        // Utility class
    }

    public static String getVersion() {
        return VERSION;
    }

    public static boolean isVersionAtLeast(String versionPrefix) {
        return VERSION.compareTo(versionPrefix) >= 0;
    }

    public static void trySetItemModel(ItemMeta meta, String itemModel) {
        if (Boolean.FALSE.equals(hasSetItemModel)) return;
        if (meta == null || itemModel == null || itemModel.isEmpty()) return;

        // Accept "minecraft:foo" or just "foo"
        final String namespace;
        final String value;
        final int colon = itemModel.indexOf(':');
        if (colon >= 0) {
            namespace = itemModel.substring(0, colon);
            value = itemModel.substring(colon + 1);
        } else {
            namespace = NamespacedKey.MINECRAFT; // "minecraft"
            value = itemModel;
        }

        final NamespacedKey key = new NamespacedKey(namespace, value);

        try {
            final Method m = ItemMeta.class.getMethod("setItemModel", NamespacedKey.class);
            m.invoke(meta, key);
            hasSetItemModel = true;
        } catch (NoSuchMethodException e) {
            hasSetItemModel = false;
            Debugger.write("ItemMeta#setItemModel not present; feature disabled.");
        } catch (Exception e) {
            hasSetItemModel = true;
            PluginLogger.error("Failed to set item model '" + namespace + ":" + value + "': " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public static SkullMeta applySkullTexture(SkullMeta skullMeta, String texture) {
        if (isVersionAtLeast("1.18.1")) {
            return applyTextureModern(skullMeta, texture);
        } else {
            return applyTextureLegacy(skullMeta, texture);
        }
    }

    private static SkullMeta applyTextureModern(SkullMeta skullMeta, String texture) {
        final PlayerProfile profile = Bukkit.createPlayerProfile(DUMMY_UUID);
        final PlayerTextures textures = profile.getTextures();

        final URL url;
        try {
            url = URI.create("https://textures.minecraft.net/texture/" + texture).toURL();
        } catch (MalformedURLException e) {
            PluginLogger.error("Failed to apply skull texture: " + e.getMessage());
            return skullMeta;
        }

        textures.setSkin(url);
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);

        return skullMeta;
    }

    private static SkullMeta applyTextureLegacy(SkullMeta skullMeta, String texture) {
        final GameProfile profile = new GameProfile(DUMMY_UUID, "odq_skull");
        final String toEncode = "{textures:{SKIN:{url:\"https://textures.minecraft.net/texture/" + texture + "\"}}}";
        final String encoded = Base64.getEncoder().encodeToString(toEncode.getBytes());
        profile.getProperties().put("textures", new Property("textures", encoded));

        try {
            final Method setProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            setProfileMethod.setAccessible(true);
            setProfileMethod.invoke(skullMeta, profile);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            PluginLogger.error("Failed to apply skull texture: " + e.getMessage());
        }

        return skullMeta;
    }
}
