package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
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
            if (!enabled || client.player == null || client.currentScreen == null) return;

            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                boolean isRightClicking = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
                
                if (isRightClicking && !rightClickPressed) {
                    for (Slot slot : client.player.currentScreenHandler.slots) {
                        if (isMouseOverSlot(client, slot) && slot.getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                            client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 
                                slot.id, 0, SlotActionType.SWAP, client.player);
                            break;
                        }
                    }
                }
                rightClickPressed = isRightClicking;
            }
        });
    }

    private boolean isMouseOverSlot(MinecraftClient client, Slot slot) {
        double mouseX = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
        
        return mouseX >= (double)slot.x && mouseX <= (double)slot.x + 16 && 
               mouseY >= (double)slot.y && mouseY <= (double)slot.y + 16;
    }
}
