package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Created by Furia on 2016/02/06.
 */
@OnlyIn(Dist.CLIENT)
public class BladeModelManager {
    
    private static final class SingletonHolder {
        private static final BladeModelManager instance = new BladeModelManager();
    }
    
    public static BladeModelManager getInstance() {
        return SingletonHolder.instance;
    }
    
    public static Registry<SlashBladeDefinition> getClientSlashBladeRegistry() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess()
            .registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
    
    public WavefrontObject defaultModel;
    
    public LoadingCache<ResourceLocation, WavefrontObject> cache;
    
    private BladeModelManager() {
        defaultModel = new WavefrontObject(DefaultResources.resourceDefaultModel);
        
        cache = CacheBuilder.newBuilder()
            .maximumSize(256)
            .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                @Override
                public WavefrontObject load(ResourceLocation key) {
                    try {
                        return new WavefrontObject(key);
                    } catch (Exception e) {
                        return defaultModel;
                    }
                }
                
            }, Executors.newFixedThreadPool(2)));
    }
    
    public WavefrontObject getModel(@Nullable ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn(e);
            }
        }
        return defaultModel;
    }
    
}
