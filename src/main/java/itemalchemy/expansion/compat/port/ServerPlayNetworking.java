package itemalchemy.expansion.compat.port;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public final class ServerPlayNetworking {
    private static final Map<Identifier, Receiver> RECEIVERS = new HashMap<>();
    private static boolean registered;
    public interface Receiver { void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender); }
    public static void registerGlobalReceiver(Identifier id, Receiver receiver) {
        CompatPayload.init();
        RECEIVERS.put(id, receiver);
        if (registered) return;
        registered = true;
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(CompatPayload.TYPE, (payload, ctx) -> {
            Receiver target = RECEIVERS.get(payload.route());
            if (target == null) return;
            FriendlyByteBuf buf = payload.buffer();
            try { target.receive(ctx.server(), ctx.player(), ctx.player().connection, buf, ctx.responseSender()); }
            finally { buf.release(); }
        });
    }
    public static void send(ServerPlayer player, Identifier id, FriendlyByteBuf buf) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, CompatPayload.of(id, buf));
    }
    public static boolean canSend(ServerPlayer player, Identifier id) {
        return net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, CompatPayload.TYPE);
    }
}
