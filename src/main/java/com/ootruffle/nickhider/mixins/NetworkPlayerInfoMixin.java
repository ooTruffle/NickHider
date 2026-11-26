package com.ootruffle.nickhider.mixins;

import com.mojang.authlib.GameProfile;
import com.ootruffle.nickhider.SkinOverride;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Player models, heads in the tab list and the inventory preview all get their skin from here. */
@Mixin(NetworkPlayerInfo.class)
public abstract class NetworkPlayerInfoMixin {

    @Shadow
    public abstract GameProfile getGameProfile();

    @Inject(method = "getLocationSkin", at = @At("HEAD"), cancellable = true)
    private void nickhider$skin(CallbackInfoReturnable<ResourceLocation> cir) {
        GameProfile profile = getGameProfile();
        if (SkinOverride.hideSkin(profile)) {
            cir.setReturnValue(SkinOverride.skin(profile));
        }
    }

    @Inject(method = "getSkinType", at = @At("HEAD"), cancellable = true)
    private void nickhider$model(CallbackInfoReturnable<String> cir) {
        GameProfile profile = getGameProfile();
        if (SkinOverride.hideSkin(profile)) {
            cir.setReturnValue(SkinOverride.model(profile));
        }
    }

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void nickhider$cape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (SkinOverride.hideCape(getGameProfile())) {
            cir.setReturnValue(null);
        }
    }
}
