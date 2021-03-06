package lianxi.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

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
	private UdpClientMessageHandler collector;
	private ConcurrentMap<String, RpcFuture<?>> pendingTasks = new ConcurrentHashMap<>();
	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	private InetSocketAddress inetSocketAddress = null;
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
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
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
		collector = new UdpClientMessageHandler(this);
		// bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		inetSocketAddress = new InetSocketAddress(serverName, serverPort);
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 如果客户端30秒没有任何请求,就关闭客户端连接
				pipe.addLast(new IdleStateHandler(30, 30, 30));
				// 加解码器
				pipe.addLast(new UdpMessageDecoder());
				// 编码器
				pipe.addLast(new UdpMessageEncoder(inetSocketAddress));
				// 将业务处理器放到最后
				 pipe.addLast(collector);

			}

		});

		// bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
		//
		// @Override
		// public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// super.channelActive(ctx);
		// }
		//
		// @Override
		// protected void initChannel(NioDatagramChannel ch) throws Exception {
		// ChannelPipeline cp = ch.pipeline();
		// cp.addLast(new MessageToMessageDecoder<DatagramPacket>() {
		// @Override
		// protected void decode(ChannelHandlerContext ctx, DatagramPacket msg,
		// List<Object> out)
		// throws Exception {
		// out.add(msg.content().toString(Charset.forName("UTF-8")));
		// }
		// });
		// cp.addLast(new MessageToMessageEncoder<DatagramPacket>() {
		// @Override
		// protected void encode(ChannelHandlerContext ctx, DatagramPacket msg,
		// List<Object> out)
		// throws Exception {
		// out.add(msg.content().toString(Charset.forName("UTF-8")));
		// }
		// });
		// // cp.addLast("handler", new UdpServerHandler());
		// }
		// });
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

//	private <T> RpcFuture<T> send(MessageOutput output) {
//		RpcFuture<T> future = new RpcFuture<T>();
//		if (channel != null) {
//			channel.eventLoop().execute(() -> {
//				pendingTasks.put(output.getRequestId(), future);
//				channel.writeAndFlush(output);
//			});
//		} else {
//			future.fail(ConnectionClosed);
//		}
//		return future;
//	}

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
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}
	public void heatbeat() throws InterruptedException {
		String requestId = RequestId.next();
		MessageOutput output = new MessageOutput(requestId, "heartbeat", "hello");
		collector.send(output);
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

	public long fib(int n) {
		return (Long) this.send("fib", n);
	}

	public ExpResponse exp(int base, int exp) {
		return (ExpResponse) this.send("exp", new ExpRequest(base, exp));
	}
	
	public static void main(String[] args) throws InterruptedException {
		UdpRpcClient udpClient = new UdpRpcClient("172.24.6.104", 8800);
		udpClient.init();
		udpClient.connect();
		//udpClient.heatbeat();
		udpClient.rpc("fib_res", Long.class);
		udpClient.rpc("exp_res", ExpResponse.class);
		
		for (int i = 0; i < 1; i++) {
			try {
				System.out.printf("fib(%d) = %d\n", i, udpClient.fib(i));
				Thread.sleep(100);
			} catch (RPCException e) {
				i--; // retry
			}
		}
		Thread.sleep(3000);
		for (int i = 0; i < 1; i++) {
			try {
				ExpResponse res = udpClient.exp(2, i);
				Thread.sleep(100);
				System.out.printf("exp2(%d) = %d cost=%dns\n", i, res.getValue(), res.getCostInNanos());
			} catch (RPCException e) {
				i--; // retry
			}
		}
		
		//udpClient.send("heatbeat", "test test");
		// udpClient.rpc("fib_res", Long.class);
		// udpClient.rpc("exp_res", ExpResponse.class);
	}

}
