package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

public class KillCounter {
    private static final class SingletonHolder {
        private static final KillCounter instance = new KillCounter();
    }
    
    public static KillCounter getInstance() {
        return SingletonHolder.instance;
    }
    
    private KillCounter() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeathEvent(LivingDeathEvent event) {
        Entity trueSource = event.getSource().getEntity();
        
        if (!(trueSource instanceof LivingEntity)) {
            return;
        }
        
        ItemStack stack = ((LivingEntity) trueSource).getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (BladeStateAccess.of(stack).isEmpty()) {
            return;
        }
        
        BladeStateAccess.of(stack).ifPresent(state -> {
            var killCountEvent = new SlashBladeEvent.AddKillCountEvent(stack, state, 1);
            NeoForge.EVENT_BUS.post(killCountEvent);
            state.setKillCount(state.getKillCount() + killCountEvent.getNewCount());
            ItemSlashBlade.updateRarity(stack);
        });
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onXPDropping(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (BladeStateAccess.of(stack).isEmpty()) {
            return;
        }
        
        IConcentrationRank.ConcentrationRanks rankBonus = player
            .getExistingData(CapabilityConcentrationRank.RANK_POINT.get())
            .map(rp -> rp.getRank(player.level().getGameTime()))
            .orElse(IConcentrationRank.ConcentrationRanks.NONE);
        int souls = (int) Math.floor(event.getDroppedExperience() * (1.0F + (rankBonus.level * 0.1F)));
        
        BladeStateAccess.of(stack).ifPresent(state -> {
            var soulEvent = new SlashBladeEvent.AddProudSoulEvent(stack, state, Math.min(SlashBladeConfig.MAX_PROUD_SOUL_GOT.get(), souls));
            NeoForge.EVENT_BUS.post(soulEvent);
            int newCount = soulEvent.getNewCount();
            state.setProudSoulCount(
                state.getProudSoulCount() + newCount);
            if (SwordType.from(stack).contains(SwordType.SOULEATER)) {
                int damage = Math.max(1, newCount / 4);
                stack.setDamageValue(Math.max(stack.getDamageValue() - damage, 0));
            }
            ItemSlashBlade.updateRarity(stack);
        });
        
    }
}
