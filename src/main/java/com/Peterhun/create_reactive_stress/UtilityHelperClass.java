package com.Peterhun.create_reactive_stress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import static com.Peterhun.create_reactive_stress.Config.*;

public abstract class UtilityHelperClass{
    @Unique
    public static void createReactiveStress$CachedCall(){
        createReactiveStress$cachedValue = false;
    }

    public static boolean createReactiveStress$cachedValue = false;

    @Unique
    public static double createReactiveStress$valuePress = 1f;
    @Unique
    public static double createReactiveStress$valueMixer = 1f;
    @Unique
    public static double createReactiveStress$valueMillstone = 1f;
    @Unique
    public static double createReactiveStress$valueSaw = 1f;
    @Unique
    public static double createReactiveStress$valueCrushingWheel = 1f;

    // Momentum extraction config caches
    @Unique
    public static boolean createReactiveStress$enableMomentumExtraction = true;
    @Unique
    public static double createReactiveStress$momentumDrainRate = 0.02;
    @Unique
    public static double createReactiveStress$speedThreshold = 0.1;
    @Unique
    public static double createReactiveStress$stressDeficitThreshold = 0.1;

    public static final Logger LOGGER = LogManager.getLogger("Create_Reactive_Stress");




    @Unique
    public static void createReactiveStress$LoadConfig(){
        //Optimization cached multipliers
        if(createReactiveStress$cachedValue) return;

        createReactiveStress$valuePress = MULTIPLIERS.get("Press").get();
        createReactiveStress$valueMixer = MULTIPLIERS.get("Mixer").get();
        createReactiveStress$valueMillstone = MULTIPLIERS.get("MillStone").get();
        createReactiveStress$valueSaw = MULTIPLIERS.get("Saw").get();
        createReactiveStress$valueCrushingWheel = MULTIPLIERS.get("CrushingWheel").get();
        
        // Load momentum extraction config values
        createReactiveStress$enableMomentumExtraction = ENABLE_MOMENTUM_EXTRACTION.get();
        createReactiveStress$momentumDrainRate = MOMENTUM_DRAIN_RATE.get();
        createReactiveStress$speedThreshold = SPEED_THRESHOLD.get();
        createReactiveStress$stressDeficitThreshold = STRESS_DEFICIT_THRESHOLD.get();
        
        createReactiveStress$cachedValue = true;
        LOGGER.info("Config for Create_Reactive_Stress has been loaded");
    }

}
