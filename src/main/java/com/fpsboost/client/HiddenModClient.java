package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class HiddenModClient implements ClientModInitializer {
    private boolean placeEnabled = false;
    private boolean hitEnabled = false;
    private boolean lastIsAttackPressed = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost")
                .then(ClientCommandManager.literal("place").executes(context -> {
                    placeEnabled = !placeEnabled;
                    context.getSource().sendFeedback(net.minecraft.text.Text.literal("§7[Ghost] Placement: " + (placeEnabled ? "§aON" : "§cOFF")));
                    return 1;
                }))
                .then(ClientCommandManager.literal("hit").executes(context -> {
                    hitEnabled = !hitEnabled;
                    context.getSource().sendFeedback(net.minecraft.text.Text.literal("§7[Ghost] Auto-Hit: " + (hitEnabled ? "§aON" : "§cOFF")));
                    return 1;
                }))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 1-TICK AUTO-HIT (Ultra Fast)
            if (hitEnabled && client.options.attackKey.isPressed()) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof EndCrystalEntity && client.player.squaredDistanceTo(e) <= 16) {
                        client.interactionManager.attackEntity(client.player, e);
                        client.player.swingHand(Hand.MAIN_HAND);
                        // Hataya delay taaki 1-tick mein chale
                    }
                }
            }

            // PLACEMENT FIX
            if (placeEnabled && client.interactionManager != null) {
                ItemStack stack = client.player.getInventory().getMainHandStack();
                boolean isAttackPressed = client.options.attackKey.isPressed();

                if (stack.getItem() instanceof SwordItem && isAttackPressed && !lastIsAttackPressed) {
                    HitResult hit = client.crosshairTarget;
                    if (hit instanceof BlockHitResult bhr) {
                        BlockPos targetPos = bhr.getBlockPos();
                        BlockPos abovePos = targetPos.up();
                        
                        // Fix: Check agar upar wala block sacch mein AIR hai
                        if (client.world.getBlockState(abovePos).getBlock() == Blocks.AIR 
                            && !client.world.getBlockState(targetPos).isOf(Blocks.BEDROCK)
                            && !client.world.getBlockState(targetPos).isOf(Blocks.OBSIDIAN)) { // Obsidian ke upar nahi lagega
                            
                            int obsSlot = findItem(client, Items.OBSIDIAN);
                            int crySlot = findItem(client, Items.END_CRYSTAL);
                            
                            if (obsSlot != -1 && crySlot != -1) {
                                int oldSlot = client.player.getInventory().selectedSlot;
                                
                                client.player.getInventory().selectedSlot = obsSlot;
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                                
                                client.player.getInventory().selectedSlot = crySlot;
                                BlockHitResult cryHit = new BlockHitResult(bhr.getPos().add(0, 1, 0), Direction.UP, abovePos, false);
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, cryHit);
                                
                                client.player.getInventory().selectedSlot = oldSlot;
                            }
                        }
                    }
                }
                lastIsAttackPressed = isAttackPressed;
            }
        });
    }

    private int findItem(MinecraftClient client, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) if (client.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }
}
