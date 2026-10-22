package de.originalpvp.features;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry and lifecycle dispatcher for all {@link IFeature} modules.
 */
public class FeatureManager {

    private final Map<Class<? extends IFeature>, IFeature> featuresByType = new LinkedHashMap<Class<? extends IFeature>, IFeature>();
    private final List<IFeature> features = new ArrayList<IFeature>();

    /**
     * Registers a feature module.
     */
    public <T extends IFeature> T register(T feature) {
        featuresByType.put(feature.getClass(), feature);
        features.add(feature);
        return feature;
    }

    /**
     * Retrieves a registered feature by its concrete class.
     */
    @SuppressWarnings("unchecked")
    public <T extends IFeature> T getFeature(Class<T> featureClass) {
        return (T) featuresByType.get(featureClass);
    }

    /**
     * @return an unmodifiable view of all registered features
     */
    public List<IFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    /**
     * Initializes all registered features.
     */
    public void initAll(Minecraft mc) {
        for (int i = 0; i < features.size(); i++) {
            features.get(i).onInit(mc);
        }
    }

    /**
     * Dispatches client tick to all registered features.
     */
    public void tickAll(Minecraft mc) {
        for (int i = 0; i < features.size(); i++) {
            features.get(i).onTick(mc);
        }
    }
}
