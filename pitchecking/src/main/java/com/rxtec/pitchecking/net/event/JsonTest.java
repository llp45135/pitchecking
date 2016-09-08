package com.rxtec.pitchecking.net.event;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class JsonTest {

	public static void main(String[] args) {

		try {
			testPIVerifyEventBean();
			testPIVerifyResultBean();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void testPIVerifyEventBean() throws IOException{
		ObjectMapper mapper = new ObjectMapper(); 
		PIVerifyEventBean b = new PIVerifyEventBean();
		b.setAge(10);
		b.setEventDirection(1);
		b.setEventName("CAM_Notify");
		b.setGender(1);
        BufferedImage bi = ImageIO.read(new File("C:/pitchecking/llp.jpg"));
		b.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));
		b.setPersonName("");
//		b.setTicket(new Ticket());
		b.setUuid("111");
		
		String jsonString = mapper.writeValueAsString(b);
		System.out.println(jsonString);

		
		PIVerifyEventBean b1 = mapper.readValue(jsonString,PIVerifyEventBean.class);
		System.out.println(b1);
		
		
	}

	
	public static void testPIVerifyResultBean() throws IOException{
		ObjectMapper mapper = new ObjectMapper(); 
		PIVerifyResultBean b = new PIVerifyResultBean();
		b.setEventDirection(2);
		b.setEventName("CAM_GetPhotoInfo");
		b.setUuid("111");
        BufferedImage bi = ImageIO.read(new File("C:/pitchecking/llp.jpg"));
		b.setPhoto1(ImageToolkit.getImageBytes(bi, "jpeg"));
		b.setPhoto2(ImageToolkit.getImageBytes(bi, "jpeg"));
		b.setPhotoLen1(100);
		b.setPhotoLen2(100);
		b.setResult(100);
		
		String jsonString = mapper.writeValueAsString(b);
		System.out.println(jsonString);

		
		
		
		EventHandler tool = new EventHandler();
		

		
	}

	
}
