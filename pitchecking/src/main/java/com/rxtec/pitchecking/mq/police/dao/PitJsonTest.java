package com.rxtec.pitchecking.mq.police.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PitJsonTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		PITInfoJmsObj pitInfoJsonBean = new PITInfoJmsObj();

		try {
			String pitInfoNewJson = mapper.writeValueAsString(pitInfoJsonBean);
			System.out.println("pitInfoNewJson==" + pitInfoNewJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
