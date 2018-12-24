package lianxi.udp.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.zdy.netty.logudp.LogPushUdpClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GenericFutureListener;
import lianxi.tcp.client.MessageCollector;
import lianxi.tcp.client.RPCClient;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.Charsets;
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
	//
	private Channel channel;
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
		//bootstrap.handler(new LoggingHandler(LogLevel.INFO));
//		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
//
//			@Override
//			protected void initChannel(NioDatagramChannel ch) throws Exception {
//				// 注册hander
//				ChannelPipeline pipe = ch.pipeline();
//				// 如果客户端30秒没有任何请求,就关闭客户端连接
//				pipe.addLast(new IdleStateHandler(30, 30, 30));
//				// 加解码器
//				pipe.addLast(new MessageDecoder());
//				// 编码器
//				pipe.addLast(new MessageEncoder());
//				// 将业务处理器放到最后
//				pipe.addLast(collector);
//
//			}
//
//		});
		
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				super.channelActive(ctx);
			}

			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				ChannelPipeline cp = ch.pipeline();
//				cp.addLast(new MessageToMessageDecoder<DatagramPacket>() {
//					@Override
//					protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out)
//							throws Exception {
//						out.add(msg.content().toString(Charset.forName("UTF-8")));
//					}
//				});
				cp.addLast(new MessageEncoder());				
//				cp.addLast(new MessageToMessageEncoder<DatagramPacket>() {
//					@Override
//					protected void encode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out)
//							throws Exception {
//						out.add(msg.content().toString(Charset.forName("UTF-8")));						
//					}
//				});				
				// cp.addLast("handler", new UdpServerHandler());
			}
		});		
	}

	/**
	 * udp连接到服务器，
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
		InetSocketAddress inetSocketAddress = null;
		if (channel == null || !channel.isActive()) {
			channel=bootstrap.bind(0).sync().channel();
			inetSocketAddress = new InetSocketAddress(serverName, serverPort);
			channel.connect(inetSocketAddress);
		}
		// channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("QUERY",
		// CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", port))).sync();
		// if (!channel.closeFuture().await(15000)) {
		// System.out.println("out of time");
		// }
		//channel.closeFuture().await(1000);
		
		String requestId = RequestId.next();
		MessageOutput output = new MessageOutput(requestId, "heartbeat", "");
		channel.writeAndFlush(output);
        if (!channel.closeFuture().await(3000)) {
            logger.info("查询超时");
        }else {
    		started = true;
        }
        
        
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, output.getRequestId());
//		writeStr(buf, output.getType());
//		writeStr(buf, JSON.toJSONString(output.getPayload()));
		
//		MessageOutput output = new MessageOutput(requestId, "heartbeat", "");
//		DatagramPacket datagramPacket = new DatagramPacket(buf, inetSocketAddress);
//        channel.writeAndFlush(datagramPacket).sync();
//        if (!channel.closeFuture().await(3000)) {
//            logger.info("查询超时");
//        }else {
//    		started = true;
//        }
        
//		channel.writeAndFlush(datagramPacket).addListener(new GenericFutureListener<ChannelFuture>() {
//			public void operationComplete(ChannelFuture future) throws Exception {
//				boolean success = future.isSuccess();
//				if (logger.isInfoEnabled()) {
//					logger.info("Sender datagramPacket result : " + success);
//				}
//			}
//		});
		


//		String requestId = RequestId.next();
//		MessageOutput output = new MessageOutput(requestId, "heartbeat", "");
//		//MessageOutput output = new MessageOutput(requestId, "fib", 1);
//		channel.writeAndFlush(output);
	}
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
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
