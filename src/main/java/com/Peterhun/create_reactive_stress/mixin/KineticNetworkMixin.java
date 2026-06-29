package com.Peterhun.create_reactive_stress.mixin;

import com.Peterhun.create_reactive_stress.FlywheelMomentumManager;
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

    @Shadow
    public abstract void updateStress();

    /**
     * Inject into updateNetwork to check for overstress conditions
     * and attempt momentum extraction before marking as overstressed.
     */
    @Inject(method = "updateNetwork", at = @At("HEAD"), cancellable = true)
    private void onUpdateNetwork(CallbackInfo ci) {
        // Calculate stress deficit
        float deficit = currentStress - currentCapacity;

        // If we're approaching overstress, try momentum extraction
        if (deficit > 0.1f) {
            KineticNetwork self = (KineticNetwork) (Object) this;
            
            // Attempt to extract momentum from flywheels
            if (FlywheelMomentumManager.attemptMomentumExtraction(self, deficit)) {
                // Momentum was extracted successfully, update stress again
                updateStress();
                // Continue normal execution
                return;
            }
            // If momentum extraction failed, continue to normal overstress handling
        }
    }
}
