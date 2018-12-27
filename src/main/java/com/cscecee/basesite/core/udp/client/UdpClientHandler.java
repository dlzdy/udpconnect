package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.MessageCommon;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.test.ExpResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.MessageOutput;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpClientHandler.class);
	// 业务线程池
	private ThreadPoolExecutor executor;

	private UdpClient udpClient;
	
	private ConcurrentMap<String, RpcFuture<?>> pendingTasks = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");

//	public UdpClientHandler(UdpClient udpClient, int workerThreads) {
//		this.udpClient = udpClient;
//		// 业务队列最大1000,避免堆积
//		// 如果子线程处理不过来,io线程也会加入业务逻辑(callerRunsPolicy)
//		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
//		// 给业务线程命名
//		ThreadFactory factory = new ThreadFactory() {
//			AtomicInteger seq = new AtomicInteger();
//
//			@Override
//			public Thread newThread(Runnable r) {
//				Thread t = new Thread(r);
//				t.setName("rpc-" + seq.getAndIncrement());
//				return t;
//			}
//
//		};
//		// 闲置时间超过30秒的线程就自动销毁
//		this.executor = new ThreadPoolExecutor(1, workerThreads, 5, TimeUnit.SECONDS, queue, factory,
//				new CallerRunsPolicy());
//	}
//
//
//	// public void closeGracefully() {
//	// //优雅一点关闭,先通知,再等待,最后强制关闭
//	// this.executor.shutdown();
//	// try {
//	// this.executor.awaitTermination(10, TimeUnit.SECONDS);
//	// } catch (InterruptedException e) {
//	// }
//	// this.executor.shutdownNow();
//	// }
//	@Override
//	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
//		try {
//			InetSocketAddress sender = datagramPacket.sender();
//			ByteBuf in = datagramPacket.content();
//			String fromId = readStr(in);//fromId
//			String requestId = readStr(in);//requestId
//			String type = readStr(in);//type
//			String payload = readStr(in);//TODO byte[]
//			
//			logger.debug("recieve reqid <<<<<" + requestId);
//
//			RpcFuture<Object> future = (RpcFuture<Object>) pendingTasks.remove(requestId);
//			if (future == null) {//没找到对应的reqId
//				if ("0".equals(fromId) || "".equals(fromId) ) {//从服务器发来的请求消息
//					final MessageCommon messageInput = new MessageCommon(fromId, requestId, type, payload);
//					this.executor.execute(() -> {
//						this.handleMessage(ctx, sender, messageInput);
//					});
//				}else {
//					logger.error("future not found with type {}", type);
//					return;
//				}
//			}else {
//				future.success(payload);
//			}
//
//		} catch (Exception e) {
//			logger.error("failed", e);
//		}
//	}
//	
//	private void handleMessage(ChannelHandlerContext ctx, InetSocketAddress sender, MessageCommon messageInput) {
//		// 业务逻辑在这里
//		
//		IMessageHandler handler = udpClient.getHandlers().get(messageInput.getType());
//		if (handler != null) {
//			handler.handle(ctx, sender, messageInput.getRequestId(),  messageInput.getPayload());
//		} else {
//			//handlers.defaultHandler().handle(ctx, input.getRequestId(), input);
//			logger.error("not found handler of " + messageInput.getType());
//		}
//	}
//	private String readStr(ByteBuf in) {
//		int len = in.readInt();
//		if (len < 0 || len > (1 << 20)) {
//			throw new DecoderException("string too long len=" + len);
//		}
//		byte[] bytes = new byte[len];
//		in.readBytes(bytes);
//		return new String(bytes, Charsets.UTF8);
//	}
//
//	@Override
//	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		ctx.close();
//		logger.error(cause.getMessage(), cause);
//	}
//
//	/**
//	 * 发送消息
//	 * @param output
//	 * @return
//	 */
//	public <T> RpcFuture<T> send(MessageCommon output) {
//		RpcFuture<T> future = new RpcFuture<T>();
//
//		
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, output.getFromId() );// fromId
//		writeStr(buf, output.getRequestId());// requestId
//		writeStr(buf, output.getType());//type
//		writeStr(buf, output.getPayload() + "");//payload
//		//ctx.writeAndFlush(new DatagramPacket(buf, sender));
//		Channel channel = udpClient.getChannel();
//		if (channel != null) {
//			channel.eventLoop().execute(() -> {
//				pendingTasks.put(output.getRequestId(), future);
//				// datasocket
//				logger.debug("send reqid >>>>>" + output.getRequestId());
//				channel.writeAndFlush(new DatagramPacket(buf, udpClient.getRemoteSocketAddress()));
//				
//			});
//		} else {
//			future.fail(ConnectionClosed);
//		}		
//		return future;
//	}
//	
//	private void writeStr(ByteBuf buf, String s) {
//		buf.writeInt(s.length());
//		buf.writeBytes(s.getBytes(Charsets.UTF8));
//	}	
}
