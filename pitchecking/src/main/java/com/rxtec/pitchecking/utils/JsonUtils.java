package com.rxtec.pitchecking.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Json序列化工具
 * 
 * @author http://blog.csdn.net/xxd851116
 * @date 2014年3月26日 下午1:21:59
 */
public class JsonUtils {
	private static Logger logger = LoggerFactory.getLogger("JsonUtils");

	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 将对象序列化为JSON字符串
	 * 
	 * @param object
	 * @return JSON字符串
	 */
	public static String serialize(Object object) {
		Writer write = new StringWriter();
		try {
			objectMapper.writeValue(write, object);
		} catch (JsonGenerationException e) {
			logger.error("JsonGenerationException when serialize object to json", e);
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException when serialize object to json", e);
		} catch (IOException e) {
			logger.error("IOException when serialize object to json", e);
		}
		return write.toString();
	}

	/**
	 * 将JSON字符串反序列化为对象
	 * 
	 * @param object
	 * @return JSON字符串
	 */
	public static <T> T deserialize(String json, Class<T> clazz) {
		Object object = null;
		try {
			object = objectMapper.readValue(json, TypeFactory.rawClass(clazz));
		} catch (JsonParseException e) {
			logger.error("JsonParseException when serialize object to json", e);
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException when serialize object to json", e);
		} catch (IOException e) {
			logger.error("IOException when serialize object to json", e);
		}
		return (T) object;
	}

	/**
	 * 将JSON字符串反序列化为对象
	 * 
	 * @param object
	 * @return JSON字符串
	 */
	public static <T> T deserialize(String json, TypeReference<T> typeRef) {
		try {
			return (T) objectMapper.readValue(json, typeRef);
		} catch (JsonParseException e) {
			logger.error("JsonParseException when deserialize json", e);
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException when deserialize json", e);
		} catch (IOException e) {
			logger.error("IOException when deserialize json", e);
		}
		return null;
	}

	/**
	 * 将Javabean转换为Map
	 * 
	 * @param javaBean
	 *            javaBean
	 * @return Map对象
	 */
	public static Map toMap(Object javaBean) {

		Map result = new HashMap();
		Method[] methods = javaBean.getClass().getDeclaredMethods();

		for (Method method : methods) {

			try {

				if (method.getName().startsWith("get")) {

					String field = method.getName();
					field = field.substring(field.indexOf("get") + 3);
					field = field.toLowerCase().charAt(0) + field.substring(1);

					Object value = method.invoke(javaBean, (Object[]) null);
					result.put(field, null == value ? "" : value.toString());

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return result;

	}

	/**
	 * 将Json对象转换成Map
	 * 
	 * @param jsonObject
	 *            json对象
	 * @return Map对象
	 * @throws JSONException
	 */
	public static Map toMap(String jsonString) throws JSONException {

		JSONObject jsonObject = new JSONObject(jsonString);

		Map result = new HashMap();
		Iterator iterator = jsonObject.keys();
		String key = null;
		String value = null;

		while (iterator.hasNext()) {

			key = (String) iterator.next();
			value = jsonObject.getString(key);
			result.put(key, value);

		}
		return result;

	}

	/**
	 * 将JavaBean转换成JSONObject（通过Map中转）
	 * 
	 * @param bean
	 *            javaBean
	 * @return json对象
	 */
	public static JSONObject toJSON(Object bean) {

		return new JSONObject(toMap(bean));

	}

	/**
	 * 将Map转换成Javabean
	 * 
	 * @param javabean
	 *            javaBean
	 * @param data
	 *            Map数据
	 */
	public static Object toJavaBean(Object javabean, Map data) {

		Method[] methods = javabean.getClass().getDeclaredMethods();
		for (Method method : methods) {

			try {
				if (method.getName().startsWith("set")) {

					String field = method.getName();
					field = field.substring(field.indexOf("set") + 3);
					field = field.toLowerCase().charAt(0) + field.substring(1);
					method.invoke(javabean, new Object[] {

							data.get(field)

					});

				}
			} catch (Exception e) {
			}

		}

		return javabean;

	}

	/**
	 * JSONObject到JavaBean
	 * 
	 * @param bean
	 *            javaBean
	 * @return json对象
	 * @throws ParseException
	 *             json解析异常
	 * @throws JSONException
	 */
	public static void toJavaBean(Object javabean, String jsonString) throws ParseException, JSONException {

		JSONObject jsonObject = new JSONObject(jsonString);

		Map map = toMap(jsonObject.toString());

		toJavaBean(javabean, map);

	}
}
