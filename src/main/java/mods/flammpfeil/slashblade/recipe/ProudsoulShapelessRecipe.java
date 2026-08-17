package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProudsoulShapelessRecipe extends ShapelessRecipe {
    
    private final ItemStack resultStack;
    
    public ProudsoulShapelessRecipe(String p_249640_, CraftingBookCategory p_249390_,
                                    ItemStack p_252071_, List<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, toNonNullList(p_250689_));
        this.resultStack = p_252071_;
    }
    
    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> list) {
        NonNullList<Ingredient> nnl = NonNullList.createWithCapacity(list.size());
        nnl.addAll(list);
        return nnl;
    }
    
    public static final RecipeSerializer<ProudsoulShapelessRecipe> SERIALIZER = new ProudsoulShapelessRecipe.Serializer();
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
    
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider access) {
        ItemStack result = super.assemble(input, access);
        HolderLookup.RegistryLookup<Enchantment> lookup = access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable all = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.isEnchanted()) {
                continue;
            }
            
            ItemEnchantments emap = stack.getAllEnchantments(lookup);
            for (var entry : emap.entrySet()) {
                int existing = all.getLevel(entry.getKey());
                all.set(entry.getKey(), Math.max(existing, entry.getIntValue()));
            }
        }
        
        EnchantmentHelper.setEnchantments(result, all.toImmutable());
        return result;
    }
    
    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean result = super.matches(input, level);
        
        if (result) {
            HolderLookup.Provider provider = level.registryAccess();
            HolderLookup.RegistryLookup<Enchantment> lookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
            Map<Holder<Enchantment>, Integer> all = new HashMap<>();
            
            int soulCount = 0;
            
            for (ItemStack stack : input.items()) {
                if (stack.isEmpty()) {
                    continue;
                }
                if (!stack.isEnchanted()) {
                    continue;
                }
                
                soulCount++;
                
                ItemEnchantments emap = stack.getAllEnchantments(lookup);
                
                for (var entry : emap.entrySet()) {
                    all.merge(entry.getKey(), entry.getIntValue(), Integer::sum);
                }
            }
            
            result = all.size() == 1 || all.isEmpty();
            if (result) {
                for (int value : all.values()) {
                    result = value == soulCount;
                }
            }
        }
        
        return result;
    }
    
    ItemStack getResultStack() {
        return resultStack;
    }
    
    public static class Serializer implements RecipeSerializer<ProudsoulShapelessRecipe> {
        public static final MapCodec<ProudsoulShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
            ItemStack.CODEC.fieldOf("result").forGetter(ProudsoulShapelessRecipe::getResultStack),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ShapelessRecipe::getIngredients)
        ).apply(inst, ProudsoulShapelessRecipe::new));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, ProudsoulShapelessRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        
        @Override
        public MapCodec<ProudsoulShapelessRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ProudsoulShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
    
}