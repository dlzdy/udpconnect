package org.zln.netty.five.part07;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpClient {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(UdpClient.class);

    public void run(int port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpClientHandler());


            Channel channel = bootstrap.bind(0).sync().channel();
            //向网段内的所有机器广播
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语字典查询?", CharsetUtil.UTF_8), new InetSocketAddress("localhost", port))).sync();
            if (!channel.closeFuture().await(3000)) {
                logger.info("查询超时");
            }



        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) {
    	UdpClient client = new UdpClient();
    	//client.run(8900);
    	client.run(8800);
	}
}