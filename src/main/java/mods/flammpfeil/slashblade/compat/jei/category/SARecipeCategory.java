package mods.flammpfeil.slashblade.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.compat.jei.ingredient.SAIngredient;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import mods.flammpfeil.slashblade.util.BladeRegisterManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yiran1457
 */
public record SARecipeCategory(IDrawable icon) implements IRecipeCategory<SlashArts> {
    public static final RecipeType<SlashArts> SA_TYPE = RecipeType.create(SlashBlade.MODID, "slash_arts", SlashArts.class);
    
    public SARecipeCategory(IGuiHelper icon) {
        this(icon.createDrawableItemLike(SlashBladeItems.PROUDSOUL_SPHERE.get()));
    }
    
    @Override
    public @NotNull RecipeType<SlashArts> getRecipeType() {
        return SA_TYPE;
    }
    
    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.slashblade.slash_arts");
    }
    
    @Override
    public int getWidth() {
        return 128;
    }
    
    @Override
    public int getHeight() {
        return 100;
    }
    
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull SlashArts slashArts, @NotNull IFocusGroup iFocusGroup) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
            .addIngredient(SAIngredient.INSTANCE, slashArts);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(SAIngredient.INSTANCE, slashArts);
        List<ItemStack> items = BladeRegisterManager.getAllBlades().stream()
            .filter(stack -> BladeStateAccess.of(stack).map(state -> state.getSlashArts().equals(slashArts)).orElse(false))
            .toList();
        if (!items.isEmpty()) {
            builder.addInputSlot(5, 2)
                .setStandardSlotBackground()
                .addItemStacks(items);
        }
        
    }
    
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SlashArts recipe, @NotNull IFocusGroup focuses) {
        builder.addText(recipe.getDescription(), getWidth() - 20, 10)
            .setPosition(25, 7);
        builder.addScrollBoxWidget(getWidth() - 10, getHeight() - 30, 5, 25)
            .setContents(List.of(Component.translatable(recipe.getDescriptionId() + ".desc")));
    }
    
    @Override
    public ResourceLocation getRegistryName(@NotNull SlashArts recipe) {
        ResourceLocation key = SlashArtsRegistry.REGISTRY.getKey(recipe);
        if (key == null) {
            throw new NullPointerException("Key of SlashArts " + recipe + " is null!");
        }
        return key;
    }
}
