package mods.flammpfeil.slashblade.compat.jei.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author yiran1457
 */
public class ShowEntityListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<HolderSet<EntityType<?>>, ShowEntityData> SHOW_DATA = new Object2ObjectOpenHashMap<>();
    
    public ShowEntityListener() {
        super(GSON, "show_entity");
    }
    
    public static @Nullable ShowEntityData getShowData(EntityType<?> entityType) {
        return SHOW_DATA.get(HolderSet.direct(Holder.direct(entityType)));
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        SHOW_DATA.clear();
        for (JsonElement value : map.values()) {
            ShowEntityData.CODEC.parse(JsonOps.INSTANCE, value).result().ifPresent(specialBladeDescData -> SHOW_DATA.put(specialBladeDescData.entityType(), specialBladeDescData));
        }
    }
}
