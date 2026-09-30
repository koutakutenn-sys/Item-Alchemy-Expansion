package itemalchemy.expansion.compat.port;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import io.netty.buffer.Unpooled;

/** Typed Fabric payload transport for the addon's existing message formats. */
public record CompatPayload(Identifier route, byte[] bytes) implements CustomPacketPayload {
    public static final Type<CompatPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("itemalchemy-expansion", "messages"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompatPayload> CODEC = StreamCodec.of(
        (buf, payload) -> { buf.writeIdentifier(payload.route); buf.writeByteArray(payload.bytes); },
        buf -> new CompatPayload(buf.readIdentifier(), buf.readByteArray(1048576)));
    private static boolean registered;
    public static void init() {
        if (registered) return;
        registered = true;
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
    public static CompatPayload of(Identifier id, FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        buffer.release();
        return new CompatPayload(id, bytes);
    }
    public FriendlyByteBuf buffer() { return new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)); }
    @Override public Type<CompatPayload> type() { return TYPE; }
}
