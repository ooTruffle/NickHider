package com.ootruffle.nickhider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds your Hypixel /nick so it gets hidden too, from:
 * <ul>
 *     <li>Hypixel's "You are now nicked as X!" message (and "Your nick has been reset" to clear it),</li>
 *     <li>your own tab list entry, which shows the nick while you are nicked.</li>
 * </ul>
 * The nick is saved, since Hypixel keeps it across sessions.
 */
public final class NickDetector {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final Pattern NICKED_AS = Pattern.compile("^You are now nicked as ([A-Za-z0-9_]{3,16})");

    private NickDetector() {
    }

    /** The detected nick, or null when there is none or detection is off. */
    public static String nick() {
        if (!NickHiderConfig.autoDetectNick) {
            return null;
        }
        String n = NickHiderConfig.detectedNick;
        return n != null && VALID_NAME.matcher(n).matches() ? n : null;
    }

    public static void onChat(String raw) {
        if (raw == null || !NickHiderConfig.autoDetectNick) {
            return;
        }
        String text = raw.trim();
        Matcher m = NICKED_AS.matcher(text);
        if (m.find()) {
            set(m.group(1));
        } else if (text.startsWith("Your nick has been reset")) {
            set("");
        }
    }

    /** Your own tab entry carries the nick while you are nicked. */
    public static void tick(Minecraft mc) {
        if (!NickHiderConfig.autoDetectNick || mc.thePlayer == null || mc.getSession() == null) {
            return;
        }
        NetHandlerPlayClient net = mc.getNetHandler();
        if (net == null) {
            return;
        }
        NetworkPlayerInfo info = net.getPlayerInfo(mc.thePlayer.getUniqueID());
        if (info == null || info.getGameProfile() == null) {
            return;
        }
        String tabName = info.getGameProfile().getName();
        if (tabName != null && VALID_NAME.matcher(tabName).matches()
                && !tabName.equalsIgnoreCase(mc.getSession().getUsername())) {
            set(tabName);
        }
    }

    private static void set(String nick) {
        if (nick.equals(NickHiderConfig.detectedNick)) {
            return;
        }
        NickHiderConfig.detectedNick = nick;
        NickHiderConfig.saveConfig();
    }
}
