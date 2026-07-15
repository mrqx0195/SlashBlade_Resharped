package mods.flammpfeil.slashblade.data;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipeBuilder;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipeBuilder;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.EnchantmentDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SlashBladeRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private final CompletableFuture<HolderLookup.Provider> lookup;
    
    public SlashBladeRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        this.lookup = registries;
    }
    
    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        SlashBladeSmithingRecipeBuilder
            .smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.RODAI_DIAMOND.location()).build()),
                Ingredient.of(Tags.Items.INGOTS_NETHERITE), RecipeCategory.COMBAT,
                SlashBladeBuiltInRegistry.RODAI_NETHERITE.location())
            .unlocks(getHasName(Items.NETHERITE_INGOT), has(Tags.Items.INGOTS_NETHERITE))
            .save(output, SlashBlade.prefix("rodai_netherite_smithing"));
        
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, SlashBladeItems.SLASHBLADE_WOOD.get()).pattern("  L")
            .pattern(" L ").pattern("B  ").define('B', Items.WOODEN_SWORD).define('L', ItemTags.LOGS)
            .unlockedBy(getHasName(Items.WOODEN_SWORD), has(Items.WOODEN_SWORD)).save(output);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_BAMBOO.get()).pattern("  L").pattern(" L ")
            .pattern("B  ").define('B', SlashBladeItems.SLASHBLADE_WOOD.get())
            .define('L', SlashBladeItemTags.BAMBOO).unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()),
                has(SlashBladeItems.SLASHBLADE_WOOD.get()))
            .save(output);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()).pattern(" EI")
            .pattern("SBD").pattern("PS ").define('B', SlashBladeItems.SLASHBLADE_BAMBOO.get())
            .define('I', Tags.Items.INGOTS_IRON).define('S', Tags.Items.STRINGS).define('P', Items.PAPER)
            .define('E', Items.EGG).define('D', Tags.Items.DYES_BLACK)
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_BAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_BAMBOO.get()))
            .save(output);
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE_WHITE.get()).pattern("  L").pattern(" L ")
            .pattern("BG ").define('B', SlashBladeItems.SLASHBLADE_WOOD.get())
            .define('L', SlashBladeItems.PROUDSOUL_INGOT.get()).define('G', Tags.Items.INGOTS_GOLD)
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()),
                has(SlashBladeItems.SLASHBLADE_WOOD.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.YAMATO.location()).pattern("PPP").pattern("PBP")
            .pattern("PPP")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.YAMATO.location()).addSwordType(SwordType.BROKEN)
                    .addSwordType(SwordType.SEALED).build()))
            .define('P', SlashBladeItems.PROUDSOUL_SPHERE.get())
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE.get()), has(SlashBladeItems.SLASHBLADE.get()))
            .save(output, SlashBlade.prefix("yamato_fix"));
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeItems.SLASHBLADE.get()).pattern(" EI").pattern("PBD")
            .pattern("SI ")
            .define('B',
                SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_WHITE.get(),
                    RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
            .define('I', Tags.Items.INGOTS_GOLD).define('S', Tags.Items.STRINGS).define('P', Tags.Items.DYES_BLUE)
            .define('E', Tags.Items.RODS_BLAZE).define('D', Tags.Items.STORAGE_BLOCKS_COAL)
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WHITE.get()),
                has(SlashBladeItems.SLASHBLADE_WHITE.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.RUBY.location()).pattern("DPI").pattern("PB ")
            .pattern("S  ")
            .define('B',
                SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                    RequestDefinition.Builder.newInstance().addSwordType(SwordType.BROKEN).build()))
            .define('I', SlashBladeItems.PROUDSOUL.get()).define('S', Tags.Items.STRINGS)
            .define('P', SlashBladeItems.PROUDSOUL_INGOT.get()).define('D', Tags.Items.DYES_RED)
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_BLACK.location()).pattern(" EF")
            .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT).define('Q', Items.QUARTZ_BLOCK)
            .define('B', Items.BLAZE_POWDER).define('S', SlashBladeItems.PROUDSOUL_CRYSTAL.get())
            .define('E', Tags.Items.OBSIDIANS).define('F', Tags.Items.FEATHERS)
            .define('C', SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                .name(SlashBladeBuiltInRegistry.RUBY.location())
                .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.SMITE), 1)).build()))
            
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.FOX_WHITE.location()).pattern(" EF")
            .pattern("BCS").pattern("WQ ").define('W', Tags.Items.CROPS_WHEAT).define('Q', Items.QUARTZ_BLOCK)
            .define('B', Items.BLAZE_POWDER).define('S', SlashBladeItems.PROUDSOUL_CRYSTAL.get())
            .define('E', Tags.Items.OBSIDIANS).define('F', Tags.Items.FEATHERS)
            .define('C', SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                .name(SlashBladeBuiltInRegistry.RUBY.location())
                
                .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.LOOTING), 1)).build()))
            
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.MURAMASA.location()).pattern("SSS")
            .pattern("SBS").pattern("SSS")
            .define('B',
                SlashBladeIngredient
                    .of(RequestDefinition.Builder.newInstance().proudSoul(10000).refineCount(20).build()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE.get()), has(SlashBladeItems.SLASHBLADE.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TAGAYASAN.location()).pattern("SES")
            .pattern("DBD").pattern("SES")
            .define('B', SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_WOOD.get(),
                RequestDefinition.Builder.newInstance()
                    .addEnchantment(new EnchantmentDefinition(getEnchantmentID(Enchantments.UNBREAKING), 1))
                    .proudSoul(1000).refineCount(10).build()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .define('E', Ingredient.of(Items.ENDER_EYE)).define('D', Ingredient.of(Items.ENDER_PEARL))
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_WOOD.get()),
                has(SlashBladeItems.SLASHBLADE_WOOD.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.AGITO.location()).pattern(" S ").pattern("SBS")
            .pattern(" S ")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.AGITO_RUST.location()).killCount(100).build()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL.get()))
            .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL.get()), has(SlashBladeItems.PROUDSOUL.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED.location()).pattern(" S ")
            .pattern("SBS").pattern(" S ")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.OROTIAGITO_RUST.location()).killCount(100).build()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL.get()))
            .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL.get()), has(SlashBladeItems.PROUDSOUL.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.OROTIAGITO.location()).pattern("PSP")
            .pattern("SBS").pattern("PSP")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.OROTIAGITO_SEALED.location()).killCount(1000)
                    .proudSoul(1000).refineCount(10).build()))
            .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL.get()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_SPHERE.get()),
                has(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.DOUTANUKI.location()).pattern("  P")
            .pattern(" B ").pattern("P  ")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.SABIGATANA.location()).killCount(100).proudSoul(1000)
                    .refineCount(10).build()))
            .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_SPHERE.get()),
                has(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.SABIGATANA.location()).pattern("  P")
            .pattern(" P ").pattern("B  ")
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .name(SlashBladeBuiltInRegistry.SABIGATANA.location()).addSwordType(SwordType.BROKEN)
                    .addSwordType(SwordType.SEALED).build()))
            .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_INGOT.get()))
            .unlockedBy(getHasName(SlashBladeItems.PROUDSOUL_INGOT.get()),
                has(SlashBladeItems.PROUDSOUL_INGOT.get()))
            .save(output);
        
        SlashBladeShapedRecipeBuilder.shaped(SlashBladeBuiltInRegistry.TUKUMO.location()).pattern("ESD").pattern("RBL")
            .pattern("ISG").define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
            .define('L', Tags.Items.STORAGE_BLOCKS_LAPIS).define('G', Tags.Items.STORAGE_BLOCKS_GOLD)
            .define('I', Tags.Items.STORAGE_BLOCKS_IRON).define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
            .define('E', Tags.Items.STORAGE_BLOCKS_EMERALD)
            .define('B',
                SlashBladeIngredient.of(RequestDefinition.Builder.newInstance()
                    .addEnchantment(
                        new EnchantmentDefinition(getEnchantmentID(Enchantments.FIRE_ASPECT), 1))
                    .build()))
            .define('S', Ingredient.of(SlashBladeItems.PROUDSOUL_SPHERE.get()))
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE.get()), has(SlashBladeItems.SLASHBLADE.get()))
            .save(output);
        
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_WOODEN.location(), Items.WOODEN_SWORD, output);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_STONE.location(), Items.STONE_SWORD, output);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_IRON.location(), Items.IRON_SWORD, output);
        rodaiRecipe(SlashBladeBuiltInRegistry.RODAI_GOLDEN.location(), Items.GOLDEN_SWORD, output);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_DIAMOND.location(), Items.DIAMOND_SWORD, output);
        rodaiAdvRecipe(SlashBladeBuiltInRegistry.RODAI_NETHERITE.location(), Items.NETHERITE_SWORD, output);
    }
    
    private void rodaiRecipe(ResourceLocation rodai, ItemLike sword, RecipeOutput output) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                    RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
            .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRINGS))
            .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_CRYSTAL.get()))
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
            .save(output);
    }
    
    private void rodaiAdvRecipe(ResourceLocation rodai, ItemLike sword, RecipeOutput output) {
        SlashBladeShapedRecipeBuilder.shaped(rodai).pattern("  P").pattern(" B ").pattern("WS ").define('B',
                SlashBladeIngredient.of(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(),
                    RequestDefinition.Builder.newInstance().killCount(100).addSwordType(SwordType.BROKEN).build()))
            .define('W', Ingredient.of(sword)).define('S', Ingredient.of(Tags.Items.STRINGS))
            .define('P', Ingredient.of(SlashBladeItems.PROUDSOUL_TRAPEZOHEDRON.get()))
            .unlockedBy(getHasName(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()),
                has(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get()))
            .save(output);
    }
    
    private Holder<Enchantment> getEnchantmentID(ResourceKey<Enchantment> enchantmentKey) {
        return this.lookup.join().lookup(Registries.ENCHANTMENT).orElseThrow().getOrThrow(enchantmentKey);
    }
}
