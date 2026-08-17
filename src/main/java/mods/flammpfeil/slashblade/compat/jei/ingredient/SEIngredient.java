package mods.flammpfeil.slashblade.compat.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
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
public class SEIngredient implements IIngredientType<SpecialEffect>, IIngredientHelper<SpecialEffect>, IIngredientRenderer<SpecialEffect> {
    public static final SEIngredient INSTANCE = new SEIngredient();
    public static final Lazy<ItemStack> RENDER_ITEM = Lazy.of(() -> SlashBladeItems.PROUDSOUL_CRYSTAL.get().getDefaultInstance());
    
    @Override
    public IIngredientType<SpecialEffect> getIngredientType() {
        return INSTANCE;
    }
    
    @Override
    public String getDisplayName(SpecialEffect specialEffect) {
        return specialEffect.getDescription().getString();
    }
    
    @Override
    public boolean hasSubtypes(SpecialEffect ingredient) {
        return true;
    }
    
    @SuppressWarnings("removal")
    @Override
    public String getUniqueId(SpecialEffect specialEffect, UidContext uidContext) {
        ResourceLocation key = SpecialEffectsRegistry.REGISTRY.getKey(specialEffect);
        if (key == null) {
            throw new NullPointerException("Key of SpecialEffect " + specialEffect + " is null!");
        }
        return key.toString();
    }
    
    @Override
    public ResourceLocation getResourceLocation(SpecialEffect specialEffect) {
        ResourceLocation key = SpecialEffectsRegistry.REGISTRY.getKey(specialEffect);
        if (key == null) {
            throw new NullPointerException("Key of SpecialEffect " + specialEffect + " is null!");
        }
        return key;
    }
    
    @Override
    public SpecialEffect copyIngredient(SpecialEffect specialEffect) {
        return specialEffect;
    }
    
    @Override
    public String getErrorInfo(@Nullable SpecialEffect specialEffect) {
        return "SA Ingredient Error";
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, SpecialEffect specialEffect) {
        guiGraphics.renderItem(RENDER_ITEM.get(), 0, 0);
        Decoration.renderSEDecorator(guiGraphics, Minecraft.getInstance().font, specialEffect);
    }
    
    @Override
    public List<Component> getTooltip(SpecialEffect specialEffect, TooltipFlag tooltipFlag) {
        return List.of(specialEffect.getDescription());
    }
    
    @Override
    public Class<? extends SpecialEffect> getIngredientClass() {
        return SpecialEffect.class;
    }
}
