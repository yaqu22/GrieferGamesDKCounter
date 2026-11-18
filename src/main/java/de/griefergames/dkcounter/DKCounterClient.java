package de.griefergames.dkcounter;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DKCounterClient implements ClientModInitializer {
    public static final Pattern COUNT_PATTERN =
            Pattern.compile("(?i)Anzahl\\s*:\\s*([0-9][0-9.,'’ ]*)");

    @Override
    public void onInitializeClient() {
        // The inventory-wide total is rendered by InventoryScreenMixin.
    }

    public static long readCompressedAmount(ItemStack stack, MinecraftClient client) {
        if (stack == null || stack.isEmpty() || client.player == null) return 0L;

        var tooltip = stack.getTooltip(
                net.minecraft.item.Item.TooltipContext.create(client.world),
                client.player,
                TooltipType.BASIC
        );

        for (Text line : tooltip) {
            Matcher matcher = COUNT_PATTERN.matcher(line.getString());
            if (matcher.find()) {
                String digits = matcher.group(1).replaceAll("[^0-9]", "");
                try {
                    // If a slot contains multiple identical compressed blocks,
                    // count the displayed amount once for each block.
                    return Math.multiplyExact(Long.parseLong(digits), stack.getCount());
                } catch (NumberFormatException | ArithmeticException ignored) {
                    return 0L;
                }
            }
        }
        return 0L;
    }

    public static String formatAmount(long total) {
        // A DK is a double chest: 54 slots × 64 items = 3,456 items.
        final long itemsPerStack = 64L;
        final long itemsPerDoubleChest = 54L * itemsPerStack;

        long dks = total / itemsPerDoubleChest;
        long remainder = total % itemsPerDoubleChest;
        long stacks = remainder / itemsPerStack;
        long items = remainder % itemsPerStack;

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
    }
}
