package mods.flammpfeil.slashblade.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.compat.jei.data.SpecialBladeDescData;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.util.BladeRegisterManager;
import net.minecraft.client.Minecraft; // 【新增】用于获取字体渲染器
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author yiran1457
 */
public record SpecialBladeDescRecipeCategory(IDrawable icon) implements IRecipeCategory<SpecialBladeDescData> {
    public static final RecipeType<SpecialBladeDescData> DESC_TYPE = RecipeType.create(SlashBlade.MODID, "special_blade_desc_data", SpecialBladeDescData.class);

    public SpecialBladeDescRecipeCategory(IGuiHelper icon) {
        this(icon.createDrawableItemLike(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get()));
    }

    @Override
    public RecipeType<SpecialBladeDescData> getRecipeType() {
        return DESC_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.slashblade.special_blade_desc_data");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SpecialBladeDescData specialBladeDescData, IFocusGroup iFocusGroup) {
        builder.addOutputSlot(5, 2)
                        .setStandardSlotBackground()
                        .addItemStack(BladeRegisterManager.getBlade(specialBladeDescData.bladeName()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SpecialBladeDescData recipe, IFocusGroup focuses) {
        ItemStack blade = BladeRegisterManager.getBlade(recipe.bladeName());

        builder.addText(((MutableComponent) blade.getHoverName()).withStyle(blade.getRarity().getStyleModifier()), getWidth() - 42, 10)
                        .setTextAlignment(HorizontalAlignment.CENTER)
                        .setPosition(21, 7);

        Component desc = Component.translatable(recipe.description());
        int boxWidth = getWidth() - 10;
        int boxHeight = getHeight() - 40;

        int textHeight = Minecraft.getInstance().font.wordWrapHeight(desc, boxWidth);

        if (textHeight > boxHeight) {
            builder.addScrollBoxWidget(boxWidth, boxHeight, 5, 35)
                            .setContents(List.of(desc));
        } else {
            builder.addText(desc, boxWidth, boxHeight)
                            .setPosition(5, 35);
        }
    }
}
