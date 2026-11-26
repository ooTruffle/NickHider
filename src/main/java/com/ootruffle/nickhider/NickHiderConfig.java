package com.ootruffle.nickhider;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Include;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;

public class NickHiderConfig extends Config {

    private static final NickHiderConfig INSTANCE = new NickHiderConfig();

    private NickHiderConfig() {
        super(NickHider.MODID + ".json", "/assets/" + NickHider.MODID + "/icon.png", NickHider.NAME, Category.HYPIXEL);
    }

    public static void register() {
        INSTANCE.preload();
    }

    public static void saveConfig() {
        INSTANCE.save();
    }

    // ---------------- Name ----------------

    @Switch(
            title = "Enabled",
            description = "Replace your name everywhere it is drawn: chat, tab list, nametags, scoreboard and menus.\n" +
                    "Client-side only - other players still see your real name. Bind a toggle key in Controls.",
            category = "Name"
    )
    public static boolean enabled = true;

    @Text(
            title = "Fake Name",
            description = "Shown instead of your name. & colour codes work. Also settable with /nickhider name.",
            placeholder = "You",
            category = "Name"
    )
    public static String fakeName = "You";

    @Text(
            title = "Also Hide",
            description = "Other names to replace with the fake name, separated by commas -\n" +
                    "alts, old names, or a nick that isn't picked up automatically.",
            placeholder = "e.g. MyNick, MyAlt",
            category = "Name"
    )
    public static String extraNames = "";

    @Switch(
            title = "Auto-Detect Hypixel Nick",
            description = "Also hide your /nick, picked up from Hypixel's \"You are now nicked as\" message\n" +
                    "or your own tab list entry. Remembered between sessions until your nick is reset.",
            category = "Name"
    )
    public static boolean autoDetectNick = true;

    /** Last nick seen by {@link NickDetector}; saved, but not shown in the menu. */
    @Include
    public static String detectedNick = "";

    @Switch(
            title = "Toggle Message",
            description = "Say in chat when the toggle key turns the mod on or off.",
            category = "Name"
    )
    public static boolean toggleMessage = true;

    // ---------------- Skin ----------------

    @Switch(
            title = "Replace Your Skin",
            description = "Show another skin on your own player, head and tab icon.",
            category = "Skin"
    )
    public static boolean hideSkin = false;

    @Dropdown(
            title = "Replacement Skin",
            description = "\"Your real skin\" shows your account's own skin even while a server gives you\n" +
                    "another one, such as your Hypixel /nick skin.",
            options = {"Steve", "Alex", "Your real skin"},
            category = "Skin"
    )
    public static int skinType = 0;

    @Switch(
            title = "Hide Your Cape",
            category = "Skin"
    )
    public static boolean hideCape = false;

    // ---------------- Other Players ----------------

    @Switch(
            title = "Anonymise Other Players",
            description = "Replace every other player's name with an alias such as Player3.\n" +
                    "Aliases stay the same until you restart the game.",
            category = "Other Players"
    )
    public static boolean hideOthers = false;

    @Text(
            title = "Alias Prefix",
            description = "Aliases are this followed by a number.",
            placeholder = "Player",
            category = "Other Players"
    )
    public static String aliasPrefix = "Player";

    @Switch(
            title = "Hide Their Skins",
            description = "Show default skins and no capes on other players while anonymising them.",
            category = "Other Players"
    )
    public static boolean hideOthersSkins = false;
}
