
package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class HiddenModClient implements ClientModInitializer {
    public static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        // Command to toggle: /fpsboost
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost").executes(context -> {
                enabled = !enabled;
                context.getSource().sendFeedback(Text.literal("§7[Ghost] Status: " + (enabled ? "§aON" : "§cOFF")));
                return 1;
            }));
        });

        // Totem Management Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.player == null || client.currentScreen == null) return;

            // Agar inventory open hai
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // Agar mouse totem ke upar hai
                if (client.player.getInventory().getCursorStack().isOf(Items.TOTEM_OF_UNDYING)) {
                    // Logic: Offhand mein move karo
                    client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 45, 0, SlotActionType.SWAP, client.player);
                }
            }
        });
    }
}
