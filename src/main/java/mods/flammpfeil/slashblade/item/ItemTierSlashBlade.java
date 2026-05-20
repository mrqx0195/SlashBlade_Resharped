package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemTierSlashBlade implements Tier {
    
    private final int uses;
    private final float attack;
    
    public ItemTierSlashBlade(int uses, float attack) {
        this.attack = attack;
        this.uses = uses;
    }
    
    @Override
    public int getUses() {
        return uses;
    }
    
    @Override
    public float getSpeed() {
        return 0;
    }
    
    @Override
    public float getAttackDamageBonus() {
        return attack;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 10;
    }
    
    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_IRON_TOOL;
    }
    
    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(SlashBladeItemTags.PROUD_SOULS);
    }
}
