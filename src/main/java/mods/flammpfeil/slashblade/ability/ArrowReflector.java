package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.TargetSelector;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class ArrowReflector {
    
    static public boolean isMatch(Entity arrow, Entity attacker) {
        if (arrow == null) {
            return false;
        }
        return arrow instanceof Projectile;
    }
    
    static public void doReflect(Entity arrow, Entity attacker) {
        if (!isMatch(arrow, attacker)) {
            return;
        }
        
        arrow.hurtMarked = true;
        if (attacker != null) {
            Vec3 dir = attacker.getLookAngle();
            
            do {
                if (!(attacker instanceof LivingEntity living)) {
                    break;
                }
                
                ItemStack stack = living.getMainHandItem();
                
                if (stack.isEmpty()) {
                    break;
                }
                if (!(stack.getItem() instanceof ItemSlashBlade)) {
                    break;
                }
                
                Entity target = BladeStateAccess.of(stack)
                    .map(s -> s.getTargetEntity(living.level()))
                    .orElse(null);
                if (target != null) {
                    dir = target.getEyePosition(1.0f).subtract(arrow.position()).normalize();
                } else {
                    dir = living.getEyePosition(1.0f).add(living.getLookAngle().scale(10))
                        .subtract(arrow.position()).normalize();
                }
                
            } while (false);
            
            ((Projectile) arrow).shoot(dir.x, dir.y, dir.z, 3.5f, 0.2f);
            
            if (arrow instanceof AbstractArrow) {
                ((AbstractArrow) arrow).setCritArrow(true);
            }
            
        }
    }
    
    static public void doTicks(LivingEntity attacker) {
        
        ItemStack stack = attacker.getMainHandItem();
        
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        
        BladeStateAccess.of(stack).ifPresent(s -> {
            int ticks = attacker.getTicksUsingItem();
            
            if (ticks == 0) {
                return;
            }
            
            ResourceLocation old = s.getComboSeq();
            ResourceLocation current = s.resolvCurrentComboState(attacker);
            // ComboStateRegistry access: REGISTRY is a vanilla Registry<ComboState>
            ComboState currentCS = ComboStateRegistry.REGISTRY.get(current);
            if (currentCS == null) {
                currentCS = ComboStateRegistry.NONE.get();
            }
            if (!old.equals(current)) {
                ComboState oldCS = ComboStateRegistry.REGISTRY.get(old);
                if (oldCS != null) {
                    ticks -= (int) TimeValueHelper.getTicksFromMSec(oldCS.getTimeoutMS());
                }
            }
            
            double period = TimeValueHelper.getTicksFromFrames(currentCS.getEndFrame() - currentCS.getStartFrame())
                * (1.0f / currentCS.getSpeed());
            
            if (ticks < period) {
                List<Entity> founds = TargetSelector.getReflectableEntitiesWithinAABB(attacker);
                
                founds.stream().filter(e -> (e instanceof Projectile) && !Objects.equals(((Projectile) e).getOwner(), attacker))
                    .forEach(e -> doReflect(e, attacker));
            }
        });
        
    }
    
}
