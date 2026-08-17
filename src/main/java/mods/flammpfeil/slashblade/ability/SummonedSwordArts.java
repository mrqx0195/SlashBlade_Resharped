package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SummonedSwordArts {
    private static final class SingletonHolder {
        private static final SummonedSwordArts instance = new SummonedSwordArts();
    }
    
    public static SummonedSwordArts getInstance() {
        return SummonedSwordArts.SingletonHolder.instance;
    }
    
    private SummonedSwordArts() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public static final ResourceLocation ADVANCEMENT_SUMMONEDSWORDS = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "arts/shooting/summonedswords");
    public static final ResourceLocation ADVANCEMENT_SPIRAL_SWORDS = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "arts/shooting/spiral_swords");
    public static final ResourceLocation ADVANCEMENT_STORM_SWORDS = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "arts/shooting/storm_swords");
    public static final ResourceLocation ADVANCEMENT_BLISTERING_SWORDS = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "arts/shooting/blistering_swords");
    public static final ResourceLocation ADVANCEMENT_HEAVY_RAIN_SWORDS = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "arts/shooting/heavy_rain_swords");
    
    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {
        
        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayer sender = event.getEntity();
        
        ItemStack blade = sender.getMainHandItem();
        ISlashBladeState bladeState = BladeStateAccess.of(blade).orElseThrow();
        
        if (bladeState.isBroken() || bladeState.isSealed()
            || !SwordType.from(blade).contains(SwordType.BEWITCHED)) {
            return;
        }
        
        int powerLevel = blade
            .getEnchantmentLevel(sender.level().registryAccess().holderOrThrow(Enchantments.POWER));
        if (powerLevel <= 0) {
            return;
        }
        
        InputCommand targetCommnad = InputCommand.M_DOWN;
        
        
        boolean onDown = !old.contains(targetCommnad) && current.contains(targetCommnad);
        
        final long pressTime = event.getState().getLastPressTime(targetCommnad);
        
        // basic summoned swords
        if (onDown) {
            
            IInputState input = sender.getData(CapabilityInputState.INPUT_STATE.get());
            // SpiralSwords command
            input.getScheduler().schedule("SpiralSwords", pressTime + 10, (rawEntity, queue, now) -> performSpiralSwords(powerLevel, pressTime, rawEntity));
            
            // StormSwords command
            input.getScheduler().schedule("StormSwords", pressTime + 10, (rawEntity, queue, now) -> performStormSwords(powerLevel, pressTime, rawEntity));
            
            // BlisteringSwords command
            input.getScheduler().schedule("BlisteringSwords", pressTime + 10, (rawEntity, queue, now) -> performBlisteringSwords(powerLevel, pressTime, rawEntity, now));
            
            input.getScheduler().schedule("HeavyRainSwords", pressTime + 10, (rawEntity, queue, now) -> performHeavyRains(powerLevel, pressTime, rawEntity, now));
            
            BladeStateAccess.of(blade).ifPresent((state) -> {
                
                if (state.getProudSoulCount() < SlashBladeConfig.SUMMON_SWORD_COST.get()) {
                    return;
                }
                state.setProudSoulCount(state.getProudSoulCount() - SlashBladeConfig.SUMMON_SWORD_COST.get());
                //幻影剑
                AdvancementHelper.grantCriterion(sender, ADVANCEMENT_SUMMONEDSWORDS);
                
                Optional<Entity> foundTarget = findTarget(sender, state.getTargetEntity(sender.level()));
                
                Level worldIn = sender.level();
                Vec3 targetPos = foundTarget.map((e) -> new Vec3(e.getX(), e.getY() + e.getEyeHeight() * 0.5, e.getZ()))
                    .orElseGet(() -> {
                        Vec3 start = sender.getEyePosition(1.0f);
                        Vec3 end = start.add(sender.getLookAngle().scale(40));
                        HitResult result = worldIn.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE, sender));
                        return result.getLocation();
                    });
                
                int counter = StatHelper.increase(sender, RegistryEvents.SWORD_SUMMONED, 1);
                boolean sided = counter % 2 == 0;
                
                EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(
                    RegistryEvents.SummonedSword, worldIn);
                
                Vec3 pos = sender.getEyePosition(1.0f)
                    .add(VectorHelper.getVectorForRotation(0.0f, sender.getViewYRot(0) + 90).scale(sided ? 1 : -1));
                ss.setPos(pos.x, pos.y, pos.z);
                ss.setDamage(powerLevel);
                Vec3 dir = targetPos.subtract(pos).normalize();
                ss.shoot(dir.x, dir.y, dir.z, 3.0f, 0.0f);
                // ss.setDamage(counter);
                ss.setOwner(sender);
                ss.setColor(state.getColorCode());
                ss.setRoll(sender.getRandom().nextFloat() * 360.0f);
                worldIn.addFreshEntity(ss);
                
                sender.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
            });
        }
    }
    
    public Optional<Entity> findTarget(ServerPlayer sender, @Nullable Entity lockedT) {
        return Stream.of(Optional.ofNullable(lockedT),
                RayTraceHelper
                    .rayTrace(sender.level(), sender, sender.getEyePosition(1.0f), sender.getLookAngle(),
                        12, 12, (e) -> true)
                    .filter(r -> r.getType() == HitResult.Type.ENTITY).filter(r -> {
                        EntityHitResult er = (EntityHitResult) r;
                        Entity target = er.getEntity();
                        
                        boolean isMatch = true;
                        if (target instanceof LivingEntity) {
                            isMatch = TargetSelector.lockon.test(sender, (LivingEntity) target);
                        }
                        
                        if (target instanceof IShootable) {
                            isMatch = !Objects.equals(((IShootable) target).getShooter(), sender);
                        }
                        
                        return isMatch;
                    }).map(r -> ((EntityHitResult) r).getEntity()))
            .filter(Optional::isPresent).map(Optional::get).findFirst();
    }
    
    Vec3 calculateViewVector(float x, float y) {
        return VectorHelper.getVectorForRotation(x, y);
    }
    
    private void performSpiralSwords(int powerLevel, final Long pressTime, LivingEntity rawEntity) {
        if (!(rawEntity instanceof ServerPlayer entity)) {
            return;
        }
        
        InputCommand targetCommnad = InputCommand.M_DOWN;
        IInputState capInput = entity.getData(CapabilityInputState.INPUT_STATE.get());
        boolean inputSucceed = capInput.getCommands().contains(targetCommnad)
            && (!InputCommand.anyMatch(capInput.getCommands(), InputCommand.move)
            || !capInput.getCommands().contains(InputCommand.SNEAK))
            && capInput.getLastPressTime(targetCommnad) == pressTime;
        
        if (!inputSucceed) {
            return;
        }
        
        // spiralSwords
        boolean alreadySummoned = entity.getPassengers().stream()
            .anyMatch(e -> e instanceof EntitySpiralSwords);
        
        if (alreadySummoned) {
            // fire
            List<Entity> list = entity.getPassengers().stream()
                .filter(e -> e instanceof EntitySpiralSwords).toList();
            
            list.forEach(e -> ((EntitySpiralSwords) e).doFire());
        } else {
            // summon
            BladeStateAccess.of(entity.getMainHandItem()).ifPresent((state) -> {
                
                if (state.getProudSoulCount() < SlashBladeConfig.SUMMON_SWORD_ART_COST.get()) {
                    return;
                }
                state.setProudSoulCount(
                    state.getProudSoulCount() - SlashBladeConfig.SUMMON_SWORD_ART_COST.get());
                
                //圆环幻影剑
                AdvancementHelper.grantCriterion(entity, ADVANCEMENT_SPIRAL_SWORDS);
                
                Level worldIn = entity.level();
                
                IConcentrationRank.ConcentrationRanks ranks = entity.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRank(worldIn.getGameTime());
                int rank = 0;
                if (ranks != null) {
                    rank = ranks.level;
                }
                
                int count = 6;
                
                if (IConcentrationRank.ConcentrationRanks.S.level <= rank) {
                    count = 8;
                }
                
                for (int i = 0; i < count; i++) {
                    EntitySpiralSwords ss = new EntitySpiralSwords(
                        RegistryEvents.SpiralSwords, worldIn);
                    ss.setPos(entity.position());
                    ss.setOwner(entity);
                    ss.setColor(state.getColorCode());
                    ss.setRoll(0);
                    ss.setDamage(powerLevel);
                    // force riding
                    ss.startRiding(entity, true);
                    
                    ss.setDelay(360 / count * i);
                    
                    worldIn.addFreshEntity(ss);
                    
                    entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F,
                        1.45F);
                }
            });
        }
    }
    
    private void performStormSwords(int powerLevel, final Long pressTime, LivingEntity rawEntity) {
        if (!(rawEntity instanceof ServerPlayer entity)) {
            return;
        }
        
        InputCommand targetCommnad = InputCommand.M_DOWN;
        IInputState capInput2 = entity.getData(CapabilityInputState.INPUT_STATE.get());
        boolean inputSucceed = capInput2.getCommands().contains(targetCommnad)
            && capInput2.getCommands().contains(InputCommand.SNEAK)
            && capInput2.getCommands().contains(InputCommand.BACK)
            && !capInput2.getCommands().contains(InputCommand.FORWARD)
            && capInput2.getLastPressTime(targetCommnad) == pressTime;
        if (!inputSucceed) {
            return;
        }
        
        // summon
        BladeStateAccess.of(entity.getMainHandItem()).ifPresent((state) -> {
            
            Level worldIn = entity.level();
            Entity target = state.getTargetEntity(worldIn);
            
            if (target == null || !target.isAlive() || target.isRemoved()) {
                return;
            }
            if (state.getProudSoulCount() < SlashBladeConfig.SUMMON_SWORD_ART_COST.get()) {
                return;
            }
            state.setProudSoulCount(
                state.getProudSoulCount() - SlashBladeConfig.SUMMON_SWORD_ART_COST.get());
            //烈风环影剑
            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_STORM_SWORDS);
            
            IConcentrationRank.ConcentrationRanks ranks = entity.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRank(worldIn.getGameTime());
            int rank = 0;
            if (ranks != null) {
                rank = ranks.level;
            }
            
            int count = 6;
            
            if (IConcentrationRank.ConcentrationRanks.S.level <= rank) {
                count = 8;
            }
            
            for (int i = 0; i < count; i++) {
                EntityStormSwords ss = new EntityStormSwords(RegistryEvents.StormSwords,
                    worldIn);
                
                ss.setPos(entity.position());
                ss.setOwner(entity);
                ss.setColor(state.getColorCode());
                ss.setRoll(0);
                ss.setDamage(powerLevel);
                // force riding
                ss.startRiding(target, true);
                ss.setDelay(360 / count * i);
                worldIn.addFreshEntity(ss);
                
                entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F,
                    1.45F);
            }
        });
    }
    
    private void performBlisteringSwords(int powerLevel, final Long pressTime, LivingEntity rawEntity, long now) {
        if (!(rawEntity instanceof ServerPlayer entity)) {
            return;
        }
        
        InputCommand targetCommnad = InputCommand.M_DOWN;
        IInputState inputState = entity.getData(CapabilityInputState.INPUT_STATE.get());
        boolean inputSucceed = inputState.getCommands().contains(targetCommnad)
            && inputState.getCommands().contains(InputCommand.SNEAK)
            && inputState.getCommands().contains(InputCommand.FORWARD)
            && inputState.getLastPressTime(InputCommand.BACK) + 20 < now
            && inputState.getLastPressTime(targetCommnad) == pressTime;
        if (!inputSucceed) {
            return;
        }
        
        // summon
        BladeStateAccess.of(entity.getMainHandItem()).ifPresent((state) -> {
            
            Level worldIn = entity.level();
            
            if (state.getProudSoulCount() < SlashBladeConfig.SUMMON_SWORD_ART_COST.get()) {
                return;
            }
            state.setProudSoulCount(
                state.getProudSoulCount() - SlashBladeConfig.SUMMON_SWORD_ART_COST.get());
            //急袭幻影剑
            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_BLISTERING_SWORDS);
            
            IConcentrationRank.ConcentrationRanks ranks = entity.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRank(worldIn.getGameTime());
            int rank = 0;
            if (ranks != null) {
                rank = ranks.level;
            }
            
            int count = 6;
            
            if (IConcentrationRank.ConcentrationRanks.S.level <= rank) {
                count = 8;
            }
            
            for (int i = 0; i < count; i++) {
                EntityBlisteringSwords ss = new EntityBlisteringSwords(
                    RegistryEvents.BlisteringSwords, worldIn);
                
                ss.setPos(entity.position());
                ss.setOwner(entity);
                ss.setColor(state.getColorCode());
                ss.setRoll(0);
                ss.setDamage(powerLevel);
                // force riding
                ss.startRiding(entity, true);
                
                ss.setDelay(i);
                
                worldIn.addFreshEntity(ss);
                
                entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F,
                    1.45F);
            }
        });
    }
    
    private void performHeavyRains(int powerLevel, final Long pressTime, LivingEntity rawEntity, long now) {
        if (!(rawEntity instanceof ServerPlayer entity)) {
            return;
        }
        
        InputCommand targetCommnad = InputCommand.M_DOWN;
        IInputState inputState = entity.getData(CapabilityInputState.INPUT_STATE.get());
        boolean inputSucceed = inputState.getCommands().contains(targetCommnad)
            && inputState.getCommands().contains(InputCommand.SNEAK)
            && inputState.getCommands().contains(InputCommand.FORWARD)
            && inputState.getLastPressTime(InputCommand.BACK) + 30 > now
            && inputState.getLastPressTime(targetCommnad) == pressTime;
        if (!inputSucceed) {
            return;
        }
        
        // summon
        BladeStateAccess.of(entity.getMainHandItem()).ifPresent((state) -> {
            
            Level worldIn = entity.level();
            Entity target = state.getTargetEntity(worldIn);
            if (state.getProudSoulCount() < SlashBladeConfig.SUMMON_SWORD_ART_COST.get()) {
                return;
            }
            state.setProudSoulCount(
                state.getProudSoulCount() - SlashBladeConfig.SUMMON_SWORD_ART_COST.get());
            
            //五月雨
            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_HEAVY_RAIN_SWORDS);
            
            IConcentrationRank.ConcentrationRanks ranks = entity.getData(CapabilityConcentrationRank.RANK_POINT.get()).getRank(worldIn.getGameTime());
            int rank = 0;
            if (ranks != null) {
                rank = ranks.level;
            }
            
            Vec3 basePos;
            
            if (target != null) {
                basePos = target.position();
            } else {
                Vec3 forwardDir = calculateViewVector(0, entity.getYRot());
                basePos = entity.getPosition(0).add(forwardDir.scale(5));
            }
            
            float yOffset = 7;
            basePos = basePos.add(0, yOffset, 0);
            
            {// no random pos
                EntityHeavyRainSwords ss = new EntityHeavyRainSwords(
                    RegistryEvents.HeavyRainSwords, worldIn);
                
                ss.setOwner(entity);
                ss.setColor(state.getColorCode());
                ss.setRoll(0);
                ss.setDamage(powerLevel);
                // force riding
                ss.startRiding(entity, true);
                
                ss.setDelay(0);
                
                ss.setPos(basePos);
                
                ss.setXRot(-90);
                
                worldIn.addFreshEntity(ss);
            }
            
            int count = 9 + Math.min(rank - 1, 0);
            int multiplier = 2;
            for (int i = 0; i < count; i++) {
                for (int l = 0; l < multiplier; l++) {
                    EntityHeavyRainSwords ss = new EntityHeavyRainSwords(
                        RegistryEvents.HeavyRainSwords, worldIn);
                    
                    ss.setOwner(entity);
                    ss.setColor(state.getColorCode());
                    ss.setRoll(0);
                    ss.setDamage(powerLevel);
                    // force riding
                    ss.startRiding(entity, true);
                    
                    ss.setDelay(i);
                    
                    ss.setSpread(basePos);
                    
                    ss.setXRot(-90);
                    
                    worldIn.addFreshEntity(ss);
                    
                    entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F,
                        1.45F);
                }
            }
        });
    }
}
