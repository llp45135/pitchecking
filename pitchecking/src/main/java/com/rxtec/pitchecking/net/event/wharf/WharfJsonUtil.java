package com.rxtec.pitchecking.net.event.wharf;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.IDCardUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class WharfJsonUtil {
	private static Logger log = LoggerFactory.getLogger("WharfJsonUtil");

	public static void main(String[] args) {

		try {
			String ticketNo = "0014152047";
			IDCard idCard = IDCardUtil.createIDCard("zp.jpg");
			float verifyResult = 0.66f;
			int isVerifyPassed = 1;
			String pitDate = CalUtils.getStringDateShort2();
			System.out.println("" + createWharfReceiptJson(2,ticketNo,"127.0.0.1",pitDate,CalUtils.getStringDateHaomiao(),verifyResult,isVerifyPassed,idCard, idCard.getCardImageBytes(), idCard.getCardImageBytes(), idCard.getCardImageBytes()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 生成createWharfReceiptJson
	 * 
	 * @param region
	 * @return
	 */
	public static String createWharfReceiptJson(int msgType,String ticketNo,String ipAddress,String pitDate,String pitTime,float verifyResult,int isVerifyPassed,IDCard idCard, byte[] idCardImgArray, byte[] faceImgArray, byte[] frameImgArray) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			PhotoReceiptBean bean = new PhotoReceiptBean();
			bean.setMsgType(msgType);
			bean.setTicketNo(ticketNo);
			bean.setIpAddress(ipAddress);
			bean.setPitDate(pitDate);
			bean.setPitTime(pitTime);
			bean.setVerifyResult(verifyResult);
			bean.setIsVerifyPassed(isVerifyPassed);
			bean.setIdCardNo(idCard.getIdNo());
			bean.setGender(idCard.getGender());
			bean.setAge(idCard.getAge());
			bean.setIdbirth(idCard.getIDBirth());
			bean.setIdnation(idCard.getIDNation());
			bean.setCitizenName(idCard.getPersonName());
			bean.setIddwelling(idCard.getIDDwelling());
			bean.setIdissue(idCard.getIDIssue());
			bean.setIdefficb(idCard.getIDEfficb());
			bean.setIdeffice(idCard.getIDEffice());
			if (idCardImgArray != null)
				bean.setIdPicImageBase64(BASE64Util.encryptBASE64(idCardImgArray));
			if (faceImgArray != null)
				bean.setFaceImageBase64(BASE64Util.encryptBASE64(faceImgArray));
			if (frameImgArray != null)
				bean.setFrameImageBase64(BASE64Util.encryptBASE64(frameImgArray));

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMOpenJson:", e);
		}
		return jsonStr;
	}

}
