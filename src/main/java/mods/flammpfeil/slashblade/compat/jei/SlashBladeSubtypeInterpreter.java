package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlashBladeSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    public static final SlashBladeSubtypeInterpreter INSTANCE = new SlashBladeSubtypeInterpreter();
    
    private SlashBladeSubtypeInterpreter() {
    
    }
    
    @Override
    @Nullable
    public Object getSubtypeData(@NotNull ItemStack ingredient, @NotNull UidContext context) {
        return BladeStateAccess.of(ingredient).map(ISlashBladeState::getTranslationKey).orElse("");
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getLegacyStringSubtypeInfo(@NotNull ItemStack ingredient, @NotNull UidContext context) {
        return getStringName(ingredient);
    }
    
    public String getStringName(ItemStack itemStack) {
        return BladeStateAccess.of(itemStack).map(ISlashBladeState::getTranslationKey).orElse("");
    }
}
