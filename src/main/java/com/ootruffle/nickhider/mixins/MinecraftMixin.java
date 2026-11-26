package com.ootruffle.nickhider.mixins;

import com.ootruffle.nickhider.NickHider;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("TAIL"))
    private void nickhider$onTickEnd(CallbackInfo ci) {
        NickHider.onTick();
    }
}
