package mods.flammpfeil.slashblade.compat.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author yiran1457
 */
public class SAIngredient implements IIngredientType<SlashArts>, IIngredientHelper<SlashArts>, IIngredientRenderer<SlashArts> {
    public static final SAIngredient INSTANCE = new SAIngredient();
    public static final Lazy<ItemStack> RENDER_ITEM = Lazy.of(() -> SlashBladeItems.PROUDSOUL_SPHERE.get().getDefaultInstance());
    
    @Override
    public IIngredientType<SlashArts> getIngredientType() {
        return INSTANCE;
    }
    
    @Override
    public String getDisplayName(SlashArts slashArts) {
        return slashArts.getDescription().getString();
    }
    
    @Override
    public boolean hasSubtypes(SlashArts ingredient) {
        return true;
    }
    
    @SuppressWarnings("removal")
    @Override
    public String getUniqueId(SlashArts slashArts, UidContext uidContext) {
        ResourceLocation key = SlashArtsRegistry.REGISTRY.getKey(slashArts);
        if (key == null) {
            throw new NullPointerException("Key of SlashArts " + slashArts + " is null!");
        }
        return key.toString();
    }
    
    @Override
    public ResourceLocation getResourceLocation(SlashArts slashArts) {
        ResourceLocation key = SlashArtsRegistry.REGISTRY.getKey(slashArts);
        if (key == null) {
            throw new NullPointerException("Key of SlashArts " + slashArts + " is null!");
        }
        return key;
    }
    
    @Override
    public SlashArts copyIngredient(SlashArts slashArts) {
        return slashArts;
    }
    
    @Override
    public String getErrorInfo(@Nullable SlashArts slashArts) {
        return "SA Ingredient Error";
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, SlashArts slashArts) {
        guiGraphics.renderItem(RENDER_ITEM.get(), 0, 0);
        Decoration.renderSADecorator(guiGraphics, Minecraft.getInstance().font, slashArts);
    }
    
    @Override
    public List<Component> getTooltip(SlashArts slashArts, TooltipFlag tooltipFlag) {
        return List.of(slashArts.getDescription());
    }
    
    @Override
    public Class<? extends SlashArts> getIngredientClass() {
        return SlashArts.class;
    }
}
