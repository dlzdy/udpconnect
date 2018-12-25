package lianxi.udp.server;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.IMessageHandler;
import lianxi.tcp.common.MessageHandlers;
import lianxi.tcp.common.MessageRegistry;
import lianxi.tcp.demo.ExpRequest;
import lianxi.tcp.demo.ExpRequestHandler;
import lianxi.tcp.demo.FibRequestHandler;
import lianxi.tcp.server.DefaultHandler;
import lianxi.tcp.server.RPCServer;
import lianxi.udp.common.UdpMessageDecoder;
import lianxi.udp.common.UdpMessageEncoder;

public class UdpRpcServer {

	private final static Logger logger = LoggerFactory.getLogger(RPCServer.class);
	// 服务器端口
	private int port;

	private Bootstrap bootstrap;
	private EventLoopGroup eventLoopGroup;
	private Channel channel;
	private MessageHandlers handlers = new MessageHandlers();
	private MessageRegistry registry = new MessageRegistry();
	// <appid, InetSocketAddress>
	public static ConcurrentMap<String, InetSocketAddress> clienttMap = new ConcurrentHashMap<>();

	private int workerThreads = 10;  //用来业务处理的计算线程
	{
		handlers.defaultHandler(new DefaultHandler());
	}

	
	public UdpRpcServer(int port) {
		this.port = port;
	}

	private UdpServerMessageHandler collector;

	// private Channel serverChannel;
	// 注册服务的快捷方式
	public UdpRpcServer service(String type, Class<?> reqClass, IMessageHandler<?> handler) {
		registry.register(type, reqClass);
		handlers.register(type, handler);
		return this;
	}

	/**
	 * 启动服务器
	 */
	public void start() {
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 1.设置bossGroup和workGroup
		bootstrap.group(eventLoopGroup);
		// 2.指定使用NioServerSocketChannel来处理连接请求。
		bootstrap.channel(NioDatagramChannel.class);
		// 3.配置TCP/UDP参数。
		bootstrap.option(ChannelOption.SO_BROADCAST, true);
		// for test
		// bootstrap.handler(new UdpServerHandler());
		collector = new UdpServerMessageHandler(handlers, registry, workerThreads);
		// 4.配置handler 数据处理器。
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				super.channelActive(ctx);
			}
			
			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 如果客户端30秒没有任何请求,就关闭客户端连接
				//pipe.addLast(new IdleStateHandler(30, 30, 30));
				// 解码器
				pipe.addLast(new UdpMessageDecoder());
				// 编码器
				pipe.addLast(new UdpMessageEncoder(null));
				// 将业务处理器放到最后
				pipe.addLast(collector);
				//pipe.addLast(new UdpServerHandler());
			}

		});

		// 5.bootstrap启动服务器。
		try {
			ChannelFuture sync = bootstrap.bind(port).sync();
			logger.info("udp server is running ......");
			channel = sync.channel();
			channel.closeFuture().await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		UdpRpcServer rpcServer = new UdpRpcServer(8800);
		rpcServer.service("fib", Integer.class, new FibRequestHandler());
		rpcServer.service("exp", ExpRequest.class,new ExpRequestHandler());
		rpcServer.start();
		// rpcServer.start2();
	}

}
