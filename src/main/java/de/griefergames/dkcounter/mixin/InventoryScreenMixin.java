package de.griefergames.dkcounter.mixin;

import de.griefergames.dkcounter.DKCounterClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void griefergamesdkcounter$renderTotals(
            DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerInventory inv = client.player.getInventory();

        // Group by the actual Minecraft item type, not by all compressed items together.
        // Thus quartz is added to quartz, oak logs to oak logs, etc.
        Map<Item, Long> totalsByItem = new LinkedHashMap<>();
        for (ItemStack stack : inv.main) addStack(totalsByItem, stack, client);
        for (ItemStack stack : inv.armor) addStack(totalsByItem, stack, client);
        for (ItemStack stack : inv.offHand) addStack(totalsByItem, stack, client);

        int lineY = y + 6;
        context.drawTextWithShadow(client.textRenderer, "GrieferGames – DK pro Block:",
                x + 8, lineY, Formatting.GOLD.getColorValue());
        lineY += 12;

        for (Map.Entry<Item, Long> entry : totalsByItem.entrySet()) {
            String itemName = entry.getKey().getName().getString();
            String line = itemName + ": " + DKCounterClient.formatAmount(entry.getValue());
            context.drawTextWithShadow(client.textRenderer, line,
                    x + 8, lineY, Formatting.WHITE.getColorValue());
            lineY += 10;
        }

        if (totalsByItem.isEmpty()) {
            context.drawTextWithShadow(client.textRenderer, "Keine komprimierten Blöcke erkannt",
                    x + 8, lineY, Formatting.GRAY.getColorValue());
        }
    }

    private static void addStack(Map<Item, Long> totals, ItemStack stack, MinecraftClient client) {
        if (stack == null || stack.isEmpty()) return;
        long amount = DKCounterClient.readCompressedAmount(stack, client);
        if (amount <= 0) return;
        Item item = stack.getItem();
        try {
            totals.merge(item, amount, Math::addExact);
        } catch (ArithmeticException ignored) {
            totals.put(item, Long.MAX_VALUE);
        }
    }
}
