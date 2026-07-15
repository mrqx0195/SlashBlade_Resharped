package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BlockPickCanceller {
    private static final class SingletonHolder {
        private static final BlockPickCanceller instance = new BlockPickCanceller();
    }
    
    public static BlockPickCanceller getInstance() {
        return BlockPickCanceller.SingletonHolder.instance;
    }
    
    private BlockPickCanceller() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBlockPick(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) {
            return;
        }
        
        final Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) {
            return;
        }
        if (!SlashBladeKeyMappings.KEY_SUMMON_BLADE.getKey().equals(SlashBladeKeyMappings.KEY_SUMMON_BLADE.getDefaultKey())) {
            return;
        }
        if (BladeStateAccess.of(player.getMainHandItem()).isPresent()) {
            event.setCanceled(true);
        }
    }
}
