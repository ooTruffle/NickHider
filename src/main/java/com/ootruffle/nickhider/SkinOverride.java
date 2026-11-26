package com.ootruffle.nickhider;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public final class SkinOverride {

    public static final int STEVE_SKIN = 0;
    public static final int ALEX_SKIN = 1;
    public static final int REAL_SKIN = 2;

    private static final ResourceLocation STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation ALEX = new ResourceLocation("textures/entity/alex.png");

    /** Your account's own skin, loaded once through the vanilla skin manager. */
    private static volatile ResourceLocation realSkin;
    private static volatile String realModel = "default";
    private static boolean realRequested;

    private SkinOverride() {
    }

    public static boolean isSelf(GameProfile profile) {
        if (profile == null) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getSession() == null) {
            return false;
        }
        GameProfile own = mc.getSession().getProfile();
        if (own != null && own.getId() != null && own.getId().equals(profile.getId())) {
            return true;
        }
        return profile.getName() != null && profile.getName().equalsIgnoreCase(mc.getSession().getUsername());
    }

    public static boolean hideSkin(GameProfile profile) {
        if (!NickHiderConfig.enabled || profile == null) {
            return false;
        }
        return isSelf(profile) ? NickHiderConfig.hideSkin
                : NickHiderConfig.hideOthers && NickHiderConfig.hideOthersSkins;
    }

    public static boolean hideCape(GameProfile profile) {
        if (!NickHiderConfig.enabled || profile == null) {
            return false;
        }
        return isSelf(profile) ? NickHiderConfig.hideCape
                : NickHiderConfig.hideOthers && NickHiderConfig.hideOthersSkins;
    }

    /** The skin to show instead, or null to leave the player's skin alone. */
    public static ResourceLocation skin(GameProfile profile) {
        if (isSelf(profile)) {
            switch (NickHiderConfig.skinType) {
                case ALEX_SKIN:
                    return ALEX;
                case REAL_SKIN:
                    return realSkin(); // null until loaded, or when it can't be: keep whatever the server gave
                default:
                    return STEVE;
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(id(profile));
    }

    /** The model to go with {@link #skin}, or null to leave it alone. */
    public static String model(GameProfile profile) {
        if (isSelf(profile)) {
            switch (NickHiderConfig.skinType) {
                case ALEX_SKIN:
                    return "slim";
                case REAL_SKIN:
                    return realSkin() != null ? realModel : null;
                default:
                    return "default";
            }
        }
        return DefaultPlayerSkin.getSkinType(id(profile));
    }

    /**
     * Your own skin, even while a server shows you with another one (e.g. a Hypixel /nick skin).
     * The skin manager looks up a profile with your account's UUID using your login session's textures.
     */
    private static ResourceLocation realSkin() {
        if (!realRequested) {
            realRequested = true;
            Minecraft mc = Minecraft.getMinecraft();
            GameProfile own = mc.getSession().getProfile();
            if (own != null && own.getId() != null) {
                GameProfile lookup = new GameProfile(own.getId(), own.getName());
                mc.getSkinManager().loadProfileTextures(lookup, (type, location, texture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        String model = texture.getMetadata("model");
                        realModel = model == null ? "default" : model;
                        realSkin = location;
                    }
                }, false);
            }
        }
        return realSkin;
    }

    private static UUID id(GameProfile profile) {
        return profile.getId() != null ? profile.getId() : new UUID(0L, 0L);
    }
}
