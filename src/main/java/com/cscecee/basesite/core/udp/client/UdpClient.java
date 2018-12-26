package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.CommonMessage;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.test.FibRespHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.Charsets;
import lianxi.tcp.common.MessageOutput;
import lianxi.tcp.common.MessageRegistry;
import lianxi.tcp.common.RequestId;
import lianxi.tcp.demo.ExpRequest;
import lianxi.tcp.demo.ExpResponse;
import lianxi.udp.common.UdpMessageDecoder;
import lianxi.udp.common.UdpMessageEncoder;

public class UdpClient {

	private final static Logger logger = LoggerFactory.getLogger(UdpClient.class);
	// 服务器名称
	private String serverName;
	// 服务器端口
	private int serverPort;
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
	private boolean started;


	private boolean stopped;
	private UdpClientHandler udpClientHandler;
	private ConcurrentMap<String, RpcFuture<?>> pendingTasks = new ConcurrentHashMap<>();
	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	private InetSocketAddress inetSocketAddress = null;

	private MessageHandlers handlers = new MessageHandlers();

	public UdpClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.init();
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/*
	 * 注册服务的快捷方式
	 */
	public void register(String type,  IMessageHandler<?> handler) {
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
		inetSocketAddress = new InetSocketAddress(serverName, serverPort);
		udpClientHandler = new UdpClientHandler(inetSocketAddress, handlers , 10);
		// bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 将业务处理器放到最后
			    pipe.addLast(udpClientHandler);

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
	public <T> RpcFuture<T> sendAsync(String type, Object payload) {
		if (!started) {
			connect();
			started = true;
		}
		String requestId = RequestId.next();
		CommonMessage output = new CommonMessage(requestId, type, payload);
		return udpClientHandler.send(output);
	}

	/**
	 * 同步发送
	 * @param type
	 * @param payload
	 * @return
	 */
	public <T> T send(String type, Object payload) {
		// 普通rpc请求,正常获取相应
		RpcFuture<T> future = sendAsync(type, payload);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RPCException(e);
		}
	}


	/**
	 * udp连接到服务器， udp 发送心跳包，且有回应
	 *
	 */
	public void connect() {
		try {
			if (channel == null || !channel.isActive()) {
				ChannelFuture channelFuture = bootstrap.bind(0).sync();
				channel = channelFuture.channel();
				
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
		} catch (Exception e) {
			e.printStackTrace();
		}
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
