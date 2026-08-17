package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BladeRuntimeSyncer {
    private static final class SingletonHolder {
        private static final BladeRuntimeSyncer instance = new BladeRuntimeSyncer();
    }
    
    public static BladeRuntimeSyncer getInstance() {
        return SingletonHolder.instance;
    }
    
    private BladeRuntimeSyncer() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onBladeMotion(BladeMotionEvent event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof ItemSlashBlade)) {
            return;
        }
        
        BladeStateAccess.of(event.getEntity().getMainHandItem()).ifPresent(state -> {
            state.setComboSeq(event.getCombo());
            state.setLastActionTime(event.getActionTime());
            event.getEntity().getPersistentData().remove(ComboState.LAST_PROCESSED_TICK_KEY);
        });
    }
}
