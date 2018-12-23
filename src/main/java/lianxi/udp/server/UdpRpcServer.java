package lianxi.udp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zln.netty.five.part07.UdpServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lianxi.tcp.common.IMessageHandler;
import lianxi.tcp.common.MessageDecoder;
import lianxi.tcp.common.MessageEncoder;
import lianxi.tcp.common.MessageHandlers;
import lianxi.tcp.common.MessageRegistry;
import lianxi.tcp.server.DefaultHandler;
import lianxi.tcp.server.MessageCollector;
import lianxi.tcp.server.RPCServer;

public class UdpRpcServer {
	
	private final static Logger logger = LoggerFactory.getLogger(RPCServer.class);
	// 服务器端口
	private int port;

	ServerBootstrap serverBootstrap;
	EventLoopGroup bossGroup;
	EventLoopGroup workGroup;
	private Channel channel;	
	private MessageHandlers handlers = new MessageHandlers();
	private MessageRegistry registry = new MessageRegistry();
	{
		handlers.defaultHandler(new DefaultHandler());
	}

	public UdpRpcServer(int port) {
		this.port = port;
	}
	private MessageCollector collector;
//	private Channel serverChannel;
	//注册服务的快捷方式
	public UdpRpcServer service(String type, Class<?> reqClass, IMessageHandler<?> handler) {
		registry.register(type, reqClass);
		handlers.register(type, handler);
		return this;
	}
	/**
	 * 启动服务器
	 */
	public void start() {
		serverBootstrap = new ServerBootstrap();
		// 1.设置bossGroup和workGroup
		bossGroup = new NioEventLoopGroup();
		workGroup = new NioEventLoopGroup();
		serverBootstrap.group(workGroup);
		//serverBootstrap.group(bossGroup, workGroup);
		// 2.指定使用NioServerSocketChannel来处理连接请求。
		serverBootstrap.channel(NioServerSocketChannel.class);
		// 3.配置TCP/UDP参数。
		serverBootstrap.option(ChannelOption.SO_BROADCAST, true);
		
		// 4.配置handler和childHandler，数据处理器。
		//serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
		serverBootstrap.handler(new UdpServerHandler());
		
//		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
//
//			@Override
//			protected void initChannel(SocketChannel ch) throws Exception {
//				// 注册hander
//				ChannelPipeline pipe = ch.pipeline();
//				// 如果客户端30秒没有任何请求,就关闭客户端连接
//				pipe.addLast(new IdleStateHandler(30,30,30));
//				// 加解码器
//				pipe.addLast(new MessageDecoder());
//				// 编码器
//				pipe.addLast(new MessageEncoder());
//				// 将业务处理器放到最后
//				pipe.addLast(collector);
//			}
//			
//		});
		// 5.ServerBootstrap启动服务器。
		try {
			channel = serverBootstrap.bind(port).sync().channel();
			channel.closeFuture().await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		UdpRpcServer rpcServer = new UdpRpcServer(8800);
		rpcServer.start();
	}

}
