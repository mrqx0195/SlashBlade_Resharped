package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.Executors;

import static mods.flammpfeil.slashblade.init.DefaultResources.ExMotionLocation;

/**
 * Created by Furia on 2016/02/06.
 */
public class BladeMotionManager {
    
    private static final class SingletonHolder {
        private static final BladeMotionManager instance = new BladeMotionManager();
    }
    
    public static BladeMotionManager getInstance() {
        return SingletonHolder.instance;
    }
    
    @Nullable
    MmdVmdMotionMc defaultMotion;
    
    LoadingCache<ResourceLocation, MmdVmdMotionMc> cache;
    
    private BladeMotionManager() {
        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException | MmdException e) {
            SlashBlade.LOGGER.warn(e);
        }
        
        cache = CacheBuilder.newBuilder()
            .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                @SuppressWarnings("DataFlowIssue")
                @Override
                public MmdVmdMotionMc load(ResourceLocation key) {
                    try {
                        return new MmdVmdMotionMc(key);
                    } catch (Exception e) {
                        SlashBlade.LOGGER.warn(e);
                        return defaultMotion;
                    }
                }
                
            }, Executors.newCachedThreadPool()));
    }
    
    public void reload() {
        cache.invalidateAll();
        
        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException | MmdException e) {
            SlashBlade.LOGGER.warn(e);
        }
    }
    
    @Nullable
    public MmdVmdMotionMc getMotion(@Nullable ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                SlashBlade.LOGGER.warn(e);
            }
        }
        return defaultMotion;
    }
    
}
