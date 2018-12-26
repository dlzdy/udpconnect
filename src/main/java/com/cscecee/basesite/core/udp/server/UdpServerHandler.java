package com.cscecee.basesite.core.udp.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.CommonMessage;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);
	// 业务线程池
	private ThreadPoolExecutor executor;

	private MessageHandlers handlers;

//	private MessageRegistry registry;

	public UdpServerHandler(MessageHandlers handlers, int workerThreads) {
		System.out.println("=========2=============" + "MessageCollector.构造");
		// 业务队列最大1000,避免堆积
		// 如果子线程处理不过来,io线程也会加入业务逻辑(callerRunsPolicy)
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
		// 给业务线程命名
		ThreadFactory factory = new ThreadFactory() {
			AtomicInteger seq = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("rpc-" + seq.getAndIncrement());
				return t;
			}

		};
		// 闲置时间超过30秒的线程就自动销毁
		this.executor = new ThreadPoolExecutor(1, workerThreads, 30, TimeUnit.SECONDS, queue, factory,
				new CallerRunsPolicy());
		this.handlers = handlers;
		//this.registry = registry;
	}

	// public void closeGracefully() {
	// //优雅一点关闭,先通知,再等待,最后强制关闭
	// this.executor.shutdown();
	// try {
	// this.executor.awaitTermination(10, TimeUnit.SECONDS);
	// } catch (InterruptedException e) {
	// }
	// this.executor.shutdownNow();
	// }
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
		
		try {
			// String req = msg.content().toString(CharsetUtil.UTF_8);
			// logger.info("收到的请求:" + req);
			// if ("谚语字典查询?".equals(req)) {
			// ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语查询结果:" +
			// nextQueue(), CharsetUtil.UTF_8), msg.sender()));
			// }

			// 用业务线程处理消息
			this.executor.execute(() -> {
				this.handleMessage(ctx, datagramPacket);
			});

		} catch (Exception e) {
			logger.error("failed", e);
		}
	}
	
	private void handleMessage(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
		// 业务逻辑在这里
//		Class<?> clazz = registry.get(input.getType());
//		if (clazz == null) {
//			//没注册的消息用默认的处理器处理
//			//handlers.defaultHandler().handle(ctx, input.getRequestId(), input);
//			logger.error("not found clazz of " + input.getType());
//			return;
//		}
		InetSocketAddress sender = datagramPacket.sender();
		ByteBuf in = datagramPacket.content();
		String fromId = readStr(in);
		String requestId = readStr(in);
		String type = readStr(in);
		String payload = readStr(in);//TODO byte[]
		final CommonMessage messageInput = new CommonMessage(fromId, requestId, type, payload);
		
		//Object payload = input.getPayload();
		//这里有问题
		@SuppressWarnings("unchecked")
		IMessageHandler<Object> handler = (IMessageHandler<Object>) handlers.get(type);
		if (handler != null) {
			handler.handle(ctx, sender, requestId, payload);
		} else {
			//handlers.defaultHandler().handle(ctx, input.getRequestId(), input);
			logger.error("not found handler of " + type);
		}
	}
	private String readStr(ByteBuf in) {
		int len = in.readInt();
		if (len < 0 || len > (1 << 20)) {
			throw new DecoderException("string too long len=" + len);
		}
		byte[] bytes = new byte[len];
		in.readBytes(bytes);
		return new String(bytes, Charsets.UTF8);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error(cause.getMessage(), cause);
	}

}
