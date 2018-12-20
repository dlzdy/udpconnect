package com.zdy.netty.logudp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.SocketAddress;
import java.nio.charset.Charset;


/**
 * 	 <B>说	明<B/>:LogPush UDP client
 * 
 * @author 作者名：冯龙淼
 * 		   E-mail：fenglongmiao@vrvmail.com.cn
 * 
 * @version 版   本  号：1.0.<br/>
 *          创建时间：2017年12月28日 下午3:37:53
 */
public class LogPushUdpClient {
	
	private final Bootstrap bootstrap;
	public final NioEventLoopGroup workerGroup;
	public static Channel channel;
	private static final Charset ASCII = Charset.forName("ASCII"); 
	
	public void start() throws Exception{
//        try {
//        	SocketAddress sss = new SocketAddress(); 
//			channel = bootstrap.bind(sss ).sync().channel();
//        	channel.closeFuture().await(1000);
//        } finally {
////        	workerGroup.shutdownGracefully();
//        }
	}
	
	public Channel getChannel(){
		return channel;
	}
	
	public static LogPushUdpClient getInstance(){
		return logPushUdpClient.INSTANCE;
	}
	
	private static final class logPushUdpClient{
		static final LogPushUdpClient INSTANCE = new LogPushUdpClient();
	}
	
	private LogPushUdpClient(){
		bootstrap = new Bootstrap();
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, true)
        .handler(new ChannelInitializer<NioDatagramChannel>() {
			@Override
			protected void initChannel(NioDatagramChannel ch)throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
//				pipeline.addLast(new StringDecoder(ASCII))  
//                .addLast(new StringEncoder(ASCII))
				pipeline.addLast(new LogPushUdpClientHandler());
			}
		});
	}
	
}
