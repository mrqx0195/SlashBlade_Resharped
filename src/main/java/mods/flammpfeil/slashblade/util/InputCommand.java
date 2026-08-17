package mods.flammpfeil.slashblade.util;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.EnumSet;

public enum InputCommand implements IExtensibleEnum {
    FORWARD, BACK, LEFT, RIGHT, SNEAK, R_DOWN, L_DOWN, M_DOWN, R_CLICK, L_CLICK, ON_GROUND, ON_AIR, SPRINT, JUMP;
    
    public final static EnumSet<InputCommand> move = EnumSet.of(InputCommand.FORWARD, InputCommand.BACK,
        InputCommand.LEFT, InputCommand.RIGHT);
    
    public static boolean anyMatch(EnumSet<InputCommand> a, EnumSet<InputCommand> b) {
        return a.stream().anyMatch(b::contains);
    }
    
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(InputCommand.class);
    }
}
