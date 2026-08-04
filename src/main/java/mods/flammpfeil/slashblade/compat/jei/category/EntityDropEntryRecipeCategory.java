package mods.flammpfeil.slashblade.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.compat.jei.drawable.EntityDrawable;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.util.BladeRegisterManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

/**
 * @author yiran1457
 */
public record EntityDropEntryRecipeCategory(IDrawable icon) implements IRecipeCategory<EntityDropEntry> {
    public static final RecipeType<EntityDropEntry> DROP_TYPE = RecipeType.create(SlashBlade.MODID, "entity_drop_entry", EntityDropEntry.class);
    
    public EntityDropEntryRecipeCategory(IGuiHelper icon) {
        this(icon.createDrawableItemLike(SlashBladeItems.PROUDSOUL_TINY.get()));
    }
    
    @Override
    public RecipeType<EntityDropEntry> getRecipeType() {
        return DROP_TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.slashblade.entity_drop_entry");
    }
    
    @Override
    public int getWidth() {
        return 16 * 5;
    }
    
    @Override
    public int getHeight() {
        return 88;//30;
    }
    
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EntityDropEntry entityDropEntry, IFocusGroup iFocusGroup) {
        Item entityItem;
        IRecipeSlotBuilder entitySlot = builder.addInputSlot(8, 6).setStandardSlotBackground();
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityDropEntry.entityType())) {
            entityItem = Items.BARRIER;
            entitySlot.addRichTooltipCallback((iRecipeSlotView, components) -> {
                components.clear();
                components.add(Component.translatable("jei.slashblade.entity.not_registered", entityDropEntry.entityType()));
            });
        } else {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityDropEntry.entityType());
            entityItem = DeferredSpawnEggItem.deferredOnlyById(entityType);
            if (entityItem == null) {
                entityItem = Items.STRUCTURE_VOID;
            }
            entitySlot.addRichTooltipCallback((iRecipeSlotView, components) -> {
                components.clear();
                components.add(entityType.getDescription());
                components.add(entityType.getDescription());
            });
        }
        entitySlot.addItemLike(entityItem);
        ItemStack blade = BladeRegisterManager.getBlade(entityDropEntry.bladeName());
        IRecipeSlotBuilder bladeSlot = builder.addOutputSlot(64 - 8, 6).setStandardSlotBackground();
        if (blade == null) {
            bladeSlot.addRichTooltipCallback((iRecipeSlotView, components) -> {
                    components.clear();
                    components.add(Component.translatable("jei.slashblade.blade.not_registered", entityDropEntry.bladeName()));
                })
                .addItemLike(Items.BARRIER);
        } else {
            bladeSlot.addItemStack(blade);
        }
    }
    
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, EntityDropEntry recipe, IFocusGroup focuses) {
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(recipe.entityType())) {
            builder.addDrawable(new EntityDrawable(BuiltInRegistries.ENTITY_TYPE.get(recipe.entityType())), 0, 0);
        }
        builder.addRecipeArrow()
            .setPosition(29, 6);
        builder.addText(Component.translatable("jei.category.slashblade.entity_drop_entry.chance", String.format("%.2f", recipe.dropRate() * 100)), getWidth(), 10)
            .setTextAlignment(HorizontalAlignment.CENTER)
            .setPosition(0, -2);
        if (recipe.requestSlashBladeKill()) {
            builder.addText(Component.translatable("jei.category.slashblade.entity_drop_entry.need_slash_blade"), getWidth(), 10)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, 23);
        }
        
    }
}
