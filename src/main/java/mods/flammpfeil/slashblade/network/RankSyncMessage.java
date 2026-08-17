package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RankSyncMessage(long rawPoint) implements CustomPacketPayload {
    public static final Type<RankSyncMessage> TYPE = new Type<>(SlashBlade.prefix("rank_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RankSyncMessage> STREAM_CODEC = CustomPacketPayload
        .codec(RankSyncMessage::write, RankSyncMessage::new);
    
    private RankSyncMessage(RegistryFriendlyByteBuf buf) {
        this(buf.readLong());
    }
    
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeLong(this.rawPoint);
    }
    
    @Override
    public Type<RankSyncMessage> type() {
        return TYPE;
    }
    
    public static void handle(RankSyncMessage msg, IPayloadContext ctx) {
        Player pl = ctx.player();
        IConcentrationRank cr = pl.getData(CapabilityConcentrationRank.RANK_POINT.get());
        
        long time = pl.level().getGameTime();
        IConcentrationRank.ConcentrationRanks oldRank = cr.getRank(time);
        
        cr.setRawRankPoint(msg.rawPoint());
        cr.setLastUpdte(time);
        
        IConcentrationRank.ConcentrationRanks newRank = cr.getRank(time);
        if (oldRank == null || newRank != null && oldRank.level < newRank.level) {
            cr.setLastRankRise(time);
        }
    }
}
