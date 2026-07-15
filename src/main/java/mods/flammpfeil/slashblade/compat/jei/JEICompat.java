package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.registration.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.compat.jei.category.EntityDropEntryRecipeCategory;
import mods.flammpfeil.slashblade.compat.jei.category.SARecipeCategory;
import mods.flammpfeil.slashblade.compat.jei.category.SERecipeCategory;
import mods.flammpfeil.slashblade.compat.jei.category.SpecialBladeDescRecipeCategory;
import mods.flammpfeil.slashblade.compat.jei.data.ShowEntityListener;
import mods.flammpfeil.slashblade.compat.jei.data.SpecialBladeDescListener;
import mods.flammpfeil.slashblade.compat.jei.ingredient.SAIngredient;
import mods.flammpfeil.slashblade.compat.jei.ingredient.SEIngredient;
import mods.flammpfeil.slashblade.compat.jei.utils.SARecipeManager;
import mods.flammpfeil.slashblade.compat.jei.utils.SERecipeManager;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.util.BladeRegisterManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@JeiPlugin
public class JEICompat implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SlashBlade.prefix(SlashBlade.MODID);
    }
    
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SlashBladeItems.SLASHBLADE.get(), SlashBladeSubtypeInterpreter.INSTANCE);
    }
    
    public static String syncSlashBlade(ItemStack stack, UidContext context) {
        return BladeStateAccess.of(stack).map(ISlashBladeState::getTranslationKey).orElse("");
    }
    
    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableSmithingRecipeCategory smithingCategory = registration.getSmithingCategory();
        
        smithingCategory.addExtension(SlashBladeSmithingRecipe.class, new SlashBladeSmithingCategoryExtension());
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        BladeRegisterManager.build();
        registration.addRecipes(SARecipeCategory.SA_TYPE, SlashArtsRegistry.REGISTRY.stream().toList());
        registration.addRecipes(SERecipeCategory.SE_TYPE, SpecialEffectsRegistry.REGISTRY.stream().toList());
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            Stream<EntityDropEntry> drops = connection.registryAccess().registryOrThrow(EntityDropEntry.REGISTRY_KEY).stream();
            drops = drops.filter(entityDropEntry -> {
                ClientLevel level = Minecraft.getInstance().level;
                if (level != null) {
                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityDropEntry.entityType())) {
                        return false;
                    }
                    return SlashBlade.getSlashBladeDefinitionRegistry(level).containsKey(entityDropEntry.bladeName());
                }
                return true;
            });
            registration.addRecipes(EntityDropEntryRecipeCategory.DROP_TYPE, drops.toList());
        }
        registration.addRecipes(SpecialBladeDescRecipeCategory.DESC_TYPE, SpecialBladeDescListener.DESC_DATA);
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new SARecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new SERecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new EntityDropEntryRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new SpecialBladeDescRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }
    
    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(SAIngredient.INSTANCE, SlashArtsRegistry.REGISTRY.stream().toList(),
            SAIngredient.INSTANCE, SAIngredient.INSTANCE, SlashArtsRegistry.REGISTRY.byNameCodec());
        registration.register(SEIngredient.INSTANCE, SpecialEffectsRegistry.REGISTRY.stream().toList(),
            SEIngredient.INSTANCE, SEIngredient.INSTANCE, SpecialEffectsRegistry.REGISTRY.byNameCodec());
    }
    
    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(SARecipeCategory.SA_TYPE, SARecipeManager.INSTANCE);
        registration.addTypedRecipeManagerPlugin(SERecipeCategory.SE_TYPE, SERecipeManager.INSTANCE);
    }
    
    public static void registerClientReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SpecialBladeDescListener());
        event.registerReloadListener(new ShowEntityListener());
    }
}
