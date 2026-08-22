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
import net.minecraft.client.Minecraft;
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
        return 132;
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
        builder.addText(recipe.getDescription().copy().withStyle(ChatFormatting.BOLD), getWidth() - 20, 10)
                        .setPosition(25, 7);

        Component levelText = recipe.getRequestLevel() == 0
                        ? Component.translatable("jei.category.slashblade.special_effect.no_request_level").withStyle(ChatFormatting.BOLD)
                        : Component.translatable("jei.category.slashblade.special_effect.request_level", recipe.getRequestLevel()).withStyle(ChatFormatting.BOLD);

        builder.addText(levelText, getWidth() - 10, 10)
                        .setTextAlignment(HorizontalAlignment.LEFT)
                        .setPosition(5, 23);

        Component copiableText = recipe.isCopiable()
                        ? Component.translatable("jei.category.slashblade.special_effect.copiable").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        : Component.translatable("jei.category.slashblade.special_effect.uncopiable").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        Component removableText = recipe.isRemovable()
                        ? Component.translatable("jei.category.slashblade.special_effect.removable").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        : Component.translatable("jei.category.slashblade.special_effect.unremovable").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        Component combinedText = copiableText.copy()
                        .append(Component.literal("   "))
                        .append(removableText);

        builder.addText(combinedText, getWidth() - 10, 10)
                        .setTextAlignment(HorizontalAlignment.LEFT)
                        .setPosition(5, 35);

        Component desc = Component.translatable(recipe.getDescriptionId() + ".desc");
        int boxWidth = getWidth() - 10;
        int boxHeight = getHeight() - 52;

        int textHeight = Minecraft.getInstance().font.wordWrapHeight(desc, boxWidth);

        if (textHeight > boxHeight) {
            builder.addScrollBoxWidget(boxWidth, boxHeight, 5, 47)
                            .setContents(List.of(desc));
        } else {
            builder.addText(desc, boxWidth, boxHeight)
                            .setPosition(5, 47);
        }
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
