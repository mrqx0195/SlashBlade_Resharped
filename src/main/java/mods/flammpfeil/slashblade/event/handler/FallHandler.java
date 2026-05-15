package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class FallHandler {
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        resetState(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onFlyableFall(PlayerFlyableFallEvent event) {
        resetState(event.getEntity());
    }
    
    public static void resetState(LivingEntity user) {
        BladeStateAccess.of(user.getMainHandItem()).ifPresent((state) -> {
            state.setFallDecreaseRate(0);
            
            ResourceLocation comboSeq = state.getComboSeq();
            ComboState combo = comboSeq != null && ComboStateRegistry.REGISTRY.containsKey(comboSeq)
                ? ComboStateRegistry.REGISTRY.get(comboSeq)
                : ComboStateRegistry.NONE.get();
            if (combo != null && combo.isAerial()) {
                state.synchronizeComboSeq(user, combo.getNextOfTimeout(user));
            }
        });
        
    }
    
    public static void spawnLandingParticle(LivingEntity user, float fallFactor) {
        if (!user.level().isClientSide()) {
            int x = Mth.floor(user.getX());
            int y = Mth.floor(user.getY() - (double) 0.5F);
            int z = Mth.floor(user.getZ());
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = user.level().getBlockState(pos);
            
            float f = (float) Mth.ceil(fallFactor);
            if (!state.isAir()) {
                double d0 = Math.min(0.2F + f / 15.0F, 2.5D);
                int i = (int) (150.0D * d0);
                if (!state.addLandingEffects((ServerLevel) user.level(), pos, state, user, i)) {
                    ((ServerLevel) user.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        user.getX(), user.getY(), user.getZ(), i, 0.0D, 0.0D, 0.0D, 0.15F);
                }
            }
        }
    }
    
    public static void spawnLandingParticle(Entity user, Vec3 targetPos, Vec3 normal, float fallFactor) {
        if (!user.level().isClientSide()) {
            
            Vec3 blockPos = targetPos.add(normal.normalize().scale(0.5f));
            
            int x = Mth.floor(blockPos.x());
            int y = Mth.floor(blockPos.y());
            int z = Mth.floor(blockPos.z());
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = user.level().getBlockState(pos);
            
            float f = (float) Mth.ceil(fallFactor);
            if (!state.isAir()) {
                double d0 = Math.min(0.2F + f / 15.0F, 2.5D);
                int i = (int) (150.0D * d0);
                ((ServerLevel) user.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    targetPos.x(), targetPos.y(), targetPos.z(), i, 0.0D, 0.0D, 0.0D, 0.15F);
            }
        }
    }
    
    public static void fallDecrease(LivingEntity user) {
        if (!user.isNoGravity() && !user.onGround()) {
            user.fallDistance = 1;
            
            float currentRatio = BladeStateAccess.of(user.getMainHandItem()).map((state) -> {
                float decRatio = state.getFallDecreaseRate();
                
                float newDecRatio = decRatio + 0.05f;
                newDecRatio = Math.min(1.0f, newDecRatio);
                state.setFallDecreaseRate(newDecRatio);
                
                return decRatio;
            }).orElse(1.0f);
            
            double gravityReductionFactor = 0.85f;
            
            var enchLookup = user.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var featherFalling = enchLookup.getOrThrow(Enchantments.FEATHER_FALLING);
            int level = EnchantmentHelper.getTagEnchantmentLevel(featherFalling, user.getMainHandItem());
            if (level > 0) {
                gravityReductionFactor = Math.min(0.93, gravityReductionFactor + 0.02 * level);
                AdvancementHelper.grantedIf(featherFalling.value(), user);
            }
            
            AttributeInstance gravity = user.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.GRAVITY);
            double g = 0;
            if (gravity != null) {
                g = gravity.getValue() * gravityReductionFactor;
            }
            
            Vec3 motion = user.getDeltaMovement();
            if (motion.y < 0) {
                user.setDeltaMovement(motion.x, (motion.y + g) * currentRatio, motion.z);
            }
        }
    }
    
    public static void fallResist(LivingEntity user) {
        if (!user.isNoGravity() && !user.onGround()) {
            user.fallDistance = 1;
            
            Vec3 motion = user.getDeltaMovement();
            AttributeInstance gravity = user.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.GRAVITY);
            double g = 0;
            if (gravity != null) {
                g = gravity.getValue();
            }
            if (motion.y < 0) {
                user.setDeltaMovement(motion.x, (motion.y + g + 0.002f), motion.z);
            }
        }
    }
}
