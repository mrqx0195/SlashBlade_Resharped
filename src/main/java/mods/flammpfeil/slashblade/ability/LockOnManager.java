package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class LockOnManager {
    @SubscribeEvent
    public static void onInputChange(InputCommandEvent event) {
        
        
        ServerPlayer player = event.getEntity();
        // set target
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        
        Entity targetEntity;
        
        if (event.getOld().contains(InputCommand.SNEAK) == event.getCurrent().contains(InputCommand.SNEAK)) {
            return;
        }
        
        if ((event.getOld().contains(InputCommand.SNEAK) && !event.getCurrent().contains(InputCommand.SNEAK))) {
            // remove target
            targetEntity = null;
        } else {
            // search target
            
            Optional<HitResult> result = RayTraceHelper.rayTrace(player.level(), player, player.getEyePosition(1.0f),
                player.getLookAngle(), 40, 40, (e) -> true);
            Optional<Entity> foundEntity = result.filter(r -> r.getType() == HitResult.Type.ENTITY).filter(r -> {
                EntityHitResult er = (EntityHitResult) r;
                Entity target = er.getEntity();
                
                if (target instanceof PartEntity) {
                    target = ((PartEntity<?>) target).getParent();
                }
                
                boolean isMatch = false;
                
                if (target instanceof LivingEntity) {
                    isMatch = TargetSelector.lockon.test(player, (LivingEntity) target);
                }
                
                return isMatch;
            }).map(r -> ((EntityHitResult) r).getEntity());
            
            if (foundEntity.isEmpty()) {
                List<LivingEntity> entities = player.level().getNearbyEntities(LivingEntity.class,
                    TargetSelector.lockon, player, player.getBoundingBox().inflate(12.0D, 6.0D, 12.0D));
                
                foundEntity = entities.stream().map(s -> (Entity) s)
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            }
            
            targetEntity = foundEntity.map(e -> (e instanceof PartEntity) ? ((PartEntity<?>) e).getParent() : e)
                .orElse(null);
            
        }
        
        BladeStateAccess.of(stack).ifPresent(s -> s.setTargetEntityId(targetEntity));
        
    }
    
}
