
package de.griefergames.dkcounter.mixin;

import de.griefergames.dkcounter.DKCounterClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
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

        var player = client.player;
        var inv = player.getInventory();

        Map<Item, Long> totalsByItem = new LinkedHashMap<>();

        // Hotbar und normales Inventar
        for (ItemStack stack : inv.getMainStacks()) {
            addStack(totalsByItem, stack, client);
        }

        // Rüstung
        addStack(totalsByItem, player.getEquippedStack(EquipmentSlot.HEAD), client);
        addStack(totalsByItem, player.getEquippedStack(EquipmentSlot.CHEST), client);
        addStack(totalsByItem, player.getEquippedStack(EquipmentSlot.LEGS), client);
        addStack(totalsByItem, player.getEquippedStack(EquipmentSlot.FEET), client);

        // Zweithand
        addStack(totalsByItem, player.getOffHandStack(), client);

        int lineY = y + 6;
        context.drawTextWithShadow(
                client.textRenderer,
                "GrieferGames – DK pro Block:",
                x + 8, lineY, Formatting.GOLD.getColorValue()
        );
        lineY += 12;

        for (Map.Entry<Item, Long> entry : totalsByItem.entrySet()) {
            String itemName = entry.getKey().getName().getString();
            String line = itemName + ": " +
                    DKCounterClient.formatAmount(entry.getValue());

            context.drawTextWithShadow(
                    client.textRenderer,
                    line,
                    x + 8, lineY, Formatting.WHITE.getColorValue()
            );
            lineY += 10;
        }

        if (totalsByItem.isEmpty()) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    "Keine komprimierten Blöcke erkannt",
                    x + 8, lineY, Formatting.GRAY.getColorValue()
            );
        }
    }

    private static void addStack(
            Map<Item, Long> totals,
            ItemStack stack,
            MinecraftClient client
    ) {
        if (stack == null || stack.isEmpty()) return;

        long amount = DKCounterClient.readCompressedAmount(stack, client);
        if (amount <= 0) return;

        totals.merge(stack.getItem(), amount, (a, b) -> {
            try {
                return Math.addExact(a, b);
            } catch (ArithmeticException ignored) {
                return Long.MAX_VALUE;
            }
        });
    }
}
