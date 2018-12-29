package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.MessageCommon;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.common.MessageReq;
import com.cscecee.basesite.core.udp.common.UdpEndPoint;
import com.cscecee.basesite.core.udp.common.UdpMessageHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.client.RpcFuture;
import lianxi.tcp.common.RequestId;

public class UdpClient extends UdpEndPoint {
	private final static Logger logger = LoggerFactory.getLogger(UdpClient.class);

	private InetSocketAddress remoteSocketAddress = null;

	public UdpClient(String serverName, int serverPort, String myId) {
		remoteSocketAddress = new InetSocketAddress(serverName, serverPort);
		this.myId = myId;
		this.init();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	/**
	 * 适用于客户端-->服务器
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
	public <T> T send(String command, boolean isCompressed, byte[] data) {
		RpcFuture<T> future = sendAsync(command, isCompressed, data);
		try {
			return future.get();
		} catch (Exception e) {
			throw new RPCException(e);
		}
	}
	
	/**
	 * 异步发送
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
	private <T> RpcFuture<T> sendAsync(String command, boolean isCompressed, byte[] data) {
		if (!started) {// 未连接
			try {
				bind(0);
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
				started = false;
			}
		}
		MessageReq output = new MessageReq(RequestId.next(), myId, command, isCompressed, data);
		return udpMessageHandler.send(getRemoteSocketAddress(), output);
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
}
