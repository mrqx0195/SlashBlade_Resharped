package mods.flammpfeil.slashblade.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.compat.jei.ingredient.SEIngredient;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.BladeRegisterManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author yiran1457
 */
public record SERecipeCategory(IDrawable icon) implements IRecipeCategory<SpecialEffect> {
    public static final RecipeType<SpecialEffect> SE_TYPE = RecipeType.create(SlashBlade.MODID, "special_effect", SpecialEffect.class);
    
    public SERecipeCategory(IGuiHelper icon) {
        this(icon.createDrawableItemLike(SlashBladeItems.PROUDSOUL_CRYSTAL.get()));
    }
    
    @Override
    public RecipeType<SpecialEffect> getRecipeType() {
        return SE_TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.slashblade.special_effect");
    }
    
    @Override
    public int getWidth() {
        return 176;
    }
    
    @Override
    public int getHeight() {
        return 120;
    }
    
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpecialEffect specialEffect, IFocusGroup iFocusGroup) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
            .addIngredient(SEIngredient.INSTANCE, specialEffect);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(SEIngredient.INSTANCE, specialEffect);
        List<ItemStack> items = BladeRegisterManager.getAllBlades().stream()
            .filter(stack -> BladeStateAccess.of(stack).map(state -> state.getSpecialEffects().contains(SpecialEffectsRegistry.REGISTRY.getKey(specialEffect))).orElse(false))
            .toList();
        if (!items.isEmpty()) {
            builder.addInputSlot(5, 2)
                .addItemStacks(items);
        }
    }
    
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SpecialEffect recipe, IFocusGroup focuses) {
        builder.addText(recipe.getDescription(), getWidth() - 20, 10)
            .setPosition(25, 7);
        builder.addText(
                Component.translatable("jei.category.slashblade.special_effect.copiable", getBooleanText(recipe.isCopiable()))
                    .append(Component.literal("   "))
                    .append(
                        Component.translatable("jei.category.slashblade.special_effect.removable", getBooleanText(recipe.isRemovable()))
                    ),
                getWidth(), 10
            )
            .setTextAlignment(HorizontalAlignment.CENTER)
            .setPosition(0, 22);
        builder.addScrollBoxWidget(getWidth() - 10, getHeight() - 40, 5, 35)
            .setContents(List.of(Component.translatable(recipe.getDescriptionId() + ".desc")));
    }
    
    public static Component getBooleanText(boolean value) {
        return value
            ? Component.translatable("jei.slashblade.true").withStyle(ChatFormatting.GREEN)
            : Component.translatable("jei.slashblade.false").withStyle(ChatFormatting.RED);
    }
    
    @Override
    public ResourceLocation getRegistryName(SpecialEffect recipe) {
        ResourceLocation key = SpecialEffectsRegistry.REGISTRY.getKey(recipe);
        if (key == null) {
            throw new NullPointerException("Key of SpecialEffect " + recipe + " is null!");
        }
        return key;
    }
    
}
