package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Objects;

public class SneakingMotionCanceller {
    private static final class SingletonHolder {
        private static final SneakingMotionCanceller instance = new SneakingMotionCanceller();
    }
    
    public static SneakingMotionCanceller getInstance() {
        return SingletonHolder.instance;
    }
    
    private SneakingMotionCanceller() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRenderPlayerEventPre(RenderPlayerEvent.Pre event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        
        if (stack.isEmpty()) {
            return;
        }
        if (BladeStateAccess.of(stack).isEmpty()) {
            return;
        }
        
        if (!event.getRenderer().getModel().crouching) {
            return;
        }
        
        final Minecraft instance = Minecraft.getInstance();
        if (instance.options.getCameraType() == CameraType.FIRST_PERSON && Objects.equals(instance.player, event.getEntity())) {
            return;
        }
        
        event.getRenderer().getModel().crouching = false;
        
        Vec3 offset = event.getRenderer()
            .getRenderOffset((AbstractClientPlayer) event.getEntity(), event.getPartialTick()).scale(-1);
        
        event.getPoseStack().translate(offset.x, offset.y, offset.z);
    }
}
