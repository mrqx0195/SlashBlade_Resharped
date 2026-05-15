package mods.flammpfeil.slashblade.event.bladestand;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.data.builtin.SlashBladeBuiltInRegistry;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.SpecialEffectsRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = SlashBlade.MODID)
public class BlandStandEventHandler {
    @SubscribeEvent
    public static void eventKoseki(SlashBladeEvent.BladeStandAttackEvent event) {
        Registry<SlashBladeDefinition> slashBladeDefinitionRegistry = SlashBlade.getSlashBladeDefinitionRegistry(event.getBladeStand().level());
        SlashBladeDefinition slashBladeDefinition = slashBladeDefinitionRegistry.get(SlashBladeBuiltInRegistry.KOSEKI);
        if (slashBladeDefinition == null) {
            return;
        }
        if (!(event.getDamageSource().getEntity() instanceof WitherBoss)) {
            return;
        }
        if (!event.getDamageSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        var in = SlashBladeIngredient.of(RequestDefinition.Builder.newInstance().build());
        if (!in.test(event.getBlade())) {
            return;
        }
        event.getBladeStand().setItem(slashBladeDefinition.getBlade(event.getBladeStand().registryAccess()));
        event.setCanceled(true);
    }
    
    @SubscribeEvent
    public static void eventChangeSE(SlashBladeEvent.BladeStandAttackEvent event) {
        if (!(event.getDamageSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack blade = event.getBlade();
        if (blade.isEmpty()) {
            return;
        }
        if (!stack.is(SlashBladeItemTags.CAN_CHANGE_SE)) {
            return;
        }
        var world = player.level();
        var state = event.getSlashBladeState();
        
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("SpecialEffectType")) {
            var bladeStand = event.getBladeStand();
            ResourceLocation SEKey = ResourceLocation.tryParse(tag.getString("SpecialEffectType"));
            if (SEKey == null) {
                return;
            }
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(SEKey)) {
                return;
            }
            if (state.hasSpecialEffect(SEKey)) {
                return;
            }
            
            BladeChangeSpecialEffectEvent e = new BladeChangeSpecialEffectEvent(
                blade, state, SEKey, event);
            
            if (!player.isCreative()) {
                e.setShrinkCount(1);
            }
            
            NeoForge.EVENT_BUS.post(e);
            if (e.isCanceled()) {
                return;
            }
            
            if (stack.getCount() < e.getShrinkCount()) {
                return;
            }
            
            state.addSpecialEffect(e.getSEKey());
            
            RandomSource random = player.getRandom();
            
            spawnSucceedEffects(world, bladeStand, random);
            
            stack.shrink(e.getShrinkCount());
            
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void eventChangeSA(SlashBladeEvent.BladeStandAttackEvent event) {
        if (!(event.getDamageSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        
        if (!stack.is(SlashBladeItemTags.CAN_CHANGE_SA) || !tag.contains("SpecialAttackType")) {
            return;
        }
        
        ResourceLocation SAKey = ResourceLocation.tryParse(tag.getString("SpecialAttackType"));
        if (SAKey == null) {
            return;
        }
        if (!SlashArtsRegistry.REGISTRY.containsKey(SAKey)) {
            return;
        }
        
        ItemStack blade = event.getBlade();
        
        BladeStateAccess.of(blade).ifPresent(state -> {
            if (!SAKey.equals(state.getSlashArtsKey())) {
                
                BladeChangeSpecialAttackEvent e = new BladeChangeSpecialAttackEvent(
                    blade, state, SAKey, event);
                
                if (!player.isCreative()) {
                    e.setShrinkCount(1);
                }
                
                NeoForge.EVENT_BUS.post(e);
                if (e.isCanceled()) {
                    event.setCanceled(true);
                    return;
                }
                
                if (stack.getCount() < e.getShrinkCount()) {
                    return;
                }
                
                state.setSlashArtsKey(e.getSAKey());
                
                RandomSource random = player.getRandom();
                BladeStandEntity bladeStand = event.getBladeStand();
                
                spawnSucceedEffects(player.level(), bladeStand, random);
                
                stack.shrink(e.getShrinkCount());
            }
        });
        event.setCanceled(true);
    }
    
    @SubscribeEvent
    public static void eventCopySE(SlashBladeEvent.BladeStandAttackEvent event) {
        if (!(event.getDamageSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack blade = event.getBlade();
        if (blade.isEmpty()) {
            return;
        }
        if (!stack.is(SlashBladeItemTags.CAN_COPY_SE)) {
            return;
        }
        
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("SpecialEffectType")) {
            return;
        }
        
        var world = player.level();
        
        if (world.isClientSide()) {
            return;
        }
        
        var state = event.getSlashBladeState();
        var bladeStand = event.getBladeStand();
        var specialEffects = state.getSpecialEffects();
        
        for (var se : specialEffects) {
            if (!SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
                continue;
            }
            
            var specialEffect = SpecialEffectsRegistry.REGISTRY.get(se);
            
            if (specialEffect == null) {
                return;
            }
            
            PreCopySpecialEffectFromBladeEvent pe = new PreCopySpecialEffectFromBladeEvent(
                blade, state, se, event, specialEffect.isRemovable(), specialEffect.isCopiable());
            
            if (!player.isCreative()) {
                pe.setShrinkCount(1);
            }
            
            NeoForge.EVENT_BUS.post(pe);
            if (pe.isCanceled()) {
                return;
            }
            
            if (stack.getCount() < pe.getShrinkCount()) {
                continue;
            }
            
            if (!pe.isCopiable()) {
                continue;
            }
            
            ItemStack orb = new ItemStack(SlashBladeItems.PROUDSOUL_CRYSTAL.get());
            CompoundTag tag = new CompoundTag();
            tag.putString("SpecialEffectType", se.toString());
            orb.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            
            stack.shrink(pe.getShrinkCount());
            
            RandomSource random = player.getRandom();
            
            spawnSucceedEffects(world, bladeStand, random);
            
            ItemEntity itemEntity = player.drop(orb, true);
            
            if (pe.isRemovable()) {
                state.removeSpecialEffect(se);
            }
            
            CopySpecialEffectFromBladeEvent e = new CopySpecialEffectFromBladeEvent(
                pe, orb, itemEntity);
            
            NeoForge.EVENT_BUS.post(e);
            
            event.setCanceled(true);
            return;
        }
    }
    
    @SubscribeEvent
    public static void eventCopySA(SlashBladeEvent.BladeStandAttackEvent event) {
        if (!(event.getDamageSource().getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack blade = event.getBlade();
        if (blade.isEmpty()) {
            return;
        }
        if (!stack.is(SlashBladeItemTags.CAN_COPY_SA) || !stack.isEnchanted()) {
            return;
        }
        var world = player.level();
        
        if (world.isClientSide()) {
            return;
        }
        
        var state = event.getSlashBladeState();
        var bladeStand = event.getBladeStand();
        ResourceLocation SA = state.getSlashArtsKey();
        if (SA != null && !SA.equals(SlashArtsRegistry.NONE.getId())) {
            
            PreCopySpecialAttackFromBladeEvent pe = new PreCopySpecialAttackFromBladeEvent(
                blade, state, SA, event);
            
            if (!player.isCreative()) {
                pe.setShrinkCount(1);
            }
            
            NeoForge.EVENT_BUS.post(pe);
            if (pe.isCanceled()) {
                return;
            }
            
            if (stack.getCount() < pe.getShrinkCount()) {
                return;
            }
            
            ItemStack orb = new ItemStack(SlashBladeItems.PROUDSOUL_SPHERE.get());
            CompoundTag tag = new CompoundTag();
            tag.putString("SpecialAttackType", state.getSlashArtsKey().toString());
            orb.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            
            stack.shrink(pe.getShrinkCount());
            
            RandomSource random = player.getRandom();
            
            spawnSucceedEffects(world, bladeStand, random);
            
            ItemEntity itemEntity = player.drop(orb, true);
            
            CopySpecialAttackFromBladeEvent e = new CopySpecialAttackFromBladeEvent(
                pe, orb, itemEntity);
            
            NeoForge.EVENT_BUS.post(e);
            
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void eventProudSoulEnchantment(SlashBladeEvent.BladeStandAttackEvent event) {
        if (!(event.getDamageSource().getEntity() instanceof Player player)) {
            return;
        }
        
        if (player.level().isClientSide()) {
            return;
        }
        
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack blade = event.getBlade();
        
        if (blade.isEmpty()) {
            return;
        }
        
        if (!stack.is(SlashBladeItemTags.PROUD_SOULS)) {
            return;
        }
        
        if (!stack.isEnchanted()) {
            return;
        }
        var world = player.level();
        var random = world.getRandom();
        var bladeStand = event.getBladeStand();
        var enchantmentLookup = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(blade));
        boolean[] hasEnchantmentChanges = {false};
        
        AtomicInteger totalShrinkCount = new AtomicInteger(0);
        if (!player.isCreative()) {
            totalShrinkCount.set(1);
        }
        for (var entry : stack.getAllEnchantments(enchantmentLookup).entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (event.isCanceled()) {
                continue;
            }
            if (!blade.supportsEnchantment(enchantment)) {
                continue;
            }
            
            var probability = 1.0F;
            if (stack.is(SlashBladeItems.PROUDSOUL_TINY.get())) {
                probability = 0.25F;
            }
            if (stack.is(SlashBladeItems.PROUDSOUL.get())) {
                probability = 0.5F;
            }
            if (stack.is(SlashBladeItems.PROUDSOUL_INGOT.get())) {
                probability = 0.75F;
            }
            
            int enchantLevel = Math.min(enchantment.value().getMaxLevel(),
                EnchantmentHelper.getTagEnchantmentLevel(enchantment, blade) + 1);
            
            ProudSoulEnchantmentEvent e = new ProudSoulEnchantmentEvent(
                blade, event.getSlashBladeState(), enchantment, enchantLevel, false, probability,
                totalShrinkCount.get(), event);
            
            NeoForge.EVENT_BUS.post(e);
            if (e.isCanceled()) {
                event.setCanceled(true);
                return;
            }
            
            totalShrinkCount.set(e.getTotalShrinkCount());
            
            enchantments.set(e.getEnchantment(), e.getEnchantLevel());
            hasEnchantmentChanges[0] = true;
            
            if (!e.willTryNextEnchant()) {
                event.setCanceled(true);
            }
        }
        
        if (stack.getCount() < totalShrinkCount.get()) {
            event.setCanceled(true);
            return;
        }
        stack.shrink(totalShrinkCount.get());
        
        EnchantmentHelper.setEnchantments(blade, enchantments.toImmutable());
        if (hasEnchantmentChanges[0]) {
            spawnSucceedEffects(world, bladeStand, random);
        }
        
        event.setCanceled(true);
    }
    
    
    @SubscribeEvent
    public static void copySAEnchantmentCheck(PreCopySpecialAttackFromBladeEvent event) {
        SlashBladeEvent.BladeStandAttackEvent oriEvent = event.getOriginalEvent();
        if (oriEvent == null) {
            return;
        }
        if (oriEvent.getDamageSource().getEntity() instanceof Player player) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack blade = event.getBlade();
            var enchantmentLookup = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            
            boolean flag = false;
            for (var entry : stack.getAllEnchantments(enchantmentLookup).entrySet()) {
                Holder<Enchantment> enchantment = entry.getKey();
                if (EnchantmentHelper.getTagEnchantmentLevel(enchantment, blade) >= enchantment.value().getMaxLevel()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void proudSoulEnchantmentProbabilityCheck(ProudSoulEnchantmentEvent event) {
        SlashBladeEvent.BladeStandAttackEvent oriEvent = event.getOriginalEvent();
        if (oriEvent == null) {
            return;
        }
        Player player = (Player) oriEvent.getDamageSource().getEntity();
        if (player != null) {
            Level world = player.level();
            RandomSource random = world.getRandom();
            
            if (random.nextFloat() > event.getProbability()) {
                event.setCanceled(true);
            }
        }
    }
    
    private static void spawnSucceedEffects(Level world, BladeStandEntity bladeStand, RandomSource random) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }
        // 音效
        serverLevel.playSound(
            bladeStand,
            bladeStand.getPos(),
            SoundEvents.WITHER_SPAWN,
            SoundSource.BLOCKS,
            0.5f,
            0.8f
        );
        
        // 粒子效果
        for (int i = 0; i < 32; ++i) {
            double xDist = (random.nextFloat() * 2.0F - 1.0F);
            double yDist = (random.nextFloat() * 2.0F - 1.0F);
            double zDist = (random.nextFloat() * 2.0F - 1.0F);
            if (xDist * xDist + yDist * yDist + zDist * zDist <= 1.0D) {
                double x = bladeStand.getX(xDist / 4.0D);
                double y = bladeStand.getY(0.5D + yDist / 4.0D);
                double z = bladeStand.getZ(zDist / 4.0D);
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    0,
                    xDist, yDist + 0.2D, zDist,
                    1);
            }
        }
    }
}
