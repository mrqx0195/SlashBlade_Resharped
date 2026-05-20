package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.stack.Comparison;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.world.item.ItemStack;

public class EMIUtils {
    
    public static Comparison SLASHBLADE_COMPARISON = Comparison.of((self, other) -> {
        ItemStack aStack = self.getItemStack();
        ItemStack bStack = other.getItemStack();
        if (!aStack.getItem().equals(bStack.getItem())) {
            return false;
        }
        String keyA = BladeStateAccess.of(aStack).map(ISlashBladeState::getTranslationKey).orElse("");
        String keyB = BladeStateAccess.of(bStack).map(ISlashBladeState::getTranslationKey).orElse("");
        
        return keyB.equals(keyA);
    });
}
