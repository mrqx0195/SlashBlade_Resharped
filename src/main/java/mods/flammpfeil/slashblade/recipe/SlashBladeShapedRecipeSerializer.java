package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SlashBladeShapedRecipeSerializer() implements RecipeSerializer<SlashBladeShapedRecipe> {
    
    public static final MapCodec<SlashBladeShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
        CraftingBookCategory.CODEC.fieldOf("category").forGetter(ShapedRecipe::category),
        ShapedRecipePattern.MAP_CODEC.forGetter(SlashBladeShapedRecipe::getPattern),
        ItemStack.CODEC.fieldOf("result").forGetter(SlashBladeShapedRecipe::getResultStack),
        ResourceLocation.CODEC.optionalFieldOf("blade").forGetter(r -> Optional.ofNullable(r.getOutputBlade()))
    ).apply(inst, SlashBladeShapedRecipe::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SlashBladeShapedRecipe> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    
    @Override
    public @NotNull MapCodec<SlashBladeShapedRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, SlashBladeShapedRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
