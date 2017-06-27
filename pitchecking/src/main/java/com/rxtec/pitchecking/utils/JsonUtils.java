package com.rxtec.pitchecking.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

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
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.PITInfoJson;
import com.rxtec.pitchecking.mq.police.dao.PITInfoJmsObj;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

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

	public static void main(String[] args) {
		try {
			// Map map = JsonUtils.toMap(DeviceConfig.Close_SECONDDOOR_Jms);

//			System.out.println(DeviceConfig.Close_SECONDDOOR_Jms.replace(" ", ""));
//			String ss = DeviceConfig.Close_SECONDDOOR_Jms.replace(" ", "");
//
//			ss = ss.replace("{", "");
//			ss = ss.replace("}", "");
//			StringTokenizer st = new StringTokenizer(ss, ",");
//			while (st.hasMoreTokens()) {
//				String tt = st.nextToken();
//				System.out.println("" + tt);
//				if (tt.indexOf("Event") != -1) {
//					System.out.println("event==" + tt.substring(tt.indexOf(":") + 1).replace("\"", ""));
//				} else if (tt.indexOf("Target") != -1) {
//					System.out.println("Target==" + tt.substring(tt.indexOf(":") + 1).replace("\"", ""));
//				} else if (tt.indexOf("EventSource") != -1) {
//					System.out.println("EventSource==" + tt.substring(tt.indexOf(":") + 1).replace("\"", ""));
//				}
//			}
			
			IDCard idcard = new IDCard();
			idcard.setIdNo("520230197912141118");
			idcard.setGender(1);
			idcard.setAge(37);
			idcard.setPersonName("赵林");
			idcard.setIDBirth("19791214");
			idcard.setIDNation("01");
			idcard.setIDNationCH("汉");
			idcard.setIDDwelling("广州市白云路28号");
			idcard.setIDIssue("广州市公安局越秀分局");
			idcard.setIDEfficb("20001230");
			idcard.setIDEffice("20201230");
			
			BufferedImage frame = null;
			try {
				frame = ImageIO.read(new File("idcard_test.jpg"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BufferedImage face = null;
			try {
				face = ImageIO.read(new File("idcard_test.jpg"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			PITData data = new PITData(null);
			data.setIdCard(idcard);
			data.setFrame(frame);
			data.setFaceImage(face);
			
			PITVerifyData fd = new PITVerifyData(data);
			fd.setQrCode("");
			PITInfoJson info = new PITInfoJson(fd);
			System.out.println(""+info.getJsonStr());
			String jsonstr = info.getJsonStr();
//			String jsonstr = ProcessUtil.getRebackTrackAppFlag("D:/pitchecking/config/pitVerifyData.json");
			
			ObjectMapper mapper = new ObjectMapper();
			PITInfoJmsObj pitInfoJsonBean = mapper.readValue(jsonstr, PITInfoJmsObj.class);
			String idNo = new String(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdCardNo()));
			System.out.println("idNo=="+idNo);
			System.out.println("getMsgType==" + pitInfoJsonBean.getMsgType());
			System.out.println("getCitizenName==" + pitInfoJsonBean.getCitizenName());
			System.out.println("getIdbirth==" + pitInfoJsonBean.getIdbirth());
			System.out.println("getIdnation==" + pitInfoJsonBean.getIdnation());
			System.out.println("getIdissue==" + pitInfoJsonBean.getIdissue());
			System.out.println("getIddwelling==" + pitInfoJsonBean.getIddwelling());
			System.out.println("getIdefficb==" + pitInfoJsonBean.getIdefficb());
			System.out.println("getIdeffice==" + pitInfoJsonBean.getIdeffice());
			
			String dbcode = pitInfoJsonBean.getDbCode();
			String gateNo = pitInfoJsonBean.getGateNo();
			String pitInfoNewJson = mapper.writeValueAsString(pitInfoJsonBean);
			pitInfoNewJson = pitInfoNewJson.replace("\\r\\n", "");

			String dirName = Config.getInstance().getPoliceJsonDir() + CalUtils.getStringDateShort2()
					+ "/";
			int ret = CommUtil.createDir(dirName);
			if (ret == 0 || ret == 1) {
				String fileName = dirName + idNo + "_" + idNo + "_" + dbcode + "_" + gateNo + "_"
						+ CalUtils.getStringFullTimeHaomiao() + ".json";
				System.out.println("fileName==" + fileName);
				// ProcessUtil.writeFileContent(fileName,
				// pitInfoNewJson);
				ProcessUtil.writeFileContent(fileName, pitInfoNewJson, "utf-8");

				// File logFile = new File(fileName);
				// if (!logFile.exists()) {
				// logFile.createNewFile();
				// }
				// mapper.writeValue(logFile, pitInfoJsonBean);
			}

			// System.out.println(ss.indexOf("Event"));
			// Map map = JsonUtils.toMap(ss);
			// System.out.println("Event=="+map.get("Event"));
			// System.out.println("Target=="+map.get("Target"));
			// System.out.println("EventSource=="+map.get("EventSource"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
