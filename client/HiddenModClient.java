package com.fpsboost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class HiddenModClient implements ClientModInitializer {
    private boolean utilityEnabled = false;

    @Override
    public void onInitializeClient() {
        // Client tick toggle control
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (utilityEnabled && client.player != null && client.world != null) {
                handleUtility(client);
            }
        });

        // /fpsboost command setup
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("fpsboost")
                .executes(context -> {
                    utilityEnabled = !utilityEnabled;
                    String status = utilityEnabled ? "§aEnabled" : "§cDisabled";
                    context.getSource().sendFeedback(net.minecraft.text.Text.literal("Utility status is now " + status));
                    return 1;
                })
            );
        });
    }

    private void handleUtility(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos targetPos = blockHit.getBlockPos();
            BlockPos placePos = targetPos.offset(blockHit.getSide());

            if (client.player.getInventory().getMainHandStack().isOf(Items.OBSIDIAN)) {
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                
                BlockHitResult replacementHit = new BlockHitResult(
                    blockHit.getPos().add(0, 1, 0), 
                    Direction.UP, 
                    placePos, 
                    false
                );
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, replacementHit);
            }
        }
    }
}

