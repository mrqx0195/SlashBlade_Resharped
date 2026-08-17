package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.EnumSet;

@OnlyIn(Dist.CLIENT)
public class MoveInputEvent extends Event implements ICancellableEvent {
    private final LocalPlayer player;
    private final EnumSet<InputCommand> oldCommands;
    private final EnumSet<InputCommand> newCommands;
    private final ClientTickEvent.Post originalEvent;
    
    public MoveInputEvent(LocalPlayer player, EnumSet<InputCommand> oldCommands, ClientTickEvent.Post originalEvent) {
        this.player = player;
        this.oldCommands = oldCommands;
        this.newCommands = oldCommands.clone();
        this.originalEvent = originalEvent;
    }
    
    public ClientTickEvent.Post getOriginalEvent() {
        return originalEvent;
    }
    
    public EnumSet<InputCommand> getOldCommands() {
        return oldCommands.clone();
    }
    
    public EnumSet<InputCommand> getNewCommands() {
        return newCommands;
    }
    
    public LocalPlayer getPlayer() {
        return player;
    }
}
