package com.ootruffle.nickhider;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public final class SkinOverride {

    private static final ResourceLocation STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation ALEX = new ResourceLocation("textures/entity/alex.png");

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

    public static ResourceLocation skin(GameProfile profile) {
        if (isSelf(profile)) {
            if (NickHiderConfig.skinType == 0) {
                return STEVE;
            }
            if (NickHiderConfig.skinType == 1) {
                return ALEX;
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(id(profile));
    }

    public static String model(GameProfile profile) {
        if (isSelf(profile)) {
            if (NickHiderConfig.skinType == 0) {
                return "default";
            }
            if (NickHiderConfig.skinType == 1) {
                return "slim";
            }
        }
        return DefaultPlayerSkin.getSkinType(id(profile));
    }

    private static UUID id(GameProfile profile) {
        return profile.getId() != null ? profile.getId() : new UUID(0L, 0L);
    }
}
