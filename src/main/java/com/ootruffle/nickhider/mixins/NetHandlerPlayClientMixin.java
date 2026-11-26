package com.ootruffle.nickhider.mixins;

import com.ootruffle.nickhider.NickDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {

    @Inject(method = "handleChat", at = @At("HEAD"))
    private void nickhider$onChat(S02PacketChat packet, CallbackInfo ci) {
        // handleChat runs once on the netty thread (which only re-queues it) and once on the main thread.
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            return;
        }
        if (packet.getType() == 2 || packet.getChatComponent() == null) {
            return; // action bar
        }
        NickDetector.onChat(packet.getChatComponent().getUnformattedText());
    }
}
