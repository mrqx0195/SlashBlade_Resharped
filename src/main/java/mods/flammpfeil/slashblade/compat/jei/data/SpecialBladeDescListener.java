package mods.flammpfeil.slashblade.compat.jei.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.Map;

/**
 * @author yiran1457
 */
public class SpecialBladeDescListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final List<SpecialBladeDescData> DESC_DATA = new ObjectArrayList<>();
    
    public SpecialBladeDescListener() {
        super(GSON, "blade_desc");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        DESC_DATA.clear();
        for (JsonElement value : map.values()) {
            SpecialBladeDescData.CODEC.parse(JsonOps.INSTANCE, value).result().ifPresent(DESC_DATA::add);
        }
    }
}
