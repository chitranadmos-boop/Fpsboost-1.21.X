package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HiddenModClient implements ClientModInitializer {
    public static boolean enabled = false;
    private boolean rightClickPressed = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost").executes(context -> {
                enabled = !enabled;
                context.getSource().sendFeedback(Text.literal("§7[Ghost] Status: " + (enabled ? "§aON" : "§cOFF")), false);
                return 1;
            }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.player == null) return;

            // Totem Swap Trigger: Jab Inventory khuli ho aur Mouse hover kar rahe ho
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // Check agar Mouse ka Right Click daba hai (totem ke upar)
                boolean isRightClicking = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
                
                if (isRightClicking && !rightClickPressed) {
                    // Current slot jahan mouse hai, check karo kya wahan Totem hai
                    if (client.currentScreen.getFocusedSlot() != null && 
                        client.currentScreen.getFocusedSlot().getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                        
                        // Swap command
                        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 
                            client.currentScreen.getFocusedSlot().id, 0, SlotActionType.SWAP, client.player);
                    }
                }
                rightClickPressed = isRightClicking;
            }
        });
    }
}
