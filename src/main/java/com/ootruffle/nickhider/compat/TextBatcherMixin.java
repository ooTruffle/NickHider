package com.ootruffle.nickhider.compat;

import com.ootruffle.nickhider.NameReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Argentum cancels FontRenderer's own drawing and measuring and does both in its TextBatcher, so names are
 * replaced at its entry points as well. Optional: skipped when Argentum isn't installed or has changed shape.
 */
@Pseudo
@Mixin(targets = "dev.rdh.argentum.impl.render.gui.TextBatcher", remap = false)
public abstract class TextBatcherMixin {

    @ModifyVariable(method = "begin", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private String nickhider$begin(String text) {
        return NameReplacer.replace(text);
    }

    @ModifyVariable(method = "stringWidth", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private String nickhider$stringWidth(String text) {
        return NameReplacer.replace(text);
    }

    @ModifyVariable(method = "shouldFlushBeforeImmediate", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private String nickhider$flushCheck(String text) {
        return NameReplacer.replace(text);
    }
}
