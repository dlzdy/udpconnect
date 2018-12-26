package com.cscecee.basesite.core.udp.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息类型注册中心和消息处理器注册中心，
 *
 * @author zhangdy
 *
 */
public class MessageRegistry {
	
	private Map<String, Class<?>> clazzes = new HashMap<>();
	
	/**
	 * 消息类型注册
	 * @param type
	 * @param clazz
	 */
	public void register(String type, Class<?> clazz) {
		clazzes.put(type, clazz);
	}
	
	public Class<?> get(String type) {
		return clazzes.get(type);
	}
}
