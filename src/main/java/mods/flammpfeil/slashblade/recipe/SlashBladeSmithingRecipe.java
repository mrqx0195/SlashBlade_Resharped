package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.util.EnchantmentsHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.stream.Stream;

public class SlashBladeSmithingRecipe implements SmithingRecipe {
    public static final RecipeSerializer<SlashBladeSmithingRecipe> SERIALIZER =
        new SlashBladeSmithingRecipe.Serializer();
    private final ResourceLocation outputBlade;
    private final ResourceLocation id;
    
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    
    public SlashBladeSmithingRecipe(ResourceLocation id, ResourceLocation outputBlade,
                                    Ingredient template, Ingredient base, Ingredient addition) {
        super();
        this.id = id;
        this.outputBlade = outputBlade;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }
    
    public SlashBladeSmithingRecipe(ResourceLocation outputBlade, Ingredient template, Ingredient base, Ingredient addition) {
        this(outputBlade, outputBlade, template, base, addition);
    }
    
    public Ingredient getTemplate() {
        return template;
    }
    
    public Ingredient getBase() {
        return base;
    }
    
    public Ingredient getAddition() {
        return addition;
    }
    
    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, this.outputBlade);
    }
    
    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = BuiltInRegistries.ITEM.containsKey(outputBlade) ? BuiltInRegistries.ITEM.get(outputBlade)
            : SlashBladeItems.SLASHBLADE.get();
        
        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }
    
    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        ItemStack result = SlashBladeSmithingRecipe.getResultBlade(this.getOutputBlade());
        
        if (!Objects.equals(BuiltInRegistries.ITEM.getKey(result.getItem()), getOutputBlade())) {
            result = access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY)
                .getOrThrow(getOutputBladeKey())
                .value()
                .getBlade(access);
        }
        
        return result;
    }
    
    public ItemStack getResultItem(RegistryAccess access) {
        return this.getResultItem((HolderLookup.Provider) access);
    }
    
    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return this.template.test(input.template())
            && this.base.test(input.base())
            && this.addition.test(input.addition());
    }
    
    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider access) {
        var result = this.getResultItem(access);
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }
        
        var resultState = BladeStateAccess.of(result).orElseThrow();
        var stack = input.base();
        if (BladeStateAccess.of(stack).isEmpty()) {
            return ItemStack.EMPTY;
        }
        var ingredientState = BladeStateAccess.of(stack).orElseThrow();
        
        resultState.setProudSoulCount(resultState.getProudSoulCount() + ingredientState.getProudSoulCount());
        resultState.setKillCount(resultState.getKillCount() + ingredientState.getKillCount());
        if (SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get()) {
            resultState.setRefine(resultState.getRefine() + ingredientState.getRefine());
        } else {
            resultState.setRefine(Math.max(resultState.getRefine(), ingredientState.getRefine()));
        }
        EnchantmentsHelper.updateEnchantment(result, stack, access);
        ItemSlashBlade.updateRarity(result);
        
        return result;
    }
    
    public ItemStack assemble(Container container, RegistryAccess access) {
        return this.assemble(new SmithingRecipeInput(
            container.getItem(0),
            container.getItem(1),
            container.getItem(2)
        ), access);
    }
    
    
    public ResourceLocation getId() {
        return this.id;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SlashBladeSmithingRecipe.SERIALIZER;
    }
    
    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }
    
    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }
    
    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }
    
    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }
    
    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }
    
    public static class Serializer implements RecipeSerializer<SlashBladeSmithingRecipe> {
        public static final MapCodec<SlashBladeSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("template").forGetter(SlashBladeSmithingRecipe::getTemplate),
            Ingredient.CODEC.fieldOf("base").forGetter(SlashBladeSmithingRecipe::getBase),
            Ingredient.CODEC.fieldOf("addition").forGetter(SlashBladeSmithingRecipe::getAddition),
            ResourceLocation.CODEC.fieldOf("blade").forGetter(SlashBladeSmithingRecipe::getOutputBlade)
        ).apply(inst, (template, base, addition, blade) -> new SlashBladeSmithingRecipe(blade, template, base, addition)));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, SlashBladeSmithingRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        
        @Override
        public MapCodec<SlashBladeSmithingRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SlashBladeSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
