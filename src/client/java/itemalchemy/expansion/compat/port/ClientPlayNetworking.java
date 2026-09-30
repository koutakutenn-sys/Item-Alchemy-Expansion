package itemalchemy.expansion.compat.port;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public final class ClientPlayNetworking {
    private static final Map<Identifier, Receiver> RECEIVERS = new HashMap<>();
    private static boolean registered;
    public interface Receiver { void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender); }
    public static void registerGlobalReceiver(Identifier id, Receiver receiver) {
        RECEIVERS.put(id, receiver);
        if (registered) return;
        registered = true;
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(CompatPayload.TYPE, (payload, ctx) -> {
            Receiver target = RECEIVERS.get(payload.route());
            if (target == null) return;
            FriendlyByteBuf buf = payload.buffer();
            try { target.receive(ctx.client(), ctx.client().getConnection(), buf, ctx.responseSender()); }
            finally { buf.release(); }
        });
    }
    public static void send(Identifier id, FriendlyByteBuf buf) {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(CompatPayload.of(id, buf));
    }
    public static boolean canSend(Identifier id) {
        return net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(CompatPayload.TYPE);
    }
}
