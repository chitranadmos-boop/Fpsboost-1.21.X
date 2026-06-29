package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class HiddenModClient implements ClientModInitializer {
    public static boolean hitEnabled = false;
    private static int cooldown = 0;

    @Override
    public void onInitializeClient() {
        // Register /fpsboost hit command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost")
                .then(ClientCommandManager.literal("hit")
                    .executes(c -> {
                        hitEnabled = !hitEnabled;
                        c.getSource().sendFeedback(Text.literal("Hit feature set to: " + hitEnabled));
                        return 1;
                    }))
            );
        });

        // Tick logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            if (cooldown > 0) {
                cooldown--;
            } else if (hitEnabled && client.options.attackKey.isPressed()) {
                // Only act if holding a sword
                if (client.player.getMainHandStack().getItem() instanceof SwordItem) {
                    HitResult hit = client.crosshairTarget;
                    
                    if (hit instanceof BlockHitResult) {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        var pos = blockHit.getBlockPos();
                        var state = client.world.getBlockState(pos);

                        // 1. Crystal on Bedrock/Obsidian, Obsidian on others
                        if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK)) {
                            if (client.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                            }
                        } else {
                            if (client.player.getMainHandStack().isOf(Items.OBSIDIAN)) {
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                            }
                        }

                        // 2. Auto-attack nearby crystals
                        for (Entity entity : client.world.getEntities()) {
                            if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 16.0) {
                                client.interactionManager.attackEntity(client.player, entity);
                                client.player.swingHand(Hand.MAIN_HAND);
                                cooldown = 5; // Apply cooldown after action
                            }
                        }
                    }
                }
            }
        });
    }
}

