package com.Peterhun.create_reactive_stress;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

import static com.Peterhun.create_reactive_stress.CRSManager.getKeyList;
import static com.Peterhun.create_reactive_stress.CRSManager.getMultiplierList;


public final class Config {

    public static final ModConfigSpec SPEC;
    public static final Map<String, ModConfigSpec.ConfigValue<Double>> MULTIPLIERS = new HashMap<>();
    
    // Momentum extraction configuration
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_MOMENTUM_EXTRACTION;
    public static final ModConfigSpec.ConfigValue<Double> MOMENTUM_DRAIN_RATE;
    public static final ModConfigSpec.ConfigValue<Double> SPEED_THRESHOLD;
    public static final ModConfigSpec.ConfigValue<Double> STRESS_DEFICIT_THRESHOLD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("reactive_stress");

        addMultiplier(builder, "Press", 2.5);
        addMultiplier(builder, "Mixer", 2.0);
        addMultiplier(builder, "MillStone", 2.0);
        addMultiplier(builder, "Saw", 2.0);
        addMultiplier(builder, "CrushingWheel", 3);

        //API implementation
        if (!(getKeyList().isEmpty())) {
            for (KineticBlockEntity key : getKeyList()) {
                addMultiplier(builder, key.toString(), getMultiplierList(key));
            }
        }

        builder.pop();
        
        // Momentum extraction settings
        builder.push("flywheel_momentum");
        
        ENABLE_MOMENTUM_EXTRACTION = builder
                .comment("Enable momentum extraction from flywheels to prevent network overstress")
                .define("enableMomentumExtraction", true);
        
        MOMENTUM_DRAIN_RATE = builder
                .comment("Percentage of flywheel speed to drain per tick (0.0 - 1.0)")
                .defineInRange("momentumDrainRate", 0.02, 0.001, 0.1);
        
        SPEED_THRESHOLD = builder
                .comment("Minimum rotation speed threshold before flywheel stops contributing momentum")
                .defineInRange("speedThreshold", 0.1, 0.01, 1.0);
        
        STRESS_DEFICIT_THRESHOLD = builder
                .comment("Stress deficit threshold (in stress units) before attempting momentum extraction")
                .defineInRange("stressDeficitThreshold", 0.1, 0.01, 10.0);
        
        builder.pop();

        SPEC = builder.build();
    }

    private static void addMultiplier(ModConfigSpec.Builder builder, String key, double defaultValue) {
        ModConfigSpec.ConfigValue<Double> value =
                builder.comment("Multiplier for " + key + " stress calculations")
                        .defineInRange(key, defaultValue, 1.0, 100.0);

        MULTIPLIERS.put(key, value);
    }
}
