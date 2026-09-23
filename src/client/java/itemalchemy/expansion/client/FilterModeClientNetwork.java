package itemalchemy.expansion.client;

import itemalchemy.expansion.network.FilterModeNetwork;
import itemalchemy.expansion.compat.port.ClientPlayNetworking;
import itemalchemy.expansion.compat.port.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import itemalchemy.expansion.search.SearchFilterMode;

/**
 * 「筛选模式切换」网络包客户端发送方。
 *
 * <p>客户端点击筛选按钮时调用 {@link #send(SearchFilterMode)}，发送 ordinal 给服务端。
 * 服务端在 {@link FilterModeNetwork#registerServer} 接收并应用。</p>
 */
public final class FilterModeClientNetwork {

    private FilterModeClientNetwork() {}

    /** 发送筛选模式切换包 */
    public static void send(SearchFilterMode mode) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeByte(mode.ordinal());
        ClientPlayNetworking.send(FilterModeNetwork.FILTER_MODE_ID, buf);
    }
}
