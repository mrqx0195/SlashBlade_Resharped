package mods.flammpfeil.slashblade.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;

public class UserPoseOverrider {
    
    private static final class SingletonHolder {
        private static final UserPoseOverrider instance = new UserPoseOverrider();
    }
    
    public static UserPoseOverrider getInstance() {
        return SingletonHolder.instance;
    }
    
    private UserPoseOverrider() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRenderPlayerEventPre(RenderLivingEvent.Pre<?, ?> event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        
        PoseStack poseStack = event.getPoseStack();
        LivingEntity entity = event.getEntity();
        float partialTicks = event.getPartialTick();
        
        ISlashBladeState state = BladeStateAccess.of(stack).orElse(null);
        if (state == null) {
            return;
        }

        float prevRot = getComboRotation(state, entity, -1);
        float currRot = getComboRotation(state, entity, 0);
        
        boolean hasSwimFly = entity.isFallFlying() || entity.getSwimAmount(partialTicks) > 0f;
        if (prevRot == 0f && currRot == 0f && !hasSwimFly) {
            return;
        }
        
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
        anotherPoseRotP(poseStack, entity, partialTicks);
        
        float yaw = getInterpolatedRotation(currRot, prevRot, partialTicks);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        
        anotherPoseRotN(poseStack, entity, partialTicks);
        poseStack.mulPose(Axis.YN.rotationDegrees(180.0F - f));
    }
    
    public static float getInterpolatedComboRotation(ISlashBladeState state, LivingEntity entity, float partialTicks) {
        return getInterpolatedRotation(getComboRotation(state, entity, 0), getComboRotation(state, entity, -1), partialTicks);
    }

    public static float getInterpolatedComboRotation(LivingEntity entity, float partialTicks) {
        return BladeStateAccess.of(entity.getMainHandItem())
            .map(state -> getInterpolatedComboRotation(state, entity, partialTicks))
            .orElse(0f);
    }
    
    private static float getInterpolatedRotation(float currRot, float prevRot, float partialTicks) {
        // Preserve the legacy render direction. The original persistent-data path
        // interpolated current rotation back toward the previous tick's rotation.
        return Mth.rotLerp(partialTicks, currRot, prevRot);
    }
    
    static public void anotherPoseRotP(PoseStack matrixStackIn, LivingEntity entityLiving, float partialTicks) {
        final float np = 1;
        
        float f = entityLiving.getSwimAmount(partialTicks);
        if (entityLiving.isFallFlying()) {
            float f1 = (float) entityLiving.getFallFlyingTicks() + partialTicks;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isAutoSpinAttack()) {
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(np * f2 * (-90.0F - entityLiving.getXRot())));
            }
            
            Vec3 vector3d = entityLiving.getViewVector(partialTicks);
            Vec3 vector3d1 = entityLiving.getDeltaMovement();
            double d0 = vector3d1.horizontalDistanceSqr();
            double d1 = vector3d.horizontalDistanceSqr();
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.mulPose(Axis.YP.rotation((float) (np * Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(np * f4));
            if (entityLiving.isVisuallySwimming()) {
                matrixStackIn.translate(0.0D, np * -1.0D, (double) np * 0.3F);
            }
        }
    }
    
    static public void anotherPoseRotN(PoseStack matrixStackIn, LivingEntity entityLiving, float partialTicks) {
        final float np = -1;
        
        float f = entityLiving.getSwimAmount(partialTicks);
        if (entityLiving.isFallFlying()) {
            Vec3 vector3d = entityLiving.getViewVector(partialTicks);
            Vec3 vector3d1 = entityLiving.getDeltaMovement();
            double d0 = vector3d1.horizontalDistanceSqr();
            double d1 = vector3d.horizontalDistanceSqr();
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.mulPose(Axis.YP.rotation((float) (np * Math.signum(d3) * Math.acos(d2))));
            }
            
            float f1 = (float) entityLiving.getFallFlyingTicks() + partialTicks;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isAutoSpinAttack()) {
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(np * f2 * (-90.0F - entityLiving.getXRot())));
            }
        } else if (f > 0.0F) {
            if (entityLiving.isVisuallySwimming()) {
                matrixStackIn.translate(0.0D, np * -1.0D, (double) np * 0.3F);
            }
            
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(np * f4));
        }
    }
    
    public static float getComboRotation(ISlashBladeState state, LivingEntity entity, int tickOffset) {
        ComboState cs = ComboStateRegistry.REGISTRY.get(state.getComboSeq());
        if (cs == null) {
            return 0f;
        }
        long elapsed = state.getElapsedTime(entity);
        if (entity.level().isClientSide()) {
            elapsed = Math.max(0, elapsed - 1);
        }
        return cs.getRotationYaw((int) elapsed + tickOffset);
    }

    public static float getComboRotation(LivingEntity entity, int tickOffset) {
        return BladeStateAccess.of(entity.getMainHandItem())
            .map(state -> getComboRotation(state, entity, tickOffset))
            .orElse(0f);
    }
}
