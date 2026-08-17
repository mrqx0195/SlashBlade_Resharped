package mods.flammpfeil.slashblade.event.ability;

/**
 * 特殊移动事件，V键冲刺时触发，取消事件可以阻止冲刺发生
 *
 * @author Arcomit
 * @since 2026-05-04
 */
import lombok.Getter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.EnumSet;

@Cancelable
public class SprintMoveEvent extends Event {
	@Getter
	private final ServerPlayer player;
	private final EnumSet<InputCommand> currentCommands;

	public SprintMoveEvent(ServerPlayer player, EnumSet<InputCommand> currentCommands) {
		this.player = player;
		this.currentCommands = currentCommands;
	}

	public EnumSet<InputCommand> getCommands() {
		return currentCommands;
	}
}