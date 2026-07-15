package mods.flammpfeil.slashblade.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Map;

public class BladeRegisterManager {
    public static Map<ResourceLocation, ItemStack> CACHE = new Object2ObjectOpenHashMap<>();
    
    public static ItemStack getBlade(ResourceLocation resourceLocation) {
        return CACHE.get(resourceLocation);
    }
    
    public static Collection<ItemStack> getAllBlades() {
        return CACHE.values();
    }
    
    public static void build() {
        CACHE.clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            SlashBlade.getSlashBladeDefinitionRegistry(level)
                .entrySet()
                .forEach(entry ->
                    CACHE.put(entry.getKey().location(), entry.getValue().getBlade(level.registryAccess())));
        }
    }
}
