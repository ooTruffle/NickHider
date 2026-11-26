package com.ootruffle.nickhider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;

public class NickHider implements ModInitializer {
    public static final String MODID = "nickhider";
    public static final String NAME = "Nick Hider";

    private static KeyBinding toggleKey;

    @Override
    public void init() {
        NickHiderConfig.register();
        KeybindEvents.REGISTER_KEYBINDS.register(() ->
                toggleKey = KeybindRegistry.register("key.nickhider.toggle", 0, "key.categories.nickhider"));
    }

    /** Called at the end of every client tick. */
    public static void onTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (toggleKey != null) {
            while (toggleKey.isPressed()) {
                NickHiderConfig.enabled = !NickHiderConfig.enabled;
                NickHiderConfig.saveConfig();
                if (mc.thePlayer != null && NickHiderConfig.toggleMessage) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            "§8[§dNick Hider§8] " + (NickHiderConfig.enabled ? "§aEnabled" : "§cDisabled")));
                }
            }
        }
        NickDetector.tick(mc);
        NameReplacer.tick(mc);
    }
}
