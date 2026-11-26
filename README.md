# Nick Hider

A client-side nick hider for Minecraft 1.8.9 (Ornithe + OneConfig). It shows a fake name in place of your own
everywhere text is drawn — chat, tab list, nametags, scoreboard and menus. Other players still see your real name.

# AI Disclosure
This mod was done by Claude, feel free to pr to clean up the code ^^
#

## Features

- **Fake name** with optional `&` colour codes. Ranks and colours around your name are kept.
- **Auto-detects your Hypixel `/nick`** from the "You are now nicked as" message or your own tab list entry,
  and remembers it between sessions until your nick is reset.
- **Also Hide** list for alts or other names.
- **Skin replacement and cape hiding**: show Steve or Alex instead of your skin, or your real skin
  even while a server gives you another one (such as your Hypixel `/nick` skin).
- **Streamer mode**: gives every other player an alias such as `Player3`, optionally with default skins.
- **Toggle key** (Controls → Nick Hider).
- Works with Argentum's text renderer.

Settings are in OneConfig under **Hypixel → Nick Hider**.

## Requirements

- Minecraft 1.8.9 with Ornithe (Fabric Loader)
- Ornithe Standard Libraries
- OneConfig

## Building

```
./gradlew build
```

The jar is written to `build/libs/`.
