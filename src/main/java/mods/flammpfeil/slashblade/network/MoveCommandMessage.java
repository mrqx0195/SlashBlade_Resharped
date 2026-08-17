package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;

public record MoveCommandMessage(EnumSet<InputCommand> command) implements CustomPacketPayload {
    public static final Type<MoveCommandMessage> TYPE = new Type<>(SlashBlade.prefix("move_command"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveCommandMessage> STREAM_CODEC = CustomPacketPayload
        .codec(MoveCommandMessage::write, MoveCommandMessage::new);
    
    @Deprecated
    public MoveCommandMessage(int command) {
        this(EnumSetConverter.convertToEnumSet(InputCommand.class, command));
    }
    
    private MoveCommandMessage(RegistryFriendlyByteBuf buf) {
        this(buf.readEnumSet(InputCommand.class));
    }
    
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnumSet(this.command, InputCommand.class);
    }
    
    @Override
    public Type<MoveCommandMessage> type() {
        return TYPE;
    }
    
    public static void handle(MoveCommandMessage msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sender)) {
            return;
        }
        
        ItemStack stack = sender.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty() || BladeStateAccess.of(stack).isEmpty()) {
            return;
        }
        
        IInputState state = sender.getData(CapabilityInputState.INPUT_STATE.get());
        EnumSet<InputCommand> old = state.getCommands().clone();
        
        state.getCommands().clear();
        state.getCommands().addAll(msg.command());
        
        EnumSet<InputCommand> current = state.getCommands().clone();
        long currentTime = sender.level().getGameTime();
        current.forEach(command -> {
            if (!old.contains(command)) {
                state.getLastPressTimes().put(command, currentTime);
            }
        });
        
        InputCommandEvent.onInputChange(sender, state, old, current);
    }
}
