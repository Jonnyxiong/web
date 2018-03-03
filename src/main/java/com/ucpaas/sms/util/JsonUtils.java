package com.ucpaas.sms.util;

import com.alibaba.fastjson.JSON;
import com.ucpaas.sms.model.SMSResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json字符串工具类
 */
public class JsonUtils {
	/**
	 * 将对象转换成为json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * 将json字符串转换成为对象
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> T toObject(String json, Class<T> classOfT) {
		return JSON.parseObject(json, classOfT);
	}
	
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "a");
		map.put("b", "b");
		map.put("c", "c");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(map);
		int i = 1;
		SMSResponse resp = new SMSResponse(i, list);
		String json = toJson(resp);
		
		System.out.println(json);
		SMSResponse resp2 = JsonUtils.toObject(json, SMSResponse.class);
		System.out.println(resp2);
		System.out.println(resp2.getData().get(0).get("a"));
	}
}
