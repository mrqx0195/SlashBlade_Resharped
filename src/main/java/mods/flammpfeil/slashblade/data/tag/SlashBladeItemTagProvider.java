package mods.flammpfeil.slashblade.data.tag;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SlashBladeItemTagProvider extends ItemTagsProvider {
    
    public SlashBladeItemTagProvider(PackOutput output, CompletableFuture<Provider> provider, CompletableFuture<TagLookup<Block>> blockLookup,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, blockLookup, SlashBlade.MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(@NotNull Provider provider) {
        
        this.tag(ItemTags.SWORDS).addTag(SlashBladeItemTags.SLASHBLADE_SWORDS);
        this.tag(ItemTags.SHARP_WEAPON_ENCHANTABLE).addTag(SlashBladeItemTags.SLASHBLADE_SWORDS);
        this.tag(SlashBladeItemTags.ENCHANTABLE_SWORD).addTag(SlashBladeItemTags.SLASHBLADE_SWORDS);
        this.tag(SlashBladeItemTags.SLASHBLADE_SWORDS)
            .add(SlashBladeItems.SLASHBLADE.get())
            .add(SlashBladeItems.SLASHBLADE_BAMBOO.get())
            .add(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get())
            .add(SlashBladeItems.SLASHBLADE_WHITE.get())
            .add(SlashBladeItems.SLASHBLADE_WOOD.get());
    }
    
}
