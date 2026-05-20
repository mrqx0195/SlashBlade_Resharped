package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class RankPointHandler {
    private static final class SingletonHolder {
        private static final RankPointHandler instance = new RankPointHandler();
    }
    
    public static RankPointHandler getInstance() {
        return SingletonHolder.instance;
    }
    
    private RankPointHandler() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    /**
     * Not reached if canceled.
     *
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurtEvent(LivingDamageEvent.Pre event) {
        
        LivingEntity victim = event.getEntity();
        IConcentrationRank victimRank = victim.getData(CapabilityConcentrationRank.RANK_POINT.get());
        victimRank.addRankPoint(victim, -victimRank.getUnitCapacity());
        
        Entity trueSource = event.getSource().getEntity();
        if (!(trueSource instanceof LivingEntity sourceEntity)) {
            return;
        }
        
        if (BladeStateAccess.of(sourceEntity.getMainHandItem()).isEmpty()) {
            return;
        }
        
        trueSource.getData(CapabilityConcentrationRank.RANK_POINT.get()).addRankPoint(event.getSource());
    }
}
