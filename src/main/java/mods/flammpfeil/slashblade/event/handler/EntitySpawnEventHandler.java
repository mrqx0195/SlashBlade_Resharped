package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class EntitySpawnEventHandler {
    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        boolean isZombie = isZombie(entity);
        if (!isZombie) {
            return;
        }
        if (!entity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return;
        }
        
        RandomSource random = event.getLevel().getRandom();
        float difficultyMultiplier = event.getDifficulty().getSpecialMultiplier();
        
        Registry<SlashBladeDefinition> bladeRegistry = SlashBlade
            .getSlashBladeDefinitionRegistry(event.getEntity().level());
        if (!bladeRegistry.containsKey(SlashBladeBuiltInRegistry.SABIGATANA.location())) {
            return;
        }
        
        float rngResult = random.nextFloat();
        
        if (rngResult < SlashBladeConfig.BROKEN_SABIGATANA_SPAWN_CHANCE.get() * difficultyMultiplier) {
            SlashBladeDefinition slashBladeDefinition;
            if (rngResult < SlashBladeConfig.SABIGATANA_SPAWN_CHANCE.get() * difficultyMultiplier) {
                slashBladeDefinition = bladeRegistry.get(SlashBladeBuiltInRegistry.SABIGATANA.location());
            } else {
                slashBladeDefinition = bladeRegistry.get(SlashBladeBuiltInRegistry.SABIGATANA_BROKEN.location());
            }
            if (slashBladeDefinition != null) {
                entity.setItemSlot(EquipmentSlot.MAINHAND, slashBladeDefinition.getBlade(entity.registryAccess()));
            }
        }
    }
    
    private static boolean isZombie(LivingEntity entity) {
        return entity instanceof Zombie && !(entity instanceof Drowned) && !(entity instanceof ZombifiedPiglin);
    }
}
