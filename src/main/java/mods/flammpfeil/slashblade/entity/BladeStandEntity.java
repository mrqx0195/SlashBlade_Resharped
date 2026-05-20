package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BladeStandEntity extends ItemFrame implements IEntityWithComplexSpawn {
    
    public Item currentType = null;
    public ItemStack currentTypeStack = ItemStack.EMPTY;
    
    public BladeStandEntity(EntityType<? extends BladeStandEntity> p_i50224_1_, Level p_i50224_2_) {
        super(p_i50224_1_, p_i50224_2_);
    }
    
    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity serverEntity) {
        return super.getAddEntityPacket(serverEntity);
    }
    
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        String standTypeStr;
        ResourceLocation itemsKey = BuiltInRegistries.ITEM.getKey(this.currentType);
        if (this.currentType != null) {
            standTypeStr = itemsKey.toString();
        } else {
            standTypeStr = "";
        }
        compound.putString("StandType", standTypeStr);
        
        compound.putByte("Pose", (byte) this.getPose().ordinal());
    }
    
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String standTypeStr = compound.getString("StandType");
        if (standTypeStr.isEmpty()) {
            this.currentType = null;
        } else {
            ResourceLocation loc = ResourceLocation.tryParse(standTypeStr);
            this.currentType = loc != null ? BuiltInRegistries.ITEM.get(loc) : null;
        }
        byte poseOrdinal = compound.getByte("Pose");
        Pose[] poses = Pose.values();
        this.setPose(poses.length > 0 ? poses[poseOrdinal % poses.length] : Pose.STANDING);
    }
    
    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        CompoundTag tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        buffer.writeNbt(tag);
    }
    
    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        CompoundTag tag = additionalData.readNbt();
        if (tag != null) {
            this.readAdditionalSaveData(tag);
        }
    }
    
    public static BladeStandEntity createInstanceFromPos(Level worldIn, BlockPos placePos, Direction dir, Item type) {
        BladeStandEntity e = new BladeStandEntity(RegistryEvents.BladeStand, worldIn);
        
        e.setPos(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5);
        e.setDirection(dir);
        e.currentType = type;
        
        return e;
    }
    
    @Nullable
    @Override
    public ItemEntity spawnAtLocation(@NotNull ItemLike iip) {
        if (iip.equals(Items.ITEM_FRAME)) {
            if (this.currentType == null || this.currentType.equals(Items.AIR)) {
                return null;
            }
            
            iip = this.currentType;
        }
        return super.spawnAtLocation(iip);
    }
    
    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float cat) {
        ItemStack blade = this.getItem();
        
        if (blade.isEmpty()) {
            return super.hurt(damageSource, cat);
        }
        
        if (BladeStateAccess.of(blade).isEmpty()) {
            return super.hurt(damageSource, cat);
        }
        
        ISlashBladeState state = BladeStateAccess.of(blade).orElseThrow();
        if (NeoForge.EVENT_BUS.post(new SlashBladeEvent.BladeStandAttackEvent(blade, state, this, damageSource)).isCanceled()) {
            return true;
        }
        
        return super.hurt(damageSource, cat);
    }
    
    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = InteractionResult.PASS;
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && !this.getItem().isEmpty()) {
                Pose current = this.getPose();
                int newIndex = (current.ordinal() + 1) % Pose.values().length;
                this.setPose(Pose.values()[newIndex]);
                result = InteractionResult.SUCCESS;
            } else if ((!itemstack.isEmpty() && BladeStateAccess.of(itemstack).isPresent())
                || (itemstack.isEmpty() && !this.getItem().isEmpty())) {
                
                if (this.getItem().isEmpty()) {
                    if (!this.isRemoved()) {
                        this.setItem(itemstack);
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
                        result = InteractionResult.SUCCESS;
                    }
                } else {
                    ItemStack displayed = this.getItem().copy();
                    
                    this.setItem(itemstack);
                    player.setItemInHand(hand, displayed);
                    
                    this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
                    result = InteractionResult.SUCCESS;
                    
                }
                
            } else {
                this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
                this.setRotation(this.getRotation() + 1);
                result = InteractionResult.SUCCESS;
            }
        }
        return result;
    }
    
    @Override
    protected @NotNull ItemStack getFrameItemStack() {
        if (currentType == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(currentType);
    }
    
    @Override
    public boolean survives() {
        return true;
    }
    
    @Override
    public void tick() {
        super.tick();
        ItemStack blade = this.getItem();
        if (blade.isEmpty()) {
            return;
        }
        BladeStateAccess.of(blade).ifPresent(state ->
            NeoForge.EVENT_BUS.post(new SlashBladeEvent.BladeStandTickEvent(blade, state, this)));
    }
}
