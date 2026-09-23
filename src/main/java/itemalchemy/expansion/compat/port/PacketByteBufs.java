package itemalchemy.expansion.compat.port;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
public final class PacketByteBufs {
    public static FriendlyByteBuf create() { return new FriendlyByteBuf(Unpooled.buffer()); }
}
