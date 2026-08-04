package mods.flammpfeil.slashblade.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemProudSoul extends Item {
    public ItemProudSoul(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isFoil(ItemStack item) {
        return true;
    }
}
