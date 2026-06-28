package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
        // Chat Commands Setup
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost")
                .then(ClientCommandManager.literal("place")
                    .executes(context -> {
                        placeEnabled = !placeEnabled;
                        String status = placeEnabled ? "§aEnabled" : "§cDisabled";
                        context.getSource().sendFeedback(net.minecraft.text.Text.literal("§7[Ghost] §fSword-Placement: " + status));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("hit")
                    .executes(context -> {
                        hitEnabled = !hitEnabled;
                        String status = hitEnabled ? "§aEnabled" : "§cDisabled";
                        context.getSource().sendFeedback(net.minecraft.text.Text.literal("§7[Ghost] §fAuto-Hit Crystal: " + status));
                        return 1;
                    })
                )
            );
        });

        // Main Tick Loop
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                return;
            }

            // --- FEATURE 1: AUTO HIT CRYSTAL SYSTEM ---
            if (hitEnabled) {
                // Player ke paas ke sabhi crystals ko dhoondo aur attack karo
                for (Entity entity : client.world.getEntities()) {
                    if (entity instanceof EndCrystalEntity) {
                        // Agar crystal player ki range (3.5 blocks) mein hai
                        if (client.player.squaredDistanceTo(entity) <= 12.25) {
                            client.interactionManager.attackEntity(client.player, entity);
                            client.player.swingHand(Hand.MAIN_HAND);
                            break; // Ek tick mein ek hit kaafi hai bypass ke liye
                        }
                    }
                }
            }

            // --- FEATURE 2: SWORD AUTO-PLACE SYSTEM ---
            if (placeEnabled) {
                ItemStack mainHandStack = client.player.getInventory().getMainHandStack();
                boolean holdingSword = mainHandStack.getItem() instanceof SwordItem;
                boolean isAttackPressed = client.options.attackKey.isPressed();

                if (holdingSword && isAttackPressed && !lastIsAttackPressed) {
                    HitResult hit = client.crosshairTarget;
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        
                        int obsidianSlot = findItemInHotbar(client, Items.OBSIDIAN);
                        int crystalSlot = findItemInHotbar(client, Items.END_CRYSTAL);

                        if (obsidianSlot != -1 && crystalSlot != -1) {
                            int originalSlot = client.player.getInventory().selectedSlot;

                            // Place Obsidian
                            client.player.getInventory().selectedSlot = obsidianSlot;
                            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);

                            // Place Crystal on top
                            BlockPos placePos = blockHit.getBlockPos().offset(blockHit.getSide());
                            BlockHitResult crystalHit = new BlockHitResult(
                                blockHit.getPos().add(0, 1, 0),
                                Direction.UP,
                                placePos,
                                false
                            );
                            client.player.getInventory().selectedSlot = crystalSlot;
                            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, crystalHit);

                            // Switch back to Sword instantly
                            client.player.getInventory().selectedSlot = originalSlot;
                        }
                    }
                }
                lastIsAttackPressed = isAttackPressed;
            }
        });
    }

    private int findItemInHotbar(MinecraftClient client, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }
}
