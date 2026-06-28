package com.fpsboost;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FpsBoostMod implements ModInitializer {
    public static final String MOD_ID = "fpsboost";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FPS Boost Initialized!");
    }
}
