package lianxi.udp.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public class TestUdpClient {
	public void run(int port, String context) throws Exception {

		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
					.handler(new TestUdpClientHandler());
			Channel ch = b.bind(0).sync().channel();

			ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(context, CharsetUtil.UTF_8),
					new InetSocketAddress("locahost", port)));

			if (!ch.closeFuture().await(5000)) {
				System.out.println("查询超时");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8800;
		String context = "qwww";
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		new TestUdpClient().run(port, context);
	}

}
