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
    private static int delay = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost").executes(context -> {
                enabled = !enabled;
                context.getSource().sendFeedback(Text.literal("§7[Ghost] Status: " + (enabled ? "§aON" : "§cOFF")));
                return 1;
            }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.player == null || client.world == null) return;

            // 1. Auto-Hit Crystal (Fixed logic)
            for (Entity e : client.world.getEntities()) {
                if (e instanceof EndCrystalEntity && client.player.squaredDistanceTo(e) <= 12) {
                    client.interactionManager.attackEntity(client.player, e);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }

            // 2. Fixed Totem Swap (Inventory check with delay)
            if (delay > 0) { delay--; return; }

            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // Check agar cursor mein totem hai AND offhand khaali hai ya sword hai
                if (client.player.currentScreenHandler.getCursorStack().isOf(Items.TOTEM_OF_UNDYING)) {
                    if (client.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 45, 0, SlotActionType.SWAP, client.player);
                        delay = 20; // 1 second ka delay taaki glitch na ho
                    }
                }
            }
        });
    }
}
