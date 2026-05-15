package mods.flammpfeil.slashblade.compat.jei.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * @author yiran1457
 */
public record SpecialBladeDescData(ResourceLocation bladeName, String description) {
    public static final Codec<SpecialBladeDescData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("bladeName").forGetter(SpecialBladeDescData::bladeName),
        Codec.STRING.fieldOf("description").forGetter(SpecialBladeDescData::description)
    ).apply(instance, SpecialBladeDescData::new));
}
