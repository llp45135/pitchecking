package com.rxtec.pitchecking.net.event;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.VerifyFaceTaskForTKVersion;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class EventHandler {

	private ObjectMapper mapper = new ObjectMapper();
	private JsonFactory f = mapper.getFactory();
	private Logger log = LoggerFactory.getLogger("EventHandler");
	private VerifyFaceTaskForTKVersion verifyFaceTask = new VerifyFaceTaskForTKVersion();

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private String getEventName(String json) throws JsonParseException, IOException {
		String eventName = "";
		JsonParser jParser = f.createParser(json);
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			
			jParser.nextToken();
			String fieldname=jParser.getCurrentName();
			if ("eventName".equals(fieldname)) {
				jParser.nextToken();
				eventName=jParser.getText();
				break;
			}
		}
		return eventName;
	}

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private PIVerifyEventBean buildPIVerifyEventBean(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		PIVerifyEventBean b = mapper.readValue(jsonString, PIVerifyEventBean.class);
		return b;
	}

	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private PITVerifyData buildPITVerifyData(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		PITVerifyData b = mapper.readValue(jsonString, PITVerifyData.class);
		return b;
	}


	/**
	 * 
	 * @param jsonString
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public void InComeEventHandler(String jsonString) throws JsonParseException, IOException {
		String eventName = getEventName(jsonString);
		if (Config.BeginVerifyFaceEvent.equals(eventName)) {
			log.info("收到调用CAM_GetPhotoInfo方式的请求开始人脸检测的event");
			PIVerifyEventBean b = buildPIVerifyEventBean(jsonString);
			Ticket ticket = new Ticket();
			IDCard idCard = new IDCard();
			idCard.setIdNo(b.getUuid());
			idCard.setAge(b.getAge());
			idCard.setCardImageBytes(b.getIdPhoto());
			
			verifyFaceTask.beginCheckFace(idCard, ticket, 0);
		}else if(Config.GetVerifyFaceResultInnerEvent.equals(eventName)) {
//			log.debug("fd json=="+jsonString);
			PITVerifyData fd = buildPITVerifyData(jsonString);
			FaceCheckingService.getInstance().offerPassFaceData(fd);
		}
	}

	/**
	 * 
	 * @param outputEvent
	 * @return
	 * @throws JsonProcessingException
	 */
	public String OutputEventToJson(Object outputEvent) throws JsonProcessingException {
		return mapper.writeValueAsString(outputEvent);
	}
}
