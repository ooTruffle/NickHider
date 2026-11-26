package com.ootruffle.nickhider;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.polyfrost.oneconfig.api.commands.v1.CommandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * /nickhider - quick control without opening OneConfig.
 * <pre>
 * /nickhider                 status and help
 * /nickhider toggle|on|off   turn the mod on or off
 * /nickhider name &lt;name&gt;     set the fake name (&amp; colour codes work)
 * /nickhider hide &lt;name&gt;     add a name to Also Hide
 * /nickhider unhide &lt;name&gt;   remove a name from Also Hide
 * /nickhider others          turn streamer mode (anonymise other players) on or off
 * /nickhider reset           forget the detected Hypixel nick
 * </pre>
 */
public final class NickHiderCommand {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final String PREFIX = "§8[§dNick Hider§8] §7";

    private NickHiderCommand() {
    }

    public static void register() {
        CommandManager.INSTANCE.register(
                CommandManager.literal("nickhider")
                        .executes(ctx -> {
                            status();
                            return 1;
                        })
                        .then(CommandManager.literal("toggle").executes(ctx -> {
                            setEnabled(!NickHiderConfig.enabled);
                            return 1;
                        }))
                        .then(CommandManager.literal("on").executes(ctx -> {
                            setEnabled(true);
                            return 1;
                        }))
                        .then(CommandManager.literal("off").executes(ctx -> {
                            setEnabled(false);
                            return 1;
                        }))
                        .then(CommandManager.literal("name")
                                .then(CommandManager.INSTANCE.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            setName(StringArgumentType.getString(ctx, "name"));
                                            return 1;
                                        })))
                        .then(CommandManager.literal("hide")
                                .then(CommandManager.INSTANCE.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            hide(StringArgumentType.getString(ctx, "name"));
                                            return 1;
                                        })))
                        .then(CommandManager.literal("unhide")
                                .then(CommandManager.INSTANCE.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            unhide(StringArgumentType.getString(ctx, "name"));
                                            return 1;
                                        })))
                        .then(CommandManager.literal("others").executes(ctx -> {
                            NickHiderConfig.hideOthers = !NickHiderConfig.hideOthers;
                            changed();
                            say("Anonymise other players: " + onOff(NickHiderConfig.hideOthers));
                            return 1;
                        }))
                        .then(CommandManager.literal("reset").executes(ctx -> {
                            String old = NickHiderConfig.detectedNick;
                            NickHiderConfig.detectedNick = "";
                            changed();
                            say(old == null || old.isEmpty() ? "No nick was detected."
                                    : "Forgot the detected nick " + reveal(old) + "§7.");
                            return 1;
                        }))
        );
    }

    private static void status() {
        say("Nick Hider is " + onOff(NickHiderConfig.enabled) + "§7. Fake name: §f" + colour(NickHiderConfig.fakeName));
        String nick = NickDetector.nick();
        say("Detected nick: " + (nick != null ? reveal(nick) : "§8none")
                + "§7. Also hiding: " + (extras().isEmpty() ? "§8nothing" : revealAll(extras())));
        say("Anonymise others: " + onOff(NickHiderConfig.hideOthers));
        say("§e/nickhider toggle|on|off§7, §ename <name>§7, §ehide|unhide <name>§7, "
                + "§eothers§7, §ereset");
    }

    private static void setEnabled(boolean on) {
        NickHiderConfig.enabled = on;
        changed();
        say("Nick Hider " + onOff(on));
    }

    private static void setName(String name) {
        name = name.trim();
        if (name.isEmpty()) {
            say("§cGive a name, e.g. /nickhider name You");
            return;
        }
        NickHiderConfig.fakeName = name;
        changed();
        say("Fake name set to §f" + colour(name));
    }

    private static void hide(String name) {
        if (!VALID_NAME.matcher(name).matches()) {
            say("§cNot a valid Minecraft name: " + name);
            return;
        }
        List<String> names = extras();
        for (String n : names) {
            if (n.equalsIgnoreCase(name)) {
                say(reveal(n) + "§7 is already hidden.");
                return;
            }
        }
        names.add(name);
        NickHiderConfig.extraNames = String.join(", ", names);
        changed();
        say("Now also hiding " + reveal(name) + "§7.");
    }

    private static void unhide(String name) {
        List<String> names = extras();
        if (!names.removeIf(n -> n.equalsIgnoreCase(name))) {
            say(reveal(name) + "§7 isn't in Also Hide.");
            return;
        }
        NickHiderConfig.extraNames = String.join(", ", names);
        changed();
        say("No longer hiding " + reveal(name) + "§7.");
    }

    private static List<String> extras() {
        List<String> names = new ArrayList<>();
        if (NickHiderConfig.extraNames != null) {
            for (String n : NickHiderConfig.extraNames.split(",")) {
                if (!n.trim().isEmpty()) {
                    names.add(n.trim());
                }
            }
        }
        return names;
    }

    private static void changed() {
        NickHiderConfig.saveConfig();
        NameReplacer.refresh();
    }

    private static String onOff(boolean on) {
        return on ? "§aon" : "§coff";
    }

    /**
     * Shows a hidden name (a plain Minecraft name) without it being swapped out when the message is drawn:
     * a repeated colour code splits it, so the whole name never appears in one piece.
     */
    private static String reveal(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        return "§f" + name.charAt(0) + "§f" + name.substring(1);
    }

    private static String colour(String s) {
        return s == null ? "" : s.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    private static String revealAll(List<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String n : names) {
            if (sb.length() > 0) {
                sb.append("§7, ");
            }
            sb.append(reveal(n));
        }
        return sb.toString();
    }

    private static void say(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(PREFIX + message));
        }
    }
}
