package mods.flammpfeil.slashblade.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class BladeMotionEvent extends Event implements ICancellableEvent {
    private final LivingEntity entity;
    private ResourceLocation combo;
    private long actionTime;
    
    public BladeMotionEvent(LivingEntity entity, ResourceLocation combo) {
        this(entity, combo, entity.level().getGameTime());
    }
    
    public BladeMotionEvent(LivingEntity entity, ResourceLocation combo, long actionTime) {
        this.entity = entity;
        this.combo = combo;
        this.actionTime = actionTime;
    }
    
    public LivingEntity getEntity() {
        return entity;
    }
    
    public ResourceLocation getCombo() {
        return this.combo;
    }
    
    public void setCombo(ResourceLocation combo) {
        this.combo = combo;
    }
    
    public long getActionTime() {
        return this.actionTime;
    }
    
    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }
    
}
