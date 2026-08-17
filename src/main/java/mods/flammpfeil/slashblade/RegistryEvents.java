package mods.flammpfeil.slashblade;

import com.google.common.base.CaseFormat;
import mods.flammpfeil.slashblade.advancement.SlashBladeItemPredicate;
import mods.flammpfeil.slashblade.client.renderer.entity.*;
import mods.flammpfeil.slashblade.entity.*;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@SuppressWarnings("NotNullFieldNotInitialized")
@EventBusSubscriber(modid = SlashBlade.MODID)
public class RegistryEvents {
    
    public static final ResourceLocation BladeItemEntityLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(BladeItemEntity.class));
    public static EntityType<BladeItemEntity> BladeItem;
    
    public static final ResourceLocation BladeStandEntityLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(BladeStandEntity.class));
    public static EntityType<BladeStandEntity> BladeStand;
    
    public static final ResourceLocation SummonedSwordLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityAbstractSummonedSword.class));
    public static EntityType<EntityAbstractSummonedSword> SummonedSword;
    public static final ResourceLocation SpiralSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntitySpiralSwords.class));
    public static EntityType<EntitySpiralSwords> SpiralSwords;
    
    public static final ResourceLocation StormSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityStormSwords.class));
    public static EntityType<EntityStormSwords> StormSwords;
    public static final ResourceLocation BlisteringSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityBlisteringSwords.class));
    public static EntityType<EntityBlisteringSwords> BlisteringSwords;
    public static final ResourceLocation HeavyRainSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityHeavyRainSwords.class));
    public static EntityType<EntityHeavyRainSwords> HeavyRainSwords;
    
    public static final ResourceLocation JudgementCutLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityJudgementCut.class));
    public static EntityType<EntityJudgementCut> JudgementCut;
    
    public static final ResourceLocation SlashEffectLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntitySlashEffect.class));
    public static EntityType<EntitySlashEffect> SlashEffect;
    
    public static final ResourceLocation DriveLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        classToString(EntityDrive.class));
    public static EntityType<EntityDrive> Drive;
    
    
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        
        event.register(Registries.ENTITY_TYPE, helper -> {
            {
                EntityType<EntityAbstractSummonedSword> entity = SummonedSword = EntityType.Builder
                    .of(EntityAbstractSummonedSword::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .setTrackingRange(4).setUpdateInterval(20)
                    .build(SummonedSwordLoc.toString());
                helper.register(SummonedSwordLoc, entity);
            }
            
            {
                EntityType<EntityStormSwords> entity = StormSwords = EntityType.Builder
                    .of(EntityStormSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(StormSwordsLoc.toString());
                helper.register(StormSwordsLoc, entity);
            }
            
            {
                EntityType<EntitySpiralSwords> entity = SpiralSwords = EntityType.Builder
                    .of(EntitySpiralSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(SpiralSwordsLoc.toString());
                helper.register(SpiralSwordsLoc, entity);
            }
            
            {
                EntityType<EntityBlisteringSwords> entity = BlisteringSwords = EntityType.Builder
                    .of(EntityBlisteringSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(BlisteringSwordsLoc.toString());
                helper.register(BlisteringSwordsLoc, entity);
            }
            
            {
                EntityType<EntityHeavyRainSwords> entity = HeavyRainSwords = EntityType.Builder
                    .of(EntityHeavyRainSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(HeavyRainSwordsLoc.toString());
                helper.register(HeavyRainSwordsLoc, entity);
            }
            
            {
                EntityType<EntityJudgementCut> entity = JudgementCut = EntityType.Builder
                    .of(EntityJudgementCut::new, MobCategory.MISC).sized(2.5F, 2.5F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(JudgementCutLoc.toString());
                helper.register(JudgementCutLoc, entity);
            }
            
            {
                EntityType<BladeItemEntity> entity = BladeItem = EntityType.Builder
                    .of(BladeItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(BladeItemEntityLoc.toString());
                helper.register(BladeItemEntityLoc, entity);
            }
            
            {
                EntityType<BladeStandEntity> entity = BladeStand = EntityType.Builder
                    .of(BladeStandEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(10)
                    .setUpdateInterval(20).setShouldReceiveVelocityUpdates(false)
                    .build(BladeStandEntityLoc.toString());
                helper.register(BladeStandEntityLoc, entity);
            }
            
            {
                EntityType<EntitySlashEffect> entity = SlashEffect = EntityType.Builder
                    .of(EntitySlashEffect::new, MobCategory.MISC).sized(3.0F, 3.0F).setTrackingRange(4)
                    .setUpdateInterval(20)
                    .build(SlashEffectLoc.toString());
                helper.register(SlashEffectLoc, entity);
            }
            
            {
                EntityType<EntityDrive> entity = Drive = EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F).setTrackingRange(4).setUpdateInterval(20)
                    .build(DriveLoc.toString());
                helper.register(DriveLoc, entity);
            }
            
        });
        
        event.register(Registries.STAT_TYPE, helper -> SWORD_SUMMONED = registerCustomStat("sword_summoned"));
        
        event.register(Registries.ITEM_SUB_PREDICATE_TYPE, helper ->
            helper.register(SlashBlade.prefix("slashblade"),
                new ItemSubPredicate.Type<>(SlashBladeItemPredicate.CODEC)));
        
    }
    
    private static String classToString(Class<? extends Entity> entityClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName())
            .replace("entity_", "");
    }
    
    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RegistryEvents.SummonedSword, SummonedSwordRenderer::new);
        event.registerEntityRenderer(RegistryEvents.StormSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(RegistryEvents.SpiralSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(RegistryEvents.BlisteringSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(RegistryEvents.HeavyRainSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(RegistryEvents.JudgementCut, JudgementCutRenderer::new);
        event.registerEntityRenderer(RegistryEvents.BladeItem, BladeItemEntityRenderer::new);
        event.registerEntityRenderer(RegistryEvents.BladeStand, BladeStandEntityRenderer::new);
        event.registerEntityRenderer(RegistryEvents.SlashEffect, SlashEffectRenderer::new);
        event.registerEntityRenderer(RegistryEvents.Drive, DriveRenderer::new);
        
    }
    
    public static ResourceLocation SWORD_SUMMONED;
    
    private static ResourceLocation registerCustomStat(String name) {
        ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, name, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
        return resourcelocation;
    }

    /*
      /scoreboard objectives add stat minecraft.custom:slashblade.sword_summoned
      /scoreboard objectives setdisplay sidebar stat
     */
}