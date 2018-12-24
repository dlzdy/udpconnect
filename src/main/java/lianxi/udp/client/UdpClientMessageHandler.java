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
public class UdpClientMessageHandler extends ChannelInboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(UdpClientMessageHandler.class);

	private MessageRegistry registry;
	private UdpRpcClient client;
	private ChannelHandlerContext context;
	//private Channel channel;
	private ConcurrentMap<String, RpcFuture<?>> pendingTasks = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");

	public UdpClientMessageHandler(UdpRpcClient client) {
		this.client = client;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.context = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.context = null;
		pendingTasks.forEach((__, future) -> {
			future.fail(ConnectionClosed);
		});
		pendingTasks.clear();
		// 尝试重连
		ctx.channel().eventLoop().schedule(() -> {
			client.reconnect();
		}, 1, TimeUnit.SECONDS);
	}

	public <T> RpcFuture<T> send(MessageOutput output) {
		RpcFuture<T> future = new RpcFuture<T>();
		if (context != null) {
			context.channel().eventLoop().execute(() -> {
				pendingTasks.put(output.getRequestId(), future);
				context.writeAndFlush(output);
			});
		} else {
			future.fail(ConnectionClosed);
		}
		return future;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof MessageInput)) {
			return;
		}
		MessageInput input = (MessageInput) msg;
		// 业务逻辑在这里
		Class<?> clazz = registry.get(input.getType());
		if (clazz == null) {
			logger.error("unrecognized msg type {}", input.getType());
			return;
		}
		Object o = input.getPayload(clazz);
		@SuppressWarnings("unchecked")
		RpcFuture<Object> future = (RpcFuture<Object>) pendingTasks.remove(input.getRequestId());
		if (future == null) {
			logger.error("future not found with type {}", input.getType());
			return;
		}
		future.success(o);
	}


	public void close() {
		if (context != null) {
			context.close();
		}
	}

}
