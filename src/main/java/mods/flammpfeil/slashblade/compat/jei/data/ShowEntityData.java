package mods.flammpfeil.slashblade.compat.jei.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

/**
 * @author yiran1457
 */
public record ShowEntityData(HolderSet<EntityType<?>> entityType, int scale, float yOffset) {
    public static final Codec<ShowEntityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entityType").forGetter(ShowEntityData::entityType),
        Codec.INT.fieldOf("scale").forGetter(ShowEntityData::scale),
        Codec.FLOAT.optionalFieldOf("yOffset", 0f).forGetter(ShowEntityData::yOffset)
    ).apply(instance, ShowEntityData::new));
}
