package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class HiddenModClient implements ClientModInitializer {
    public static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost").executes(context -> {
                enabled = !enabled;
                // Correct 1.21.1 Feedback method
                context.getSource().sendFeedback(Text.literal("§7[Ghost] Status: " + (enabled ? "§aON" : "§cOFF")));
                return 1;
            }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.player == null || client.world == null) return;

            // Auto-Hit
            for (Entity e : client.world.getEntities()) {
                if (e instanceof EndCrystalEntity && client.player.squaredDistanceTo(e) <= 16) {
                    client.interactionManager.attackEntity(client.player, e);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }

            // Totem Swap (Using correct screenHandler method)
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                if (client.player.currentScreenHandler.getCursorStack().isOf(Items.TOTEM_OF_UNDYING)) {
                    client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 45, 0, SlotActionType.SWAP, client.player);
                }
            }
        });
    }
}
