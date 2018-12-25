package lianxi.udp.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.MessageInput;
import lianxi.tcp.common.MessageOutput;
import lianxi.tcp.common.MessageRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class UdpClientUtils {

	private final static Logger logger = LoggerFactory.getLogger(UdpClientUtils.class);

	private MessageRegistry registry;
	
	private UdpRpcClient client;
	
	private Channel channel;
	
	private ConcurrentMap<String, RpcFuture<?>> pendingTasks = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");

	public UdpClientUtils(UdpRpcClient client) {
		this.client = client;
	}

	public <T> RpcFuture<T> send(MessageOutput output) {
		RpcFuture<T> future = new RpcFuture<T>();
		if (channel != null) {
			channel.eventLoop().execute(() -> {
				pendingTasks.put(output.getRequestId(), future);
				channel.writeAndFlush(output);
			});
		} else {
			future.fail(ConnectionClosed);
		}
		return future;
	}



	public void close() {
		if (channel != null) {
			channel.close();
		}
	}

}
