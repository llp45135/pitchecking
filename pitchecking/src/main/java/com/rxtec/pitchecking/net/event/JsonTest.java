package com.rxtec.pitchecking.net.event;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class JsonTest {

	public static void main(String[] args) {

		try {
//			testPIVerifyEventBean();
//			testPIVerifyResultBean();
			testPIVerifyRequestBean();
		} catch (Exception e) {
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
        BufferedImage bi = ImageIO.read(new File("D:/maven/git/zp.jpg"));
		b.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));
		b.setPersonName("");
//		b.setTicket(new Ticket());
		b.setUuid("111");
		
		String jsonString = mapper.writeValueAsString(b);
		System.out.println(jsonString);

		
		PIVerifyEventBean b1 = mapper.readValue(jsonString,PIVerifyEventBean.class);
		System.out.println(b1);
		
		byte[] ss = b1.getIdPhoto();
		CommUtil.byte2image(ss, "D:/maven/git/test.jpg");
	}
	
	public static void testPIVerifyRequestBean() throws IOException, JsonMappingException, Exception {
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = "{  \"eventDirection\" : 1,  \"eventName\" : \"CAM_GetPhotoInfo\",   \"uuid\":\"520203199612169998\",   \"iDelay\":10 }";
		PIVerifyRequestBean bean = mapper.readValue(jsonStr, PIVerifyRequestBean.class);
		System.out.println("getEventName=="+bean.getEventName());
		System.out.println("getUuid=="+bean.getUuid());
		System.out.println("getiDelay=="+bean.getiDelay());
		System.out.println("getEventDirection=="+bean.getEventDirection());
	}

	
	public static void testPIVerifyResultBean() throws IOException{
		ObjectMapper mapper = new ObjectMapper(); 
		PIVerifyResultBean b = new PIVerifyResultBean();
		b.setResult(100);
		b.setUuid("520203199612169998");
		b.setEventDirection(2);
		b.setEventName("CAM_GetPhotoInfo");

		BufferedImage bi;

		bi = ImageIO.read(new File("D:/maven/git/zl.jpg"));
		
		byte[] bb = CommUtil.image2byte("D:/maven/git/zp.jpg");
		
		byte[] biArray = ImageToolkit.getImageBytes(bi, "jpeg");
		
		b.setPhotoLen1(biArray.length);
		b.setPhoto1(biArray);
		b.setPhotoLen2(biArray.length);
		b.setPhoto2(biArray);
		b.setPhotoLen3(biArray.length);
		b.setPhoto3(biArray);
		
		String jsonString = mapper.writeValueAsString(b);
//		System.out.println(jsonString);

		PIVerifyResultBean b1 = mapper.readValue(jsonString,PIVerifyResultBean.class);
		System.out.println(b1);
		byte[] a1 = b1.getPhoto1();
		CommUtil.byte2image(a1, "D:/maven/git/a1.jpg");
		
		byte[] a2 = b1.getPhoto1();
		CommUtil.byte2image(a2, "D:/maven/git/a2.jpg");
		
		byte[] a3 = b1.getPhoto1();
		CommUtil.byte2image(a3, "D:/maven/git/a3.jpg");
		
//		EventHandler tool = new EventHandler();
		

		
	}

	
}
