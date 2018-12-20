package com.zdy.netty.logudp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * <B>说 明<B/>:
 * 
 * @author 作者名：冯龙淼 E-mail：fenglongmiao@vrvmail.com.cn
 *
 * @version 版 本 号：1.0.<br/>
 *          创建时间：2017年12月21日 下午4:20:49
 */
public class LogPushUdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private static final Logger logger = Logger.getLogger(LogPushUdpClientHandler.class);

	// private static Channel channel = LogPushUdpClient.getInstance().getChannel();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 当channel就绪后。
		logger.info("client channel is ready!");
		// ctx.writeAndFlush("started");//阻塞直到发送完毕 这一块可以去掉的
		// NettyUdpClientHandler.sendMessage("你好UdpServer", new
		// InetSocketAddress("127.0.0.1",8888));
		// sendMessageWithInetAddressList(message);
		// logger.info("client send message is: 你好UdpServer");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
		// TODO 不确定服务端是否有response 所以暂时先不用处理
		final ByteBuf buf = packet.content();
		int readableBytes = buf.readableBytes();
		byte[] content = new byte[readableBytes];
		buf.readBytes(content);
		String serverMessage = new String(content);
		logger.info("reserveServerResponse is: " + serverMessage);
	}

	/**
	 * 向服务器发送消息
	 * 
	 * @param msg
	 *            按规则拼接的消息串
	 * @param inetSocketAddress
	 *            目标服务器地址
	 */
	public static void sendMessage(final String msg, final InetSocketAddress inetSocketAddress) {
		if (msg == null) {
			throw new NullPointerException("msg is null");
		}
		// TODO 这一块的msg需要做处理 字符集转换和Bytebuf缓冲区
		senderInternal(datagramPacket(msg, inetSocketAddress));
	}

	/**
	 * 发送数据包并监听结果
	 * 
	 * @param datagramPacket
	 */
	public static void senderInternal(final DatagramPacket datagramPacket, List<Channel> channelList) {
		for (Channel channel : channelList) {
			if (channel != null) {
				channel.writeAndFlush(datagramPacket).addListener(new GenericFutureListener<ChannelFuture>() {
					public void operationComplete(ChannelFuture future) throws Exception {
						boolean success = future.isSuccess();
						if (logger.isInfoEnabled()) {
							logger.info("Sender datagramPacket result : " + success);
						}
					}
				});
			}
		}
	}

	/**
	 * 组装数据包
	 * 
	 * @param msg
	 *            消息串
	 * @param inetSocketAddress
	 *            服务器地址
	 * @return DatagramPacket
	 */
	private static DatagramPacket datagramPacket(String msg, InetSocketAddress inetSocketAddress) {
		ByteBuf dataBuf = Unpooled.copiedBuffer(msg, Charset.forName("UTF-8"));
		DatagramPacket datagramPacket = new DatagramPacket(dataBuf, inetSocketAddress);
		return datagramPacket;
	}

	/**
	 * 发送数据包服务器无返回结果
	 * 
	 * @param datagramPacket
	 */
	private static void senderInternal(final DatagramPacket datagramPacket) {
		logger.info("LogPushUdpClient.channel" + LogPushUdpClient.channel);
		if (LogPushUdpClient.channel != null) {
			LogPushUdpClient.channel.writeAndFlush(datagramPacket)
					.addListener(new GenericFutureListener<ChannelFuture>() {

						public void operationComplete(ChannelFuture future) throws Exception {
							boolean success = future.isSuccess();
							if (logger.isInfoEnabled()) {
								logger.info("Sender datagramPacket result : " + success);
							}
						}
					});
		} else {
			throw new NullPointerException("channel is null");
		}
	}

}
