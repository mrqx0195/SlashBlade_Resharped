package mods.flammpfeil.slashblade.compat.jei.utils;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.slasharts.SlashArts;

import java.util.List;
import java.util.Optional;

/**
 * @author yiran1457
 */
public class SARecipeManager implements ISimpleRecipeManagerPlugin<SlashArts> {
    public static final SARecipeManager INSTANCE = new SARecipeManager();
    
    @Override
    public boolean isHandledInput(ITypedIngredient<?> iTypedIngredient) {
        return iTypedIngredient.getItemStack().filter(stack -> BladeStateAccess.of(stack).isPresent()).isPresent();
    }
    
    @Override
    public boolean isHandledOutput(ITypedIngredient<?> iTypedIngredient) {
        return false;
    }
    
    @Override
    public List<SlashArts> getRecipesForInput(ITypedIngredient<?> iTypedIngredient) {
        SlashArts slashArts = iTypedIngredient.getItemStack()
            .map(BladeStateAccess::of)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ISlashBladeState::getSlashArts)
            .orElse(null);
        if (slashArts != null) {
            return List.of(slashArts);
        }
        return List.of();
    }
    
    @Override
    public List<SlashArts> getRecipesForOutput(ITypedIngredient<?> iTypedIngredient) {
        return List.of();
    }
    
    @Override
    public List<SlashArts> getAllRecipes() {
        return SlashArtsRegistry.REGISTRY.stream().toList();
    }
}
