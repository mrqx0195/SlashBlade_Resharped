package mods.flammpfeil.slashblade.slasharts;

import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class JudgementCut {
    static public EntityJudgementCut doJudgementCutJust(LivingEntity user) {
        EntityJudgementCut sa = doJudgementCut(user);
        sa.setIsCritical(true);
        return sa;
    }
    
    static public EntityJudgementCut doJudgementCut(LivingEntity user) {
        
        Level worldIn = user.level();
        
        Vec3 eyePos = user.getEyePosition(1.0f);
        final double airReach = 5;
        final double entityReach = 7;
        
        ItemStack stack = user.getMainHandItem();
        Optional<Vec3> resultPos = BladeStateAccess.of(stack)
            .filter(s -> s.getTargetEntity(worldIn) != null)
            .map(s -> s.getTargetEntity(worldIn))
            .map(target -> target.getEyePosition(1.0f));
        
        if (resultPos.isEmpty()) {
            Optional<HitResult> raytraceresult = RayTraceHelper.rayTrace(worldIn, user, eyePos, user.getLookAngle(),
                airReach, entityReach, (entity) -> !entity.isSpectator() && entity.isAlive() && entity.isPickable() && (!entity.equals(user)));
            
            resultPos = raytraceresult.map((rtr) -> {
                Vec3 pos = null;
                HitResult.Type type = rtr.getType();
                switch (type) {
                    case ENTITY:
                        Entity target = ((EntityHitResult) rtr).getEntity();
                        pos = target.position().add(0, target.getEyeHeight() / 2.0f, 0);
                        break;
                    case BLOCK:
                        pos = rtr.getLocation();
                        break;
                    default:
                        break;
                }
                return pos;
            });
        }
        
        Vec3 pos = resultPos.orElseGet(() -> eyePos.add(user.getLookAngle().scale(airReach)));
        EntityJudgementCut jc = new EntityJudgementCut(RegistryEvents.JudgementCut, worldIn);
        jc.setPos(pos.x, pos.y, pos.z);
        jc.setOwner(user);
        BladeStateAccess.of(stack).ifPresent((state) -> jc.setColor(state.getColorCode()));
        
        jc.setRank(user.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRankLevel(worldIn.getGameTime()));
        
        worldIn.addFreshEntity(jc);
        
        worldIn.playSound(null, jc.getX(), jc.getY(), jc.getZ(), SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS, 0.5F, 0.8F / (user.getRandom().nextFloat() * 0.4F + 0.8F));
        
        return jc;
    }
    
    public static void doJudgementCutSuper(LivingEntity owner) {
        doJudgementCutSuper(owner, null);
    }
    
    public static void doJudgementCutSuper(LivingEntity owner, @Nullable List<Entity> exclude) {
        Level level = owner.level();
        ItemStack stack = owner.getMainHandItem();
        
        List<Entity> founds = TargetSelector.getTargettableEntitiesWithinAABB(level, owner,
            owner.getBoundingBox().inflate(48.0D), TargetSelector.getResolvedReach(owner) + 32D);
        if (exclude != null) {
            founds.removeAll(exclude);
        }
        for (Entity entity : founds) {
            if (entity instanceof LivingEntity) {
                
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10));
                EntityJudgementCut judgementCut = new EntityJudgementCut(RegistryEvents.JudgementCut, level);
                judgementCut.setPos(entity.getX(), entity.getY(), entity.getZ());
                judgementCut.setOwner(owner);
                BladeStateAccess.of(stack)
                    .ifPresent(state -> judgementCut.setColor(state.getColorCode()));
                judgementCut.setRank(owner.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRankLevel(level.getGameTime()));
                level.addFreshEntity(judgementCut);
            }
        }
        
        level.playSound(owner, owner.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
