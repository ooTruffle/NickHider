package com.ootruffle.nickhider.mixins;

import com.ootruffle.nickhider.NameReplacer;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Replaces names in text as it is drawn and measured, so widths (tab list columns, nametag backgrounds,
 * scoreboard) fit the fake name. Both the public draw entry point and renderStringAtPos are hooked so
 * split/aligned text paths are covered too; replacement is idempotent, so the double pass is harmless.
 */
@Mixin(value = FontRenderer.class, priority = 900)
public abstract class FontRendererMixin {

    @ModifyVariable(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String nickhider$drawString(String text) {
        return NameReplacer.replace(text);
    }

    @ModifyVariable(method = "renderStringAtPos", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String nickhider$renderStringAtPos(String text) {
        return NameReplacer.replace(text);
    }

    @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String nickhider$getStringWidth(String text) {
        return NameReplacer.replace(text);
    }
}
