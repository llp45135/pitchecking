package com.rxtec.pitchecking.net.event;

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
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.IDCardUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class CAMJsonUtil {
	private static Logger log = LoggerFactory.getLogger("CAMJsonUtil");

	public static void main(String[] args) {

		try {
			int x = 0; // 窗口左边界
			int y = 0; // 窗口顶边界。
			int cx = 640; // 以像素指定窗口的新的宽度。
			int cy = 480; // 以像素指定窗口的新的高度。
			int checkThreshold = 65; // 人脸比对阀值，取值0-100
			int iCollect = 1; // 摄像头采集图片方式 0:固采 1：预采
			int faceTimeout = Config.getInstance().getFaceCheckDelayTime(); // 人脸识别算法的超时时间，单位秒

			int[] region = { x, y, cx, cy, checkThreshold, iCollect, faceTimeout };

			System.out.println("" + createCAMOpenJson(region));

			IDCard idcard = IDCardUtil.createIDCard("zp.jpg");

			System.out.println("" + createCAMNotifyJson(1, idcard));

			System.out.println(createCAMNotifyZTCJson(1, "520203197912141118", "zp.jpg"));

			ObjectMapper mapper = new ObjectMapper();
			CAMNotifyBean b1 = mapper.readValue(createCAMNotifyJson(1, idcard), CAMNotifyBean.class);
			System.out.println("getUuid==" + b1.getUuid());
			System.out.println("getPersonName==" + b1.getPersonName());
			System.out.println("getAge==" + b1.getAge());
			System.out.println("getGender==" + b1.getGender());
			System.out.println("getIdBirth==" + b1.getIdBirth());
			System.out.println("getIdNation==" + b1.getIdNation());
			System.out.println("getIdDwelling==" + b1.getIdDwelling());
			System.out.println("getIdEfficb==" + b1.getIdEfficb());
			System.out.println("getIdEffice==" + b1.getIdEffice());
			System.out.println("getIdIssue==" + b1.getIdIssue());
			System.out.println("getIdPhoto.length==" + b1.getIdPhoto().length);

			System.out.println(createCAM_GetPhotoInfoRequest("520203197912141118", 10));

			System.out.println(createCAM_ScreenDisplay("人证核验成功#请通过", 5));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 生成CAM_Open的json
	 * 
	 * @param region
	 * @return
	 */
	public static String createCAMOpenJson(int[] region) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			CAMOpenBean bean = new CAMOpenBean();
			bean.setEventDirection(1);
			bean.setEventName("CAM_Open");
			bean.setThreshold(region[4]);
			bean.setTimeout(region[6]);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMOpenJson:", e);
		}
		return jsonStr;
	}

	/**
	 * 生成CAM_Notify的json
	 * 
	 * @param iFlag
	 * @param uuidStr
	 * @param photoDir
	 * @return
	 */
	public static String createCAMNotifyJson(int iFlag, String uuidStr, String photoDir) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			CAMNotifyBean bean = new CAMNotifyBean();
			// bean.setAge(10);
			bean.setEventDirection(iFlag);
			bean.setEventName("CAM_Notify");
			bean.setGender(1);
			BufferedImage bi;
			bi = ImageIO.read(new File(photoDir));
			bean.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));
			bean.setPersonName("");
			bean.setTicket(new Ticket());
			bean.setUuid(uuidStr);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMNotifyJson:", e);
		}
		return jsonStr;
	}

	/**
	 * 生成中铁程统一软件能够调用的CAM_notify的json
	 * 
	 * @param iFlag
	 * @param uuidStr
	 * @param photoDir
	 * @return
	 */
	public static String createCAMNotifyZTCJson(int iFlag, String uuidStr, String photoDir) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			CAMNotifyZTCBean bean = new CAMNotifyZTCBean();
			// bean.setAge(10);
			bean.setEventDirection(iFlag);
			bean.setEventName("CAM_Notify");
			bean.setGender(1);
			BufferedImage bi;
			bi = ImageIO.read(new File(photoDir));
			bean.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));
			bean.setPersonName("");
			bean.setTicket(new Ticket());
			bean.setUuid(uuidStr);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMNotifyZTCJson:", e);
		}
		return jsonStr;
	}

	/**
	 * 根据完整二代证信息生成CAM_notify的json
	 * 
	 * @param iFlag
	 * @param idcard
	 * @return
	 */
	public static String createCAMNotifyJson(int iFlag, IDCard idcard) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			CAMNotifyBean bean = new CAMNotifyBean();
			// bean.setAge(10);
			bean.setEventDirection(iFlag);
			bean.setEventName("CAM_Notify");
			bean.setUuid(idcard.getIdNo());
			bean.setPersonName(idcard.getPersonName());
			bean.setGender(idcard.getGender());
			bean.setIdBirth(idcard.getIDBirth());
			bean.setIdNation(idcard.getIDNation());
			bean.setIdIssue(idcard.getIDIssue());
			bean.setIdDwelling(idcard.getIDDwelling());
			bean.setIdEfficb(idcard.getIDEfficb());
			bean.setIdEffice(idcard.getIDEffice());
			bean.setIdDwelling(idcard.getIDDwelling());
			bean.setIdPhoto(idcard.getCardImageBytes());
			// BufferedImage bi;
			// bi = ImageIO.read(new File(photoDir));
			// bean.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));

			bean.setTicket(new Ticket());

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMNotifyJson:", e);
		}
		return jsonStr;
	}

	/**
	 * createCAM_GetPhotoInfoRequest
	 * 
	 * @param uuidStr
	 * @param iDelay
	 * @return
	 */
	public static String createCAM_GetPhotoInfoRequest(String uuidStr, int iDelay) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyRequestBean bean = new PIVerifyRequestBean();
			bean.setEventDirection(1);
			bean.setEventName("CAM_GetPhotoInfo");
			bean.setUuid(uuidStr);
			bean.setiDelay(iDelay);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAM_GetPhotoInfoRequest:", e);
		}
		return jsonStr;
	}

	/**
	 * createCAM_GetPhotoInfoResult
	 * 
	 * @param uuidStr
	 * @param iDelay
	 * @return
	 */
	public static String createCAM_GetPhotoInfoResult(PITVerifyData data, int verifyStatus, byte[] idCardImg, byte[] faceImg, byte[] frameImg) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyResultBean bean = new PIVerifyResultBean();
			float faceResult = 1f;
			faceResult = CommUtil.round(2, data.getVerifyResult());

			bean.setEventDirection(2);
			bean.setEventName("CAM_GetPhotoInfo");
			bean.setResult((int) (faceResult * 100));
			bean.setUuid(data.getIdNo());
			bean.setiDelay(com.rxtec.pitchecking.Config.getInstance().getCheckDelayPassTime());
			if (Config.getInstance().getSendReceiptCount() == 3) { // 回执照片为3张
				bean.setPhoto1(idCardImg); // for debug
				bean.setPhoto2(faceImg);
				bean.setPhoto3(frameImg);
				log.info("CAM_GetPhotoInfoResult:idCardimg = " + bean.getPhotoLen1() + ",faceImg.length = " + bean.getPhotoLen2() + ",frameImg.length = "
						+ bean.getPhotoLen3());
			}
			if (Config.getInstance().getSendReceiptCount() == 2) { // 回执照片为2张
				bean.setPhoto1(faceImg); // for debug
				bean.setPhoto2(frameImg);
				bean.setPhoto3("".getBytes());
				log.info("CAM_GetPhotoInfoResult:faceImg.length = " + bean.getPhotoLen1() + ",frameImg.length = " + bean.getPhotoLen2());
			}
			if (Config.getInstance().getSendReceiptCount() == 1) { // 回执照片为1张
				bean.setPhoto1(faceImg); // for debug
				bean.setPhoto2(faceImg);
				bean.setPhoto3("".getBytes());
				log.info("CAM_GetPhotoInfoResult:faceImg.length = " + bean.getPhotoLen1() + ",faceImg.length = " + bean.getPhotoLen2());
			}
			bean.setVerifyStatus(verifyStatus);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAM_GetPhotoInfoResult:", e);
		}
		return jsonStr;
	}

	public static String createCAM_GetPhotoInfoResult(PITVerifyData data, int verifyStatus) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyResultBean bean = new PIVerifyResultBean();
			float faceResult = 1f;
			faceResult = CommUtil.round(2, data.getVerifyResult());

			bean.setEventDirection(2);
			bean.setEventName("CAM_GetPhotoInfo");
			bean.setResult((int) (faceResult * 100));
			bean.setUuid(data.getIdNo());
			bean.setiDelay(com.rxtec.pitchecking.Config.getInstance().getCheckDelayPassTime());
			if (Config.getInstance().getSendReceiptCount() == 3) { // 回执照片为3张
				bean.setPhoto1(BASE64Util.decryptBASE64(data.getIdCardImgByBase64())); // for
																						// debug
				bean.setPhoto2(BASE64Util.decryptBASE64(data.getFaceImgByBase64()));
				bean.setPhoto3(BASE64Util.decryptBASE64(data.getFrameImgByBase64()));
				log.info("CAM_GetPhotoInfoResult:idCardimg = " + bean.getPhotoLen1() + ",faceImg.length = " + bean.getPhotoLen2() + ",frameImg.length = "
						+ bean.getPhotoLen3());
			}
			if (Config.getInstance().getSendReceiptCount() == 2) { // 回执照片为2张
				bean.setPhoto1(BASE64Util.decryptBASE64(data.getFaceImgByBase64())); // for
																						// debug
				bean.setPhoto2(BASE64Util.decryptBASE64(data.getFrameImgByBase64()));
				bean.setPhoto3("".getBytes());
				log.info("CAM_GetPhotoInfoResult:faceImg.length = " + bean.getPhotoLen1() + ",frameImg.length = " + bean.getPhotoLen2());
			}
			if (Config.getInstance().getSendReceiptCount() == 1) { // 回执照片为1张
				bean.setPhoto1(BASE64Util.decryptBASE64(data.getFaceImgByBase64())); // for
																						// debug
				bean.setPhoto2(BASE64Util.decryptBASE64(data.getFaceImgByBase64()));
				bean.setPhoto3("".getBytes());
				log.info("CAM_GetPhotoInfoResult:faceImg.length = " + bean.getPhotoLen1() + ",faceImg.length = " + bean.getPhotoLen2());
			}
			bean.setVerifyStatus(verifyStatus);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAM_GetPhotoInfoResult:", e);
		}
		return jsonStr;
	}

	/**
	 * createCAM_ScreenDisplay
	 * 
	 * @param displayStr
	 * @param displayTimeOut
	 * @return
	 */
	public static String createCAM_ScreenDisplay(String displayStr, int displayTimeOut) {
		String jsonStr = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			ScreenDisplayBean bean = new ScreenDisplayBean();
			bean.setEventDirection(1);
			bean.setEventName("CAM_ScreenDisplay");
			bean.setScreenDisplay(displayStr);
			bean.setTimeout(displayTimeOut);

			jsonStr = mapper.writeValueAsString(bean);
			jsonStr = jsonStr.replace("\\r\\n", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAM_GetPhotoInfoRequest:", e);
		}
		return jsonStr;
	}

}
