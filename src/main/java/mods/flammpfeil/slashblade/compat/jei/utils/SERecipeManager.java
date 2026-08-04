package mods.flammpfeil.slashblade.compat.jei.utils;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yiran1457
 */
public class SERecipeManager implements ISimpleRecipeManagerPlugin<SpecialEffect> {
    public static final SERecipeManager INSTANCE = new SERecipeManager();
    
    @Override
    public boolean isHandledInput(ITypedIngredient<?> iTypedIngredient) {
        return iTypedIngredient.getItemStack().filter(stack -> BladeStateAccess.of(stack).isPresent()).isPresent();
    }
    
    @Override
    public boolean isHandledOutput(ITypedIngredient<?> iTypedIngredient) {
        return false;
    }
    
    @Override
    public List<SpecialEffect> getRecipesForInput(ITypedIngredient<?> iTypedIngredient) {
        return iTypedIngredient.getItemStack()
            .map(BladeStateAccess::of)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ISlashBladeState::getSpecialEffects)
            .orElse(new ArrayList<>())
            .stream()
            .map(SpecialEffectsRegistry.REGISTRY::get)
            .filter(Objects::nonNull)
            .toList();
    }
    
    @Override
    public List<SpecialEffect> getRecipesForOutput(ITypedIngredient<?> iTypedIngredient) {
        return List.of();
    }
    
    @Override
    public List<SpecialEffect> getAllRecipes() {
        return SpecialEffectsRegistry.REGISTRY.stream().toList();
    }
}
