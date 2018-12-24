package org.zln.netty.five.part07;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpServer {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(UdpServer.class);

    public void run(int port){
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioDatagramChannel.class);
            bootstrap.option(ChannelOption.SO_BROADCAST,true);
            bootstrap.handler(new UdpServerHandler());
            
            bootstrap.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) {
		UdpServer server = new UdpServer();
		server.run(8900);
	}
}