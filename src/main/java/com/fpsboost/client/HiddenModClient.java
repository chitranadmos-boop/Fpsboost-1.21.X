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
    public static boolean placeEnabled = false;
    public static boolean hitEnabled = false;
    private boolean lastIsAttackPressed = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost")
                .then(ClientCommandManager.literal("place").executes(context -> {
                    placeEnabled = !placeEnabled;
                    return 1;
                }))
                .then(ClientCommandManager.literal("hit").executes(context -> {
                    hitEnabled = !hitEnabled;
                    return 1;
                }))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 1. AUTO-HIT (Priority High: Daba kar rakhne pe har tick pe check karega)
            if (hitEnabled && client.options.attackKey.isPressed()) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof EndCrystalEntity && client.player.squaredDistanceTo(e) <= 16) {
                        client.interactionManager.attackEntity(client.player, e);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }

            // 2. PLACEMENT (Priority Medium: Sirf tabhi jab click release ho ya naya click ho)
            if (placeEnabled && client.interactionManager != null) {
                boolean isAttackPressed = client.options.attackKey.isPressed();
                
                // Agar sword haath mein hai aur click daba rahe ho
                if (client.player.getMainHandStack().getItem() instanceof SwordItem && isAttackPressed && !lastIsAttackPressed) {
                    HitResult hit = client.crosshairTarget;
                    if (hit instanceof BlockHitResult bhr) {
                        BlockPos targetPos = bhr.getBlockPos();
                        BlockPos abovePos = targetPos.up();
                        
                        // Check: Agar upar kuch nahi hai, tabhi place karo
                        if (client.world.getBlockState(abovePos).getBlock() == Blocks.AIR) {
                            int obsSlot = findItem(client, Items.OBSIDIAN);
                            int crySlot = findItem(client, Items.END_CRYSTAL);
                            
                            if (obsSlot != -1 && crySlot != -1) {
                                int oldSlot = client.player.getInventory().selectedSlot;
                                
                                // Action: Obsidian + Crystal
                                client.player.getInventory().selectedSlot = obsSlot;
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                                
                                client.player.getInventory().selectedSlot = crySlot;
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, new BlockHitResult(bhr.getPos().add(0, 1, 0), Direction.UP, abovePos, false));
                                
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
