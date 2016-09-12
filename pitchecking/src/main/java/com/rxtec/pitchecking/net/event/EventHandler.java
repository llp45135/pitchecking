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

public class EventHandler {

	private ObjectMapper mapper = new ObjectMapper();
	private JsonFactory f = mapper.getFactory();
	private Logger log = LoggerFactory.getLogger("EventHandler");
	private VerifyFaceTaskForTKVersion verifyFaceTask = new VerifyFaceTaskForTKVersion();

	private String getEventName(String json) throws JsonParseException, IOException {
		String eventName = "";
		JsonParser jParser = f.createParser(json);
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			
			jParser.nextToken();
			String fieldname=jParser.getCurrentName();
			if ("eventName".equals(fieldname)) {
				jParser.nextToken();
				eventName=jParser.getText();
				log.debug("getEventName" + jParser.getText()); 
				break;
			}
		}
		return eventName;
	}

	private PIVerifyEventBean buildPIVerifyEventBean(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		PIVerifyEventBean b = mapper.readValue(jsonString, PIVerifyEventBean.class);
		return b;
	}



	public void InComeEventHandler(String jsonString) throws JsonParseException, IOException {

		String eventName = getEventName(jsonString);
		if (Config.BeginVerifyFaceEvent.equals(eventName)) {
			PIVerifyEventBean b = buildPIVerifyEventBean(jsonString);
			Ticket ticket = new Ticket();
			IDCard idCard = new IDCard();
			idCard.setCardImageBytes(b.getIdPhoto());
			verifyFaceTask.beginCheckFace(idCard, ticket, b.getDelaySeconds());
		}
	}

	public String OutputEventToJson(Object outputEvent) throws JsonProcessingException {
		return mapper.writeValueAsString(outputEvent);
	}
}
