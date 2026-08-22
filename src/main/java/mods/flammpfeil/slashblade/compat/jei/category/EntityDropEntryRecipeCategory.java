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
        this(icon.createDrawableItemLike(Items.ZOMBIE_HEAD));
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
        return 16 * 5; // 80
    }

    @Override
    public int getHeight() {
        return 104;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EntityDropEntry entityDropEntry, IFocusGroup iFocusGroup) {
        ItemStack blade = BladeRegisterManager.getBlade(entityDropEntry.bladeName());

        boolean hasEgg = false;
        Item entityItem = null;
        EntityType<?> entityType = null;

        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityDropEntry.entityType())) {
            entityType = BuiltInRegistries.ENTITY_TYPE.get(entityDropEntry.entityType());
            entityItem = DeferredSpawnEggItem.deferredOnlyById(entityType);
            if (entityItem != null) {
                hasEgg = true;
            }
        }

        if (hasEgg) {
            IRecipeSlotBuilder entitySlot = builder.addInputSlot(8, 22).setStandardSlotBackground();
            entitySlot.addItemLike(entityItem);

            final EntityType<?> finalType = entityType;
            entitySlot.addRichTooltipCallback((iRecipeSlotView, components) -> {
                components.clear();
                components.add(finalType.getDescription());
            });

            IRecipeSlotBuilder bladeSlot = builder.addOutputSlot(64 - 8, 22).setStandardSlotBackground();
            bladeSlot.addItemStack(blade);
        } else {
            IRecipeSlotBuilder bladeSlot = builder.addOutputSlot(31, 22).setStandardSlotBackground();
            bladeSlot.addItemStack(blade);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, EntityDropEntry recipe, IFocusGroup focuses) {
        boolean hasEgg = false;

        if (BuiltInRegistries.ENTITY_TYPE.containsKey(recipe.entityType())) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(recipe.entityType());

            EntityDrawable entityDrawable = new EntityDrawable(entityType);

            int centerX = getWidth() / 2;
            int centerY = 76;

            builder.addDrawable(entityDrawable, centerX, centerY);

            if (DeferredSpawnEggItem.deferredOnlyById(entityType) != null) {
                hasEgg = true;
            }
        }

        if (hasEgg) {
            builder.addRecipeArrow().setPosition(29, 22);
        }

        builder.addText(Component.translatable("jei.category.slashblade.entity_drop_entry.chance.tip"), getWidth(), 10)
                        .setTextAlignment(HorizontalAlignment.CENTER)
                        .setPosition(0, 3);

        builder.addText(Component.translatable("jei.category.slashblade.entity_drop_entry.chance", String.format("%.2f", recipe.dropRate() * 100)), getWidth(), 10)
                        .setTextAlignment(HorizontalAlignment.CENTER)
                        .setPosition(0, 13);

        if (recipe.requestSlashBladeKill()) {
            builder.addText(Component.translatable("jei.category.slashblade.entity_drop_entry.need_slash_blade"), getWidth(), 10)
                            .setTextAlignment(HorizontalAlignment.CENTER)
                            .setPosition(0, 40);
        }
    }
}
