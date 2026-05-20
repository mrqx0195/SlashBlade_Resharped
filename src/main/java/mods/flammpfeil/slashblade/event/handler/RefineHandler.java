package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.RefineProgressEvent;
import mods.flammpfeil.slashblade.event.RefineSettlementEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class RefineHandler {
    private static final class SingletonHolder {
        private static final RefineHandler instance = new RefineHandler();
    }
    
    public static RefineHandler getInstance() {
        return SingletonHolder.instance;
    }
    
    private RefineHandler() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        if (!event.getOutput().isEmpty()) {
            return;
        }
        
        ItemStack base = event.getLeft();
        ItemStack material = event.getRight();
        
        if (base.isEmpty()) {
            return;
        }
        if (BladeStateAccess.of(base).isEmpty()) {
            return;
        }
        
        if (material.isEmpty()) {
            return;
        }
        
        boolean isRepairable = base.getItem().isValidRepairItem(base, material);
        if (!isRepairable) {
            return;
        }
        
        int level = material.getEnchantmentValue();
        
        if (level < 0) {
            return;
        }
        
        ItemStack result = base.copy();
        
        int refineLimit = Math.max(10, level);
        
        int materialCost = 0;
        int levelCostBase = SlashBladeConfig.REFINE_LEVEL_COST.get();
        int costResult = 0;
        AtomicInteger refineResult = new AtomicInteger(0);
        BladeStateAccess.of(result).ifPresent(s -> refineResult.set(s.getRefine()));
        
        while (materialCost < material.getCount()) {
            
            RefineProgressEvent e = new RefineProgressEvent(result,
                BladeStateAccess.of(result).orElseThrow(), materialCost + 1, levelCostBase,
                costResult, refineResult.get(), event);
            
            NeoForge.EVENT_BUS.post(e);
            if (e.isCanceled()) {
                break;
            }
            
            refineResult.set(e.getRefineResult());
            
            materialCost = e.getMaterialCost();
            costResult = e.getCostResult() + e.getLevelCost();
            
            if (!event.getPlayer().getAbilities().instabuild && event.getPlayer().experienceLevel < costResult) {
                break;
            }
        }
        
        if (BladeStateAccess.of(result).isPresent()) {
            ISlashBladeState state = BladeStateAccess.of(result).orElseThrow();
            RefineSettlementEvent e2 = new RefineSettlementEvent(result,
                state, materialCost, costResult, refineResult.get(), event);
            
            NeoForge.EVENT_BUS.post(e2);
            if (e2.isCanceled()) {
                return;
            }
            materialCost = e2.getMaterialCost();
            costResult = e2.getCostResult();
            
            if (state.getRefine() <= refineLimit) {
                if (state.getRefine() + e2.getRefineResult() < 200) {
                    state.setMaxDamage(state.getMaxDamage() + e2.getRefineResult());
                } else if (state.getRefine() < 200) {
                    state.setMaxDamage(state.getMaxDamage() + Math.min(state.getRefine() + e2.getRefineResult(), 200)
                        - state.getRefine());
                }
                
                state.setProudSoulCount(state.getProudSoulCount() + getRefineProudsoulCount(level, state, e2));
                
                state.setRefine(e2.getRefineResult());
            }
            
            result.setDamageValue(result.getDamageValue() - Math.max(result.getDamageValue(), materialCost * Math.max(1, level / 2)));
            
            ItemSlashBlade.updateRarity(result);
        }
        
        event.setMaterialCost(materialCost);
        event.setCost(costResult);
        event.setOutput(result);
    }
    
    public int getRefineProudsoulCount(int level, ISlashBladeState state, RefineSettlementEvent e2) {
        return (e2.getRefineResult() - state.getRefine())
            * Math.min(5000, level * 10);
    }
    
    private static final ResourceLocation REFINE = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "tips/refine");
    
    @SubscribeEvent
    public void onAnvilRepairEvent(AnvilRepairEvent event) {
        
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        
        ItemStack material = event.getRight();// .getIngredientInput();
        ItemStack base = event.getLeft();// .getItemInput();
        ItemStack output = event.getOutput();
        
        if (base.isEmpty()) {
            return;
        }
        if (!(base.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        if (material.isEmpty()) {
            return;
        }
        
        boolean isRepairable = base.getItem().isValidRepairItem(base, material);
        
        if (!isRepairable) {
            return;
        }
        
        int before = BladeStateAccess.of(base).map(ISlashBladeState::getRefine).orElse(0);
        int after = BladeStateAccess.of(output).map(ISlashBladeState::getRefine).orElse(0);
        
        if (before < after) {
            AdvancementHelper.grantCriterion((ServerPlayer) event.getEntity(), REFINE);
        }
        
    }
    
    @SubscribeEvent
    public void refineLimitCheck(RefineProgressEvent event) {
        AnvilUpdateEvent oriEvent = event.getOriginalEvent();
        if (oriEvent == null) {
            return;
        }
        int refineLimit = Math.max(10, oriEvent.getRight().getEnchantmentValue());
        if (event.getRefineResult() < refineLimit) {
            event.setRefineResult(event.getRefineResult() + 1);
        }
    }
}
