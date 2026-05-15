package mods.flammpfeil.slashblade.event.drop;

import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class EntityDropEvent {
    @SubscribeEvent
    public static void dropBlade(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        
        var bladeRegistry = SlashBlade.getSlashBladeDefinitionRegistry(entity.level());
        entity.level().registryAccess().registryOrThrow(EntityDropEntry.REGISTRY_KEY).forEach(entry -> {
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entry.entityType())) {
                return;
            }
            if (!bladeRegistry.containsKey(entry.bladeName())) {
                return;
            }
            
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
                return;
            }
            
            if (SlashBladeConfig.FRIENDLY_ENABLE.get() || (entity instanceof Enemy)) {
                if (entry.requestSlashBladeKill()
                    && !(attacker.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
                    return;
                }
            }
            
            int lootingLevel = 0;
            if (event.getSource().getEntity() instanceof LivingEntity lootAttacker) {
                var enchLookup = lootAttacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                lootingLevel = EnchantmentHelper.getEnchantmentLevel(enchLookup.getOrThrow(Enchantments.LOOTING), lootAttacker);
            }
            float resultRate = Math.min(1F, entry.dropRate() + lootingLevel * 0.1F);
            SlashBladeDefinition slashBladeDefinition = bladeRegistry.get(entry.bladeName());
            if (slashBladeDefinition != null) {
                final ItemStack blade = slashBladeDefinition.getBlade(event.getEntity().registryAccess());
                if (entry.dropFixedPoint()) {
                    dropBlade(entity, BuiltInRegistries.ENTITY_TYPE.get(entry.entityType()),
                        blade, resultRate, entry.dropPoint().x,
                        entry.dropPoint().y, entry.dropPoint().z);
                } else {
                    dropBlade(entity, BuiltInRegistries.ENTITY_TYPE.get(entry.entityType()),
                        blade, resultRate, entity.getX(), entity.getY(),
                        entity.getZ());
                }
            }
        });
        
    }
    
    public static void dropBlade(LivingEntity entity, EntityType<?> type, ItemStack blade, float percent, double x,
                                 double y, double z) {
        if (entity.getType().equals(type)) {
            var rand = entity.level().getRandom();
            
            if (rand.nextFloat() > percent) {
                return;
            }
            ItemEntity itementity = new ItemEntity(entity.level(), x, y, z, blade);
            BladeItemEntity e = new BladeItemEntity(RegistryEvents.BladeItem, entity.level());
            
            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);
            
            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);
            
            e.setAirSupply(-1);
            
            e.setThrower(entity);
            
            entity.level().addFreshEntity(e);
        }
    }
}
