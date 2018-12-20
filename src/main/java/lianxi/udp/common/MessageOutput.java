package lianxi.udp.common;

/**
 * 输出消息
 * @author zhangdy
 *
 */
public class MessageOutput {
	/**
	 * 消息ID
	 */
	private String requestId;
	/**
	 * 消息类型
	 */
	private String type;
	/**
	 * 消息净荷
	 */
	private Object payload;

	public MessageOutput(String requestId, String type, Object payload) {
		this.requestId = requestId;
		this.type = type;
		this.payload = payload;
	}

	public String getType() {
		return this.type;
	}

	public String getRequestId() {
		return requestId;
	}

	public Object getPayload() {
		return payload;
	}

}
