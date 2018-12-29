package com.cscecee.basesite.core.udp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.UdpEndPoint;

public class UdpServer extends UdpEndPoint {

	private final static Logger logger = LoggerFactory.getLogger(UdpServer.class);

	private int port;

	public UdpServer(int port) {
		this.port = port;
		this.init();
	}



}
