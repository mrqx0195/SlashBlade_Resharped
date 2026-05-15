package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
        .create(BuiltInRegistries.RECIPE_TYPE, SlashBlade.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
        .create(BuiltInRegistries.RECIPE_SERIALIZER, SlashBlade.MODID);
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister
        .create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, SlashBlade.MODID);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SLASHBLADE_SHAPED = RECIPE_SERIALIZER
        .register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> PROUDSOUL_RECIPE = RECIPE_SERIALIZER
        .register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SLASHBLADE_SMITHING = RECIPE_SERIALIZER
        .register("slashblade_smithing", () -> SlashBladeSmithingRecipe.SERIALIZER);
    
    public static final DeferredHolder<IngredientType<?>, IngredientType<SlashBladeIngredient>> SLASHBLADE_INGREDIENT =
        INGREDIENT_TYPES.register("blade",
            () -> new IngredientType<>(SlashBladeIngredient.CODEC, SlashBladeIngredient.STREAM_CODEC));
    
    static {
        SlashBladeIngredient.TYPE = SLASHBLADE_INGREDIENT;
    }
}
