package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.util.EnchantmentsHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SlashBladeShapedRecipe extends ShapedRecipe {
    
    public static final RecipeSerializer<SlashBladeShapedRecipe> SERIALIZER =
        new SlashBladeShapedRecipeSerializer();
    
    private final ShapedRecipePattern pattern;
    private final ItemStack resultStack;
    @Nullable
    private final ResourceLocation outputBlade;
    
    public SlashBladeShapedRecipe(String group, CraftingBookCategory category,
                                  ShapedRecipePattern pattern, ItemStack result,
                                  Optional<ResourceLocation> outputBlade) {
        super(group, category, pattern, result);
        this.pattern = pattern;
        this.resultStack = result;
        this.outputBlade = outputBlade.orElse(null);
    }
    
    public ShapedRecipePattern getPattern() {
        return pattern;
    }
    
    public ItemStack getResultStack() {
        return resultStack;
    }
    
    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = BuiltInRegistries.ITEM.containsKey(outputBlade) ?
            BuiltInRegistries.ITEM.get(outputBlade)
            : SlashBladeItems.SLASHBLADE.get();
        
        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }
    
    @Nullable
    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }
    
    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY,
            Objects.requireNonNull(outputBlade, "outputBlade must not be null for getOutputBladeKey"));
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        ResourceLocation blade = this.getOutputBlade();
        
        if (blade == null) {
            return this.getResultStack().copy();
        }
        
        ItemStack result = SlashBladeShapedRecipe.getResultBlade(blade);
        var key = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (!blade.equals(key)) {
            result = access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY).getOrThrow(getOutputBladeKey())
                .value()
                .getBlade(access);
        }
        
        return result;
    }
    
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider access) {
        var result = this.getResultItem(access).copy();
        
        var resultState = BladeStateAccess.of(result).orElseThrow();
        boolean sumRefine = SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get();
        int proudSoul = resultState.getProudSoulCount();
        int killCount = resultState.getKillCount();
        int refine = resultState.getRefine();
        for (var stack : input.items()) {
            if (!(stack.getItem() instanceof ItemSlashBlade)) {
                continue;
            }
            var ingredientState = BladeStateAccess.of(stack).orElseThrow();
            
            proudSoul += ingredientState.getProudSoulCount();
            killCount += ingredientState.getKillCount();
            if (sumRefine) {
                refine += ingredientState.getRefine();
            } else {
                refine = Math.max(refine, ingredientState.getRefine());
            }
            EnchantmentsHelper.updateEnchantment(result, stack, access);
        }
        resultState.setProudSoulCount(proudSoul);
        resultState.setKillCount(killCount);
        resultState.setRefine(refine);
        ItemSlashBlade.updateRarity(result);
        
        return result;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
    
}
