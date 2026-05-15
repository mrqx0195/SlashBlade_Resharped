package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class SlashBladeShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category = RecipeCategory.COMBAT;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    @Nullable
    private ResourceLocation blade;
    
    public SlashBladeShapedRecipeBuilder(ItemLike item, int count) {
        this.result = item.asItem();
        this.count = count;
    }
    
    public static SlashBladeShapedRecipeBuilder shaped(ResourceLocation blade) {
        return shaped(SlashBladeItems.SLASHBLADE.get(), 1).blade(blade);
    }
    
    public static SlashBladeShapedRecipeBuilder shaped(ItemLike result) {
        return shaped(result, 1);
    }
    
    public static SlashBladeShapedRecipeBuilder shaped(ItemLike result, int count) {
        return new SlashBladeShapedRecipeBuilder(result, count);
    }
    
    public SlashBladeShapedRecipeBuilder define(Character key, TagKey<Item> tag) {
        return this.define(key, Ingredient.of(tag));
    }
    
    public SlashBladeShapedRecipeBuilder define(Character key, ItemLike tag) {
        return this.define(key, Ingredient.of(tag));
    }
    
    public SlashBladeShapedRecipeBuilder blade(ResourceLocation blade) {
        this.blade = blade;
        return this;
    }
    
    public SlashBladeShapedRecipeBuilder define(Character key, Ingredient ingredient) {
        if (this.key.containsKey(key)) {
            throw new IllegalArgumentException("Symbol '" + key + "' is already defined!");
        } else if (key == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(key, ingredient);
            return this;
        }
    }
    
    public SlashBladeShapedRecipeBuilder pattern(String pattern) {
        if (!this.rows.isEmpty() && pattern.length() != this.rows.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pattern);
            return this;
        }
    }
    
    @Override
    public @NotNull SlashBladeShapedRecipeBuilder unlockedBy(@NotNull String key,
                                                             @NotNull Criterion<?> trigger) {
        this.criteria.put(key, trigger);
        this.advancement.addCriterion(key, trigger);
        return this;
    }
    
    @Override
    public @NotNull SlashBladeShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }
    
    @Override
    public @NotNull Item getResult() {
        return this.result;
    }
    
    @Override
    public void save(@NotNull RecipeOutput output) {
        ResourceLocation id = this.blade != null ? this.blade : BuiltInRegistries.ITEM.getKey(this.result);
        this.save(output, id);
    }
    
    @Override
    public void save(RecipeOutput output, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancementBuilder = output.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancementBuilder::addCriterion);
        
        ShapedRecipePattern pattern = ShapedRecipePattern.of(this.key, this.rows);
        ItemStack resultStack = new ItemStack(this.result, this.count);
        SlashBladeShapedRecipe recipe = new SlashBladeShapedRecipe(
            this.group == null ? "" : this.group,
            CraftingBookCategory.EQUIPMENT,
            pattern,
            resultStack,
            Optional.ofNullable(this.blade)
        );
        
        output.accept(id, recipe, advancementBuilder.build(
            id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
    
    private void ensureValid(ResourceLocation id) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
        } else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');
            
            for (String s : this.rows) {
                for (int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if (!this.key.containsKey(c0) && c0 != ' ') {
                        throw new IllegalStateException(
                            "Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
                    }
                    
                    set.remove(c0);
                }
            }
            
            if (!set.isEmpty()) {
                throw new IllegalStateException(
                    "Ingredients are defined but not used in pattern for recipe " + id);
            } else if (this.rows.size() == 1 && this.rows.getFirst().length() == 1) {
                throw new IllegalStateException("Shaped recipe " + id
                    + " only takes in a single item - should it be a shapeless recipe instead?");
            } else if (this.criteria.isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + id);
            }
        }
    }
}
