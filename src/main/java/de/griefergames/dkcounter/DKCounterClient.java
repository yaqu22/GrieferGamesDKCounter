
package de.griefergames.dkcounter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DKCounterClient implements ClientModInitializer {

    public static final Pattern COUNT_PATTERN =
            Pattern.compile("(?i)Anzahl\\s*:\\s*([0-9][0-9.,'’ ]*)");

    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) {
                ScreenEvents.afterRender(screen).register(
                        (currentScreen, context, mouseX, mouseY, tickDelta) -> {
                            renderTotals(context, client);
                        }
                );
            }
        });
    }

    private static void renderTotals(DrawContext context, MinecraftClient client) {
        if (client.player == null) return;

        var player = client.player;
        var inv = player.getInventory();
        Map<Item, Long> totals = new LinkedHashMap<>();

        for (ItemStack stack : inv.getMainStacks()) {
            addStack(totals, stack, client);
        }

        addStack(totals, player.getEquippedStack(EquipmentSlot.HEAD), client);
        addStack(totals, player.getEquippedStack(EquipmentSlot.CHEST), client);
        addStack(totals, player.getEquippedStack(EquipmentSlot.LEGS), client);
        addStack(totals, player.getEquippedStack(EquipmentSlot.FEET), client);
        addStack(totals, player.getOffHandStack(), client);

        int x = (client.getWindow().getScaledWidth() - 176) / 2 + 180;
        int y = (client.getWindow().getScaledHeight() - 166) / 2 + 8;

        context.drawTextWithShadow(
                client.textRenderer,
                "GrieferGames - DK Counter",
                x, y, Formatting.GOLD.getColorValue()
        );

        y += 14;

        for (Map.Entry<Item, Long> entry : totals.entrySet()) {
            String line = entry.getKey().getName().getString()
                    + ": " + formatAmount(entry.getValue());

            context.drawTextWithShadow(
                    client.textRenderer,
                    line, x, y, Formatting.WHITE.getColorValue()
            );
            y += 11;
        }

        if (totals.isEmpty()) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    "Keine komprimierten Blöcke",
                    x, y, Formatting.GRAY.getColorValue()
            );
        }
    }

    private static void addStack(
            Map<Item, Long> totals, ItemStack stack, MinecraftClient client
    ) {
        if (stack == null || stack.isEmpty()) return;

        long amount = readCompressedAmount(stack, client);
        if (amount > 0) {
            totals.merge(stack.getItem(), amount, Long::sum);
        }
    }

    public static long readCompressedAmount(
            ItemStack stack, MinecraftClient client
    ) {
        if (stack == null || stack.isEmpty() || client.player == null) return 0L;

        var tooltip = stack.getTooltip(
                Item.TooltipContext.create(client.world),
                client.player,
                TooltipType.BASIC
        );

        for (Text line : tooltip) {
            Matcher matcher = COUNT_PATTERN.matcher(line.getString());
            if (matcher.find()) {
                String digits = matcher.group(1).replaceAll("[^0-9]", "");
                try {
                    return Math.multiplyExact(
                            Long.parseLong(digits), stack.getCount()
                    );
                } catch (NumberFormatException | ArithmeticException ignored) {
                    return 0L;
                }
            }
        }
        return 0L;
    }

    public static String formatAmount(long total) {
        long dks = total / 3456;
        long remainder = total % 3456;
        long stacks = remainder / 64;
        long items = remainder % 64;

        StringBuilder out = new StringBuilder();
        if (dks > 0) out.append(dks).append(" DK");
        if (stacks > 0) {
            if (!out.isEmpty()) out.append(" + ");
            out.append(stacks).append(" Stacks");
        }
        if (items > 0 || out.isEmpty()) {
            if (!out.isEmpty()) out.append(" + ");
            out.append(items).append(" Items");
        }
        return out.toString();
    


    

