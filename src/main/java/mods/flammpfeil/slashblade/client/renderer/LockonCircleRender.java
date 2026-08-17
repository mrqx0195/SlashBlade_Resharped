package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

@EventBusSubscriber(modid = SlashBlade.MODID, value = Dist.CLIENT)
public class LockonCircleRender {
    @Nullable
    private static Entity cachedLockOnTarget;
    
    static final ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath("slashblade", "model/util/lockon.obj");
    static final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath("slashblade", "model/util/lockon.png");
    
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onEntityUpdate(RenderFrameEvent.Pre event) {
        final Minecraft mcinstance = Minecraft.getInstance();
        if (mcinstance.player == null) {
            return;
        }
        
        LocalPlayer player = mcinstance.player;
        
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        
        IInputState input = player.getData(CapabilityInputState.INPUT_STATE.get());
        if (!input.getCommands().contains(InputCommand.SNEAK)) {
            cachedLockOnTarget = null;
            return;
        }
        
        Entity target = BladeStateAccess.of(stack)
            .map(s -> s.getTargetEntity(player.level()))
            .orElse(null);
        if (target == null || !target.isAlive()) {
            cachedLockOnTarget = null;
            return;
        }
        cachedLockOnTarget = target;
        
        float partialTicks = mcinstance.getTimer().getGameTimeDeltaPartialTick(true);
        
        float oldYawHead = player.yHeadRot;
        float oldYawOffset = player.yBodyRot;
        float oldPitch = player.getXRot();
        float oldYaw = player.getYRot();
        
        float prevYawHead = player.yHeadRotO;
        float prevYawOffset = player.yBodyRotO;
        float prevYaw = player.yRotO;
        float prevPitch = player.xRotO;
        
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getEyeHeight() / 2.0, 0));
        
        float step = 0.125f * partialTicks;
        
        step *= (float) Math.min(1.0f, Math.abs(Mth.wrapDegrees(oldYaw - player.yHeadRot) * 0.5));
        
        player.setXRot(Mth.rotLerp(step, oldPitch, player.getXRot()));
        player.setYRot(Mth.rotLerp(step, oldYaw, player.getYRot()));
        player.setYHeadRot(Mth.rotLerp(step, oldYawHead, player.getYHeadRot()));
        
        player.yBodyRot = oldYawOffset;
        
        player.yBodyRotO = prevYawOffset;
        player.yHeadRotO = prevYawHead;
        player.yRotO = prevYaw;
        player.xRotO = prevPitch;
    }
    
    
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        final Minecraft minecraftInstance = Minecraft.getInstance();
        Player player = minecraftInstance.player;
        if (player == null) {
            return;
        }
        if (!player.getData(CapabilityInputState.INPUT_STATE.get()).getCommands().contains(InputCommand.SNEAK)) {
            return;
        }
        
        if (cachedLockOnTarget == null || !cachedLockOnTarget.isAlive()
            || event.getEntity() != cachedLockOnTarget) {
            return;
        }
        
        ItemStack stack = player.getMainHandItem();
        Optional<Color> effectColor = BladeStateAccess.of(stack).map(ISlashBladeState::getEffectColor);
        if (effectColor.isEmpty()) {
            return;
        }
        
        LivingEntity livingEntity = event.getEntity();
        
        if (!livingEntity.isAlive()) {
            return;
        }
        
        float health = livingEntity.getHealth() / livingEntity.getMaxHealth();
        
        Color col = new Color(effectColor.get().getRGB() & 0xFFFFFF | 0xAA000000, true);
        
        PoseStack poseStack = event.getPoseStack();
        
        float f = livingEntity.getBbHeight() * 0.5f;
        float partialTicks = event.getPartialTick();
        
        poseStack.pushPose();
        poseStack.translate(0.0D, f, 0.0D);
        
        Vec3 offset = minecraftInstance.getEntityRenderDispatcher().camera.getPosition()
            .subtract(livingEntity.getPosition(partialTicks).add(0, f, 0));
        offset = offset.scale(0.5f);
        poseStack.translate(offset.x(), offset.y(), offset.z());
        
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        // poseStack.scale(-0.025F, -0.025F, 0.025F);
        
        float scale = 0.0025f;
        poseStack.scale(scale, -scale, scale);
        
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
        ResourceLocation resourceTexture = textureLoc;
        
        MultiBufferSource buffer = event.getMultiBufferSource();
        
        final String base = "lockonBase";
        final String mask = "lockonHealthMask";
        final String value = "lockonHealth";
        
        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, base, resourceTexture, poseStack, buffer,
            BladeRenderState.MAX_LIGHT);
        {
            poseStack.pushPose();
            poseStack.translate(0, 0, (1.0f - health) * 10.0f);
            BladeRenderState.setCol(new Color(0x20000000, true));
            BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, mask, resourceTexture, poseStack,
                buffer, BladeRenderState.MAX_LIGHT);
            poseStack.popPose();
        }
        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, value, resourceTexture, poseStack,
            buffer, BladeRenderState.MAX_LIGHT);
        
        poseStack.popPose();
    }
    
    @Nullable
    public static Entity getCachedLockOnTarget() {
        return cachedLockOnTarget;
    }
}
