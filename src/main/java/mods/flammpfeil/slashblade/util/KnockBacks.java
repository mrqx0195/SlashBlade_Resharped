package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.event.handler.KnockBackHandler;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.function.Consumer;

public enum KnockBacks implements IExtensibleEnum {
    cancel((e) -> KnockBackHandler.setFactor(e, 0, 0, 0)),
    toss((e) -> KnockBackHandler.setVertical(e, 0.75f)),
    meteor((e) -> KnockBackHandler.setVertical(e, -5.5f)),
    smash((e) -> KnockBackHandler.setSmash(e, 1.5f));
    
    public final Consumer<LivingEntity> action;
    
    KnockBacks(Consumer<LivingEntity> action) {
        this.action = action;
    }
    
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(KnockBacks.class);
    }
}
