package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.init.DefaultResources;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ItemSlashBladeDetune extends ItemSlashBlade {
    private ResourceLocation model;
    private ResourceLocation texture;
    private final float baseAttack;
    private boolean isDestructable;
    
    public ItemSlashBladeDetune(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
        this.baseAttack = attackDamageIn;
        this.isDestructable = false;
        this.model = DefaultResources.resourceDefaultModel;
        this.texture = DefaultResources.resourceDefaultTexture;
    }
    
    public ResourceLocation getModel() {
        return model;
    }
    
    public ItemSlashBladeDetune setModel(ResourceLocation model) {
        this.model = model;
        return this;
    }
    
    public ResourceLocation getTexture() {
        return texture;
    }
    
    public float getBaseAttackModifier() {
        return baseAttack;
    }
    
    public ItemSlashBladeDetune setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }
    
    public boolean isDestructable() {
        return isDestructable;
    }
    
    public ItemSlashBladeDetune setDestructable() {
        this.isDestructable = true;
        return this;
    }
    
    @Override
    public boolean isDestructable(ItemStack stack) {
        return this.isDestructable;
    }
    
    @Override
    public void appendSwordType(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    
    }
    
    @Override
    public EnumSet<SwordType> getSwordType(EnumSet<SwordType> from, ItemStack itemStack, Optional<ISlashBladeState> state) {
        EnumSet<SwordType> types = super.getSwordType(from, itemStack, state);
        types.remove(SwordType.ENCHANTED);
        types.remove(SwordType.BEWITCHED);
        return types;
    }
}
