package lianxi.udp.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zdy.netty.logudp.LogPushUdpClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GenericFutureListener;
import lianxi.tcp.client.MessageCollector;
import lianxi.tcp.client.RPCClient;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.MessageDecoder;
import lianxi.tcp.common.MessageEncoder;
import lianxi.tcp.common.MessageOutput;
import lianxi.tcp.common.MessageRegistry;
import lianxi.tcp.common.RequestId;
import lianxi.tcp.demo.ExpResponse;

public class UdpRpcClient {

	private final static Logger logger = LoggerFactory.getLogger(UdpRpcClient.class);
	// 服务器名称
	private String serverName;
	// 服务器端口
	private int serverPort;
	//
	private Bootstrap bootstrap;
	//
	private EventLoopGroup group;
	// // 是否运行
	// private boolean isRunning;
	// // 是否连接
	// private boolean isConnected;
	private boolean started;
	private boolean stopped;
	private UdpMessageCollector collector;
	private MessageRegistry registry = new MessageRegistry();

	public UdpRpcClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.init();
	}

	public UdpRpcClient rpc(String type, Class<?> reqClass) {
		// rpc响应类型的注册快速入口
		registry.register(type, reqClass);
		return this;
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
		collector = new UdpMessageCollector(this);
		bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 如果客户端30秒没有任何请求,就关闭客户端连接
				pipe.addLast(new IdleStateHandler(30, 30, 30));
				// 加解码器
				pipe.addLast(new MessageDecoder());
				// 编码器
				pipe.addLast(new MessageEncoder());
				// 将业务处理器放到最后
				pipe.addLast(collector);

			}

		});
	}

	/**
	 * udp 发送心跳包，且有回应
	 *
	 */
	public void connect() {
		//
		try {
			heatbeat();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void heatbeat() throws InterruptedException {
		//
		//Channel channel = bootstrap.bind(9990).sync().channel();
		Channel channel=bootstrap.bind(0).sync().channel();
		// channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("QUERY",
		// CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", port))).sync();
		// if (!channel.closeFuture().await(15000)) {
		// System.out.println("out of time");
		// }
		//channel.closeFuture().await(1000);
		if(!channel.closeFuture().await(15000)){
			System.out.println("查询超时");
		}
		InetSocketAddress inetSocketAddress = new InetSocketAddress(serverName, serverPort);
		DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer("heartbeat".getBytes()),
				inetSocketAddress);
		channel.writeAndFlush(datagramPacket).addListener(new GenericFutureListener<ChannelFuture>() {
			public void operationComplete(ChannelFuture future) throws Exception {
				boolean success = future.isSuccess();
				if (logger.isInfoEnabled()) {
					logger.info("Sender datagramPacket result : " + success);
				}
			}
		});
		started = true;

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
		collector.close();
		group.shutdownGracefully(0, 5000, TimeUnit.MILLISECONDS);
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
		MessageOutput output = new MessageOutput(requestId, type, payload);
		return collector.send(output);
	}

	public <T> T send(String type, Object payload) {
		// 普通rpc请求,正常获取相应
		RpcFuture<T> future = sendAsync(type, payload);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RPCException(e);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		UdpRpcClient udpClient = new UdpRpcClient("localhost", 8800);
		udpClient.heatbeat();
//		udpClient.send("heatbeat", "test test");
//		udpClient.rpc("fib_res", Long.class);
//		udpClient.rpc("exp_res", ExpResponse.class);
	}

}
