package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.MessageCommon;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.common.MessageReq;
import com.cscecee.basesite.core.udp.common.UdpMessageHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.RequestId;

public class UdpClient {

	private final static Logger logger = LoggerFactory.getLogger(UdpClient.class);
	// 服务器名称
	private String serverName;
	// 服务器端口
	private int serverPort;
	// 客户端ID
	private String clientId;
	//
	private Bootstrap bootstrap;
	//
	private EventLoopGroup group;
	//
	private Channel channel;
	// // 是否运行
	// private boolean isRunning;
	// // 是否连接
	// private boolean isConnected;
	private boolean started = false;

	private boolean stopped;

	private UdpMessageHandler clientHandler;
	
	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	
	private InetSocketAddress remoteSocketAddress = null;

	private MessageHandlers handlers = new MessageHandlers();

	public UdpClient(String serverName, int serverPort, String clientId) {
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.clientId = clientId;
		this.init();
	}
	
	public Channel getChannel() {
		return channel;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}
	
	public MessageHandlers getHandlers() {
		return handlers;
	}

	/*
	 * 注册服务的快捷方式
	 */
	public void register(String type,  IMessageHandler handler) {
		handlers.register(type, handler);
	}

	private void init() {
		bootstrap = new Bootstrap();
		// 1.设置bossGroup和workGroup
		group = new NioEventLoopGroup();
		bootstrap.group(group);
		// 2.指定使用NioServerSocketChannel来处理连接请求。
		bootstrap.channel(NioDatagramChannel.class);
		// 3.配置TCP/UDP参数。
		bootstrap.option(ChannelOption.SO_BROADCAST, true);
		// 4.配置handler和childHandler，数据处理器。
		remoteSocketAddress = new InetSocketAddress(serverName, serverPort);
		clientHandler =  new UdpMessageHandler(handlers ,10);
		
		// bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 将业务处理器放到最后
			    pipe.addLast(clientHandler);

			}

		});

	}

	/**
	 * 异步发送
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
	private <T> RpcFuture<T> sendAsync(String command, boolean isCompressed, byte[] data) {
		if (!started) {//未连接
			try {
				connect();
				started = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
				started = false;
			}
		}
		String requestId = RequestId.next();
		MessageReq output = new MessageReq(requestId, clientId,  command,  isCompressed, data);
		return clientHandler.send(output);
	}

	/**
	 * 同步发送
	 * @param type
	 * @param payload
	 * @return
	 */
	public <T> T send(String command, boolean isCompressed, byte[] data) {
		// 普通rpc请求,正常获取响应
		RpcFuture<T> future = sendAsync(command, isCompressed, data);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RPCException(e);
		}
	}


	/**
	 * udp连接到服务器， udp 发送心跳包，且有回应
	 * @throws InterruptedException 
	 *
	 */
	public void connect() throws InterruptedException {
			if (channel == null || !channel.isActive()) {
				ChannelFuture channelFuture = bootstrap.bind(0).sync();
				channel = channelFuture.channel();
			}
//				MessageOutput output = new MessageOutput(RequestId.next(), "heartbeat", "");
//				ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//				writeStr(buf, output.getRequestId());
//				writeStr(buf, output.getType());
//				writeStr(buf, JSON.toJSONString(output.getPayload()));
//				
//				DatagramPacket datagramPacket = new DatagramPacket(buf, inetSocketAddress);
//		        channel.writeAndFlush(datagramPacket).sync();
//		        if (!channel.closeFuture().await(3000)) {
//		            logger.warn("connect failed");
//		        }else {
//		    		started = true;
//		        }

	}

	public void reconnect() {
		// if (stopped) {
		// return;
		// }
		// bootstrap.connect(ip, port).addListener(future -> {
		// if (future.isSuccess()) {
		// return;
		// }
		// if (!stopped) {
		// group.schedule(() -> {
		// reconnect();
		// }, 1, TimeUnit.SECONDS);
		// }
		// logger.error("connect {}:{} failure", ip, port, future.cause());
		// });
	}

	public void close() {
		stopped = true;
		channel.close();
		group.shutdownGracefully(0, 5000, TimeUnit.MILLISECONDS);
	}

//	public Long fib(int n) {
//		return (Long) udpClientHandler.send("fib", n);
//	}


}
