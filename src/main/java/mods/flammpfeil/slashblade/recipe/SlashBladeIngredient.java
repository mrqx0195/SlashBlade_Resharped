package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlashBladeIngredient implements ICustomIngredient {
    private final Set<Item> items;
    private final RequestDefinition request;
    
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static Supplier<IngredientType<SlashBladeIngredient>> TYPE;
    
    private static final Codec<Set<Item>> ITEMS_CODEC =
        ResourceLocation.CODEC.listOf().xmap(
            ids -> ids.stream().map(BuiltInRegistries.ITEM::get).collect(Collectors.toSet()),
            items -> items.stream().map(BuiltInRegistries.ITEM::getKey).toList()
        );
    
    public static final MapCodec<SlashBladeIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ITEMS_CODEC.fieldOf("items").forGetter(i -> i.items),
        RequestDefinition.CODEC.fieldOf("request").forGetter(i -> i.request)
    ).apply(inst, SlashBladeIngredient::new));
    
    public static final StreamCodec<? super RegistryFriendlyByteBuf, SlashBladeIngredient> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    
    protected SlashBladeIngredient(Set<Item> items, RequestDefinition request) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a SlashBladeIngredient with no items");
        }
        this.items = Set.copyOf(items);
        this.request = request;
    }
    
    public static Ingredient of(ItemLike item, RequestDefinition request) {
        return new SlashBladeIngredient(Set.of(item.asItem()), request).toVanilla();
    }
    
    public static Ingredient of(RequestDefinition request) {
        return new SlashBladeIngredient(Set.of(SlashBladeItems.SLASHBLADE.get()), request).toVanilla();
    }
    
    public static Ingredient of(ItemLike item, ResourceLocation request) {
        return new SlashBladeIngredient(Set.of(item.asItem()),
            RequestDefinition.Builder.newInstance().name(request).build()).toVanilla();
    }
    
    public static Ingredient of(ResourceLocation request) {
        return new SlashBladeIngredient(Set.of(SlashBladeItems.SLASHBLADE.get()),
            RequestDefinition.Builder.newInstance().name(request).build()).toVanilla();
    }
    
    public static Ingredient blankNameless() {
        return of(RequestDefinition.Builder.newInstance().build());
    }
    
    @Override
    public boolean test(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        return items.contains(input.getItem()) && this.request.test(input);
    }
    
    @Override
    public boolean isSimple() {
        return false;
    }
    
    @Override
    public Stream<ItemStack> getItems() {
        return items.stream().map(item -> {
            var result = new ItemStack(item);
            this.request.initItemStack(result);
            return result;
        });
    }
    
    @Override
    public IngredientType<?> getType() {
        return TYPE.get();
    }
}
