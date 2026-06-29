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
                .then(ClientCommandManager.literal("place").executes(c -> { placeEnabled = !placeEnabled; return 1; }))
                .then(ClientCommandManager.literal("hit").executes(c -> { hitEnabled = !hitEnabled; return 1; }))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 1. AUTO-HIT: Crystal milte hi turant attack
            if (hitEnabled && client.options.attackKey.isPressed()) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof EndCrystalEntity && client.player.squaredDistanceTo(e) <= 16) {
                        client.interactionManager.attackEntity(client.player, e);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }

            // 2. PLACEMENT: Sirf tabhi jab BlockTarget ho, AIR pe nahi
            if (placeEnabled && client.interactionManager != null) {
                boolean isAttack = client.options.attackKey.isPressed();
                if (client.player.getMainHandStack().getItem() instanceof SwordItem && isAttack && !lastIsAttackPressed) {
                    HitResult hit = client.crosshairTarget;
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
                        BlockPos targetPos = bhr.getBlockPos();
                        BlockPos abovePos = targetPos.up();
                        // SIRF AIR par place karega
                        if (client.world.getBlockState(abovePos).isAir()) {
                            int obs = findItem(client, Items.OBSIDIAN);
                            int cry = findItem(client, Items.END_CRYSTAL);
                            if (obs != -1 && cry != -1) {
                                int old = client.player.getInventory().selectedSlot;
                                client.player.getInventory().selectedSlot = obs;
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                                client.player.getInventory().selectedSlot = cry;
                                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, new BlockHitResult(bhr.getPos().toCenterPos(), Direction.UP, abovePos, false));
                                client.player.getInventory().selectedSlot = old;
                            }
                        }
                    }
                }
                lastIsAttackPressed = isAttack;
            }
        });
    }

    private int findItem(MinecraftClient client, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) if (client.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }
}
