package com.Peterhun.create_reactive_stress.mixin;

import com.Peterhun.create_reactive_stress.FlywheelMomentumManager;
import com.Peterhun.create_reactive_stress.UtilityHelperClass;
import com.simibubi.create.content.kinetics.KineticNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for KineticNetwork to intercept stress updates and handle overstress
 * by extracting momentum from flywheels before triggering global overstress.
 */
@Mixin(KineticNetwork.class)
public abstract class KineticNetworkMixin {

    @Shadow
    private float currentCapacity;

    @Shadow
    private float currentStress;

    /**
     * Inject into updateNetwork to check for overstress conditions
     * and attempt momentum extraction before marking as overstressed.
     */
    @Inject(method = "updateNetwork", at = @At("HEAD"), cancellable = true)
    private void onUpdateNetwork(CallbackInfo ci) {
        // Only process if momentum extraction is enabled
        if (!UtilityHelperClass.createReactiveStress$enableMomentumExtraction) {
            return;
        }

        // Calculate stress deficit (how much over capacity we are)
        float deficit = currentStress - currentCapacity;

        // If we're approaching or exceeding capacity, try momentum extraction
        if (deficit > UtilityHelperClass.createReactiveStress$stressDeficitThreshold) {
            KineticNetwork self = (KineticNetwork) (Object) this;
            
            // Attempt to extract momentum from flywheels
            boolean extracted = FlywheelMomentumManager.attemptMomentumExtraction(self, deficit);
            
            if (extracted) {
                UtilityHelperClass.LOGGER.debug(
                        "Momentum extraction successful for network {}. Deficit: {} stress units",
                        self.id, deficit
                );
            } else {
                UtilityHelperClass.LOGGER.debug(
                        "Momentum extraction failed for network {}. Overstress will occur. Deficit: {} stress units",
                        self.id, deficit
                );
            }
            // Continue to normal stress update logic
        }
    }
}
