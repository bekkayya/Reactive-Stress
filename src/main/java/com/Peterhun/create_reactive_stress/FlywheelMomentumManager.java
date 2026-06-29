package com.Peterhun.create_reactive_stress;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages momentum extraction from flywheels in a kinetic network.
 * When a network is overloaded, flywheels gradually slow down to provide energy,
 * preventing immediate overstress failure.
 */
public class FlywheelMomentumManager {
    private static final Logger LOGGER = LogManager.getLogger("Create_Reactive_Stress");

    /**
     * Attempts to extract momentum from flywheels in the network to cover overstress.
     * Returns true if enough momentum was extracted, false if overstress is unavoidable.
     *
     * @param network The kinetic network to check
     * @param stressDeficit The amount of stress that needs to be covered (current - capacity)
     * @return true if momentum extraction can handle the deficit, false if overstress occurs
     */
    public static boolean attemptMomentumExtraction(KineticNetwork network, float stressDeficit) {
        // Check if momentum extraction is enabled
        if (!UtilityHelperClass.createReactiveStress$enableMomentumExtraction) {
            return false;
        }

        if (stressDeficit <= UtilityHelperClass.createReactiveStress$stressDeficitThreshold) {
            return true; // No significant deficit, all good
        }

        List<FlywheelBlockEntity> flywheels = findFlywheelsInNetwork(network);

        if (flywheels.isEmpty()) {
            return false; // No flywheels available, overstress will occur
        }

        // Calculate total available momentum
        float totalMomentumAvailable = calculateTotalMomentum(flywheels);

        if (totalMomentumAvailable < stressDeficit) {
            return false; // Not enough momentum available
        }

        // Extract momentum from the fastest flywheel
        extractMomentumFromFastest(flywheels, stressDeficit);
        return true;
    }

    /**
     * Finds all flywheels connected to this network using BFS traversal.
     */
    private static List<FlywheelBlockEntity> findFlywheelsInNetwork(KineticNetwork network) {
        List<FlywheelBlockEntity> flywheels = new ArrayList<>();

        if (network == null || network.members == null || network.members.isEmpty()) {
            return flywheels;
        }

        Set<KineticBlockEntity> visited = new HashSet<>();
        Queue<KineticBlockEntity> queue = new LinkedList<>(network.members.keySet());

        while (!queue.isEmpty()) {
            KineticBlockEntity current = queue.poll();

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            if (current instanceof FlywheelBlockEntity flywheel) {
                flywheels.add(flywheel);
            }

            // In Create, network members are already the connected entities
            // Additional traversal would use RotationPropagator, but network.members is sufficient
        }

        return flywheels;
    }

    /**
     * Calculates total available momentum from all flywheels.
     * Momentum is based on current rotational speed.
     */
    private static float calculateTotalMomentum(List<FlywheelBlockEntity> flywheels) {
        return (float) flywheels.stream()
                .filter(fw -> fw.getLevel() != null && !fw.getLevel().isClientSide())
                .mapToDouble(fw -> Math.abs(fw.getTheoreticalSpeed()))
                .sum();
    }

    /**
     * Extracts momentum from the fastest flywheel by gradually slowing it down.
     */
    private static void extractMomentumFromFastest(List<FlywheelBlockEntity> flywheels, float stressDeficit) {
        if (flywheels.isEmpty()) {
            return;
        }

        // Find the fastest flywheel as the primary extraction source
        FlywheelBlockEntity primaryFlywheel = flywheels.stream()
                .filter(fw -> fw.getLevel() != null && !fw.getLevel().isClientSide())
                .max(Comparator.comparingDouble(fw -> Math.abs(fw.getTheoreticalSpeed())))
                .orElse(null);

        if (primaryFlywheel == null) {
            return;
        }

        float currentSpeed = primaryFlywheel.getTheoreticalSpeed();
        
        // Calculate drain amount based on configured drain rate
        float drainAmount = (float) (Math.abs(currentSpeed) * UtilityHelperClass.createReactiveStress$momentumDrainRate);
        drainAmount = Math.min(drainAmount, stressDeficit / 100f); // Conservative extraction

        float newSpeed = currentSpeed > 0 ? currentSpeed - drainAmount : currentSpeed + drainAmount;

        // Clamp to minimum threshold
        if (Math.abs(newSpeed) < UtilityHelperClass.createReactiveStress$speedThreshold) {
            newSpeed = 0;
        }

        primaryFlywheel.setSpeed(newSpeed);
        primaryFlywheel.sendData();

        LOGGER.debug("Extracted momentum from flywheel at {}. Speed: {} -> {}",
                primaryFlywheel.getBlockPos(), currentSpeed, newSpeed);
    }

    /**
     * Checks if there's sufficient momentum in the network to prevent overstress.
     */
    public static float getAvailableMomentum(KineticNetwork network) {
        if (network == null) {
            return 0;
        }

        List<FlywheelBlockEntity> flywheels = findFlywheelsInNetwork(network);
        return calculateTotalMomentum(flywheels);
    }

    /**
     * Resets flywheel speed degradation (called when stress returns to normal).
     */
    public static void resetMomentumExtraction(KineticNetwork network) {
        if (network == null) {
            return;
        }

        List<FlywheelBlockEntity> flywheels = findFlywheelsInNetwork(network);
        // Flywheels will naturally recover through sources in the network
        LOGGER.debug("Momentum extraction reset for network {}", network.id);
    }
}
