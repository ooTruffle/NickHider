package com.ootruffle.nickhider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Swaps names for their replacements in any string about to be drawn or measured.
 * Matching is case-insensitive and whole-word, where a formatting code (e.g. {@code §a}) counts as a word break,
 * so {@code §aTruffle} matches but {@code Truffles} does not.
 */
public final class NameReplacer {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final int CACHE_LIMIT = 4096;
    private static final int REFRESH_TICKS = 10;

    /** Immutable snapshot, swapped in one write so a half-built state is never used. */
    private static final class State {
        final Pattern pattern;
        final Map<String, String> replacements; // lower-case name -> replacement
        final Map<String, String> cache = new HashMap<>();

        State(Pattern pattern, Map<String, String> replacements) {
            this.pattern = pattern;
            this.replacements = replacements;
        }
    }

    private static volatile State state;
    private static String lastKey = "";
    private static boolean lastEnabled;
    private static int ticks;

    /** Real (lower-case) name -> alias number, kept for the whole game session so aliases are stable. */
    private static final Map<String, Integer> aliases = new HashMap<>();

    private NameReplacer() {
    }

    public static String replace(String text) {
        State s = state;
        if (s == null || text == null || text.isEmpty()) {
            return text;
        }
        String cached = s.cache.get(text);
        if (cached != null) {
            return cached;
        }
        String out = text;
        Matcher m = s.pattern.matcher(text);
        if (m.find()) {
            StringBuilder sb = new StringBuilder(text.length() + 16);
            int last = 0;
            do {
                sb.append(text, last, m.start());
                String rep = s.replacements.get(m.group().toLowerCase(Locale.ROOT));
                sb.append(rep != null ? rep : m.group());
                last = m.end();
            } while (m.find());
            sb.append(text, last, text.length());
            out = sb.toString();
        }
        if (s.cache.size() >= CACHE_LIMIT) {
            s.cache.clear();
        }
        s.cache.put(text, out);
        return out;
    }

    public static void tick(Minecraft mc) {
        // Rebuild immediately when the toggle flips, otherwise twice a second.
        if (++ticks < REFRESH_TICKS && lastEnabled == NickHiderConfig.enabled) {
            return;
        }
        ticks = 0;
        lastEnabled = NickHiderConfig.enabled;
        rebuild(mc);
    }

    private static void rebuild(Minecraft mc) {
        if (!NickHiderConfig.enabled) {
            state = null;
            lastKey = "";
            return;
        }

        String fake = colour(orDefault(NickHiderConfig.fakeName, "You"));
        Map<String, String> reps = new LinkedHashMap<>();

        if (NickHiderConfig.hideOthers) {
            String prefix = colour(orDefault(NickHiderConfig.aliasPrefix, "Player"));
            for (String other : otherNames(mc)) {
                String key = other.toLowerCase(Locale.ROOT);
                Integer alias = aliases.get(key);
                if (alias == null) {
                    alias = aliases.size() + 1;
                    aliases.put(key, alias);
                }
                reps.put(key, prefix + alias);
            }
        }

        // Own names last so they win over an alias for the same name.
        String own = mc.getSession() != null ? mc.getSession().getUsername() : null;
        addName(reps, own, fake);
        addName(reps, NickDetector.nick(), fake);
        if (NickHiderConfig.extraNames != null) {
            for (String extra : NickHiderConfig.extraNames.split(",")) {
                addName(reps, extra.trim(), fake);
            }
        }

        // A name that appears inside a replacement would be swapped again on the next draw pass; drop it.
        reps.keySet().removeIf(name -> {
            Pattern single = wordPattern(new ArrayList<>(Collections.singletonList(name)));
            for (String value : reps.values()) {
                if (single.matcher(value).find()) {
                    return true;
                }
            }
            return false;
        });

        String key = reps.toString();
        if (key.equals(lastKey) && state != null) {
            return;
        }
        lastKey = key;
        state = reps.isEmpty() ? null : new State(wordPattern(new ArrayList<>(reps.keySet())), reps);
    }

    private static void addName(Map<String, String> reps, String name, String replacement) {
        if (name != null && VALID_NAME.matcher(name).matches()) {
            reps.put(name.toLowerCase(Locale.ROOT), replacement);
        }
    }

    private static Set<String> otherNames(Minecraft mc) {
        Set<String> names = new HashSet<>();
        String own = mc.getSession() != null ? mc.getSession().getUsername() : "";
        NetHandlerPlayClient handler = mc.getNetHandler();
        if (handler != null) {
            for (NetworkPlayerInfo info : handler.getPlayerInfoMap()) {
                if (info.getGameProfile() != null) {
                    names.add(info.getGameProfile().getName());
                }
            }
        }
        if (mc.theWorld != null) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                names.add(player.getName());
            }
        }
        names.removeIf(n -> n == null || n.equalsIgnoreCase(own) || !VALID_NAME.matcher(n).matches());
        return names;
    }

    /** Longest first, so "Truffle" doesn't shadow "Truffle_2". */
    private static Pattern wordPattern(List<String> names) {
        names.sort((a, b) -> b.length() - a.length());
        StringBuilder alt = new StringBuilder();
        for (String n : names) {
            if (alt.length() > 0) {
                alt.append('|');
            }
            alt.append(Pattern.quote(n));
        }
        // Not preceded by a word character, unless that character belongs to a formatting code (§a).
        return Pattern.compile("(?<!(?<!§)[A-Za-z0-9_])(?:" + alt + ")(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);
    }

    private static String orDefault(String s, String def) {
        return s == null || s.trim().isEmpty() ? def : s.trim();
    }

    private static String colour(String s) {
        return s.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }
}
