package com.zdy.netty.logudp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * 	 <B>说	明<B/>:UdpServer
 * 
 * @author 作者名：冯龙淼
 * 		   E-mail：fenglongmiao@vrvmail.com.cn
 * 
 * @version 版   本  号：1.0.<br/>
 *          创建时间：2018年1月8日 上午10:03:38
 */
public class UdpServer {

	private final Bootstrap bootstrap;
	private final NioEventLoopGroup acceptGroup;
	private Channel channel;
	public void start(String host,int port) throws Exception{
        try {
        	channel = bootstrap.bind(host, port).sync().channel();
        	System.out.println("UdpServer start success"+port);
        	channel.closeFuture().await();
        } finally {
            acceptGroup.shutdownGracefully();
        }
	}
	
	public Channel getChannel(){
		return channel;
	}
	
	public static UdpServer getInstance(){
		return UdpServerHolder.INSTANCE;
	}
	
	private static final class UdpServerHolder{
		static final UdpServer INSTANCE = new UdpServer();
	}
	
	private UdpServer(){
		bootstrap = new Bootstrap();
		acceptGroup = new NioEventLoopGroup();
		bootstrap.group(acceptGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, true)
        .handler(new ChannelInitializer<NioDatagramChannel>() {
			@Override
			protected void initChannel(NioDatagramChannel ch)
					throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new UdpServerHandler());
			}
		});
	}
}
