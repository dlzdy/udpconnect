package com.cscecee.basesite.core.udp.common;

/**
 * 通用消息，
 * @author zhangdy
 *
 */
public class CommonMessage {
	/**
	 * 消息来源,server端=0，client=appid
	 */
	protected String fromId;
	/**
	 * 消息号
	 */
	protected String requestId;
	/**
	 * 消息类型
	 */
	protected String type;
	/**
	 * 消息净荷
	 */
	protected Object payload;

	public CommonMessage(String fromId, String requestId, String type, Object payload) {
		this.fromId = fromId;
		this.requestId = requestId;
		this.type = type;
		this.payload = payload;
	}
	
	public CommonMessage(String requestId, String type, Object payload) {
		this("0", requestId, type, payload);
	}
	
	public String getType() {
		return this.type;
	}

	public String getFromId() {
		return fromId;
	}

	public String getRequestId() {
		return requestId;
	}

	public Object getPayload() {
		return payload;
	}

}
