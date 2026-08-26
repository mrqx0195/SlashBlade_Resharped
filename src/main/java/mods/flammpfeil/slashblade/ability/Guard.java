package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.EnumSet;

public class Guard {
    private static final class SingletonHolder {
        private static final Guard instance = new Guard();
    }
    
    public static Guard getInstance() {
        return Guard.SingletonHolder.instance;
    }
    
    private Guard() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    static public final ResourceLocation ADVANCEMENT_GUARD = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "abilities/guard");
    static public final ResourceLocation ADVANCEMENT_GUARD_JUST = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
        "abilities/guard_just");
    
    final static EnumSet<InputCommand> move = EnumSet.of(InputCommand.FORWARD, InputCommand.BACK, InputCommand.LEFT,
        InputCommand.RIGHT);
    
    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        
        // begin executable check -----------------
        // item check
        ItemStack stack = victim.getMainHandItem();
        var slashBlade = BladeStateAccess.of(stack);
        if (slashBlade.isEmpty()) {
            return;
        }
        if (slashBlade.filter(ISlashBladeState::isBroken).isPresent()) {
            return;
        }
        if (stack.getEnchantmentLevel(victim.level().registryAccess().holderOrThrow(Enchantments.THORNS)) <= 0) {
            return;
        }
        
        // user check
        if (!victim.onGround()) {
            return;
        }
        IInputState input = victim.getData(CapabilityInputState.INPUT_STATE.get());
        
        // commanc check
        InputCommand targetCommand = InputCommand.SNEAK;
        boolean handleCommand = input.getCommands().contains(targetCommand)
            && input.getCommands().stream().noneMatch(move::contains);
        
        if (handleCommand) {
            AdvancementHelper.grantCriterion(victim, ADVANCEMENT_GUARD);
        }
        
        // ninja run
        handleCommand |= (input.getCommands().contains(InputCommand.SPRINT)
            && victim.isSprinting());
        
        if (!handleCommand) {
            return;
        }
        
        // range check
        if (!isInsideGuardableRange(source, victim)) {
            return;
        }
        
        // performance branch -----------------
        // just check
        long timeStartPress = input.getLastPressTime(targetCommand);
        long timeCurrent = victim.level().getGameTime();
        
        int soulSpeedLevel = EnchantmentHelper.getTagEnchantmentLevel(victim.level().registryAccess().holderOrThrow(Enchantments.SOUL_SPEED), stack);
        int justAcceptancePeriod = 5 + soulSpeedLevel;
        
        boolean isJust = false;
        if (timeCurrent - timeStartPress < justAcceptancePeriod) {
            isJust = true;
            AdvancementHelper.grantedIf(victim.level().registryAccess().holderOrThrow(Enchantments.SOUL_SPEED).value(), victim);
        }
        
        // rank check
        boolean isHighRank = false;
        IConcentrationRank rank = victim.getData(CapabilityConcentrationRank.RANK_POINT.get());
        IConcentrationRank.ConcentrationRanks ranks = rank.getRank(timeCurrent);
        if (ranks != null && IConcentrationRank.ConcentrationRanks.S.level <= ranks.level) {
            isHighRank = true;
        }
        
        // damage sauce check
        boolean isProjectile = source.is(DamageTypeTags.IS_PROJECTILE)
            || source.getDirectEntity() instanceof Projectile;
        
        // after executable check -----------------
        if (!isJust) {
            if (!isProjectile) {
                return;
            }
            if (!isHighRank && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                return;
            }
            
            boolean inMotion = slashBlade.filter(s -> {
                ResourceLocation current = s.resolvCurrentComboState(victim);
                ComboState currentCS = ComboStateRegistry.REGISTRY.get(current);
                if (currentCS != null) {
                    return !current.equals(ComboStateRegistry.NONE.getId()) && current.equals(currentCS.getNext(victim));
                }
                return false;
            }).isPresent();
            if (inMotion) {
                return;
            }
        } else {
            if (!isProjectile && !(source.getDirectEntity() instanceof LivingEntity)) {
                return;
            }
        }
        
        // execute performance------------------
        // damage cancel
        event.setCanceled(true);
        
        // Motion
        if (isJust) {
            slashBlade.ifPresent(s -> s.updateComboSeq(victim, ComboStateRegistry.COMBO_A1.getId()));
        } else {
            slashBlade.ifPresent(s -> s.updateComboSeq(victim, ComboStateRegistry.COMBO_A1_END2.getId()));
        }
        
        // DirectAttack knockback
        if (!isProjectile) {
            Entity entity = source.getDirectEntity();
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).knockback(0.5D, entity.getX() - victim.getX(), entity.getZ() - victim.getZ());
            }
        }
        
        // untouchable time
        if (isJust) {
            Untouchable.setUntouchable(victim, 10);
        }
        
        // rankup
        if (isJust) {
            rank.addRankPoint(victim.level().damageSources().thorns(victim));
        }
        
        // play sound
        if (victim instanceof Player) {
            victim.playSound(SoundEvents.TRIDENT_HIT_GROUND, 1.0F,
                1.0F + victim.level().getRandom().nextFloat() * 0.4F);
        }
        
        // advancement
        if (isJust) {
            AdvancementHelper.grantCriterion(victim, ADVANCEMENT_GUARD_JUST);
        }
        
        // cost-------------------------
        if (!isJust && !isHighRank) {
            slashBlade.ifPresent(s -> stack.hurtAndBreak(1, victim, EquipmentSlot.MAINHAND));
        }
        
    }
    
    public boolean isInsideGuardableRange(DamageSource source, LivingEntity victim) {
        Vec3 sPos = source.getSourcePosition();
        if (sPos != null) {
            Vec3 viewVec = victim.getViewVector(1.0F);
            Vec3 attackVec = sPos.vectorTo(victim.position()).normalize();
            attackVec = new Vec3(attackVec.x, 0.0D, attackVec.z);
            return attackVec.dot(viewVec) < 0.0D;
        }
        return false;
    }
}
