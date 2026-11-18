# GrieferGames DK Counter — Fabric 1.21.11

Client-side Fabric mod source project. In the player's normal inventory screen it groups compressed amounts **separately by Minecraft item type**:
- quartz blocks are added to quartz blocks,
- oak logs are added to oak logs,
- other block types are counted separately.

It scans the hotbar, main inventory, armor and offhand. It does not scan chests or shulker-box contents.

For each stack it looks for a tooltip line like `Anzahl: 6.561`, then adds that amount to the total for that item type. Results are drawn in the inventory screen.

## Conversion
- 1 Stack = 64 items
- 1 DK (double chest) = 54 stacks = 3,456 items

Example: 6,561 quartz blocks = 1 DK + 48 Stacks + 33 Items.

## Build
Requires Java 21 and Gradle. Run `gradle build` from this folder. The JAR should appear in `build/libs/`.

## Install
Use Minecraft 1.21.11 with Fabric Loader and Fabric API. Put the built JAR in the instance's `mods` folder.

## Important testing note
The mod obtains tooltip text from the client-side ItemStack tooltip API. It will only count a stack if the GrieferGames `Anzahl:` line is available through that API even when the item is not hovered. If the server/client only creates that line dynamically while hovering, the implementation needs a different approach (capture and cache tooltip values while hovered).

This is source code, not a compiled or in-game-tested JAR.

## Build automatically on GitHub
1. Create a new GitHub repository and upload the contents of this project (the files inside `GrieferGamesDKCounter`, not the ZIP itself).
2. Open the repository's **Actions** tab.
3. Select **Build GrieferGames DK Counter** and click **Run workflow**.
4. When the run finishes successfully, open it and download the `griefergames-dk-counter` artifact. It contains the mod JAR.

The workflow compiles the source; it does not replace testing in Minecraft. If the build reports an error, the source/API compatibility must be fixed before the JAR can be used.
