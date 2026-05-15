package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.mobeffect.IMobEffectState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.stream.Collectors;

public class Untouchable {
    private static final class SingletonHolder {
        private static final Untouchable instance = new Untouchable();
    }
    
    public static Untouchable getInstance() {
        return Untouchable.SingletonHolder.instance;
    }
    
    private Untouchable() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public static void setUntouchable(LivingEntity entity, int ticks) {
        IMobEffectState effect = entity.getData(CapabilityMobEffect.MOB_EFFECT.get());
        effect.setManagedUntouchable(entity.level().getGameTime(), ticks);
        effect.storeEffects(entity.getActiveEffectsMap().keySet().stream().map(Holder::value).collect(Collectors.toSet()));
        effect.storeHealth(entity.getHealth());
    }
    
    private boolean checkUntouchable(LivingEntity entity) {
        return entity.getData(CapabilityMobEffect.MOB_EFFECT.get()).isUntouchable(entity.level().getGameTime());
    }
    
    private void doWitchTime(Entity entity) {
        if (entity == null) {
            return;
        }
        
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        
        StunManager.setStun((LivingEntity) entity);
    }
    
    public boolean doUntouchable(LivingEntity self, Entity other) {
        if (checkUntouchable(self)) {
            doWitchTime(other);
            return true;
        }
        return false;
    }
    
    @SubscribeEvent
    public void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (doUntouchable(event.getEntity(), event.getSource().getEntity())) {
            event.setNewDamage(0);
        }
    }
    
    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        if (doUntouchable(event.getEntity(), event.getSource().getEntity())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (doUntouchable(entity, event.getSource().getEntity())) {
            event.setCanceled(true);
            
            IMobEffectState effect = entity.getData(CapabilityMobEffect.MOB_EFFECT.get());
            if (effect.hasUntouchableWorked()) {
                List<Holder<MobEffect>> filterd = entity.getActiveEffectsMap().keySet().stream()
                    .filter(p -> !(effect.getEffectSet().contains(p.value()) || p.value().isBeneficial())).toList();
                
                filterd.forEach(entity::removeEffect);
                
                float storedHealth = effect.getStoredHealth();
                if (entity.getHealth() < storedHealth) {
                    entity.setHealth(storedHealth);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingTicks(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        
        if (entity.level().isClientSide()) {
            return;
        }
        
        IMobEffectState effect = entity.getData(CapabilityMobEffect.MOB_EFFECT.get());
        if (effect.hasUntouchableWorked()) {
            effect.setUntouchableWorked(false);
            List<Holder<MobEffect>> filterd = entity.getActiveEffectsMap().keySet().stream()
                .filter(p -> !(effect.getEffectSet().contains(p.value()) || p.value().isBeneficial())).toList();
            
            filterd.forEach(entity::removeEffect);
            
            float storedHealth = effect.getStoredHealth();
            if (entity.getHealth() < storedHealth) {
                entity.setHealth(storedHealth);
            }
        }
    }
    
    final static int JUMP_TICKS = 10;
    
    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (BladeStateAccess.of(event.getEntity().getMainHandItem()).isEmpty()) {
            return;
        }
        
        Untouchable.setUntouchable(event.getEntity(), JUMP_TICKS);
    }
}
