package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MotionBroadcastMessage(UUID playerId, ResourceLocation combo,
                                     long actionTime) implements CustomPacketPayload {
    public static final Type<MotionBroadcastMessage> TYPE = new Type<>(SlashBlade.prefix("motion_broadcast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MotionBroadcastMessage> STREAM_CODEC = CustomPacketPayload
        .codec(MotionBroadcastMessage::write, MotionBroadcastMessage::new);
    
    private MotionBroadcastMessage(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readResourceLocation(), buf.readVarLong());
    }
    
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeResourceLocation(this.combo);
        buf.writeVarLong(this.actionTime);
    }
    
    @Override
    public Type<MotionBroadcastMessage> type() {
        return TYPE;
    }
    
    public static void handle(MotionBroadcastMessage msg, IPayloadContext ctx) {
        Player target = ctx.player().level().getPlayerByUUID(msg.playerId());
        if (target == null) {
            return;
        }
        ComboState comboState = ComboStateRegistry.REGISTRY.get(msg.combo());
        if (comboState == null) {
            return;
        }

//        if (target == ctx.player()) {
//            return;
//        }
        
        NeoForge.EVENT_BUS.post(new BladeMotionEvent(target, msg.combo(), msg.actionTime()));
        comboState.clickAction(target);
    }
}
