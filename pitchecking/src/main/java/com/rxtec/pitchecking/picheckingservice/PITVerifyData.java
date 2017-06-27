package com.rxtec.pitchecking.picheckingservice;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.util.Log;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class PITVerifyData implements Serializable, Comparable<PITVerifyData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1318465620096874506L;

	private String eventName = "CAM_GetVerifyResult";

	private int eventDirection = 1;

	private byte[] faceImg;
	private byte[] idCardImg;
	private byte[] frameImg;

	// private BufferedImage idCardBufImg;
	// private BufferedImage faceBufImg;
	// private BufferedImage frameBufImg;

	private String faceImgByBase64;
	private String idCardImgByBase64;
	private String frameImgByBase64;

	private float verifyResult = 0f;

	private IDCard idCard;
	private String idNo;
	private String personName;
	private int gender;
	private int age;
	
	private String idBirth = ""; // 出生日期
	private String idNation = ""; // 民族
	private String idDwelling = "";
	private String idIssue = "";
	private String idEfficb = "";
	private String idEffice = "";
	
	private String qrCode;
	private Ticket ticket;
	private float faceDistance;
	private int useTime;
	private String pitDate;
	private String pitStation;
	private String pitTime;

	private float facePosePitch;
	private float facePoseRoll;
	private float facePoseYaw;

	private int cameraPosition;
	private int cameraFaceMode;

	private int faceQuality = 0; // 人臉檢測質量

	// --------------------------------------------------------------------------

	public IDCard getIdCard() {
		return idCard;
	}

	public String getIdBirth() {
		return idBirth;
	}

	public void setIdBirth(String idBirth) {
		this.idBirth = idBirth;
	}

	public String getIdNation() {
		return idNation;
	}

	public void setIdNation(String idNation) {
		this.idNation = idNation;
	}

	public String getIdDwelling() {
		return idDwelling;
	}

	public void setIdDwelling(String idDwelling) {
		this.idDwelling = idDwelling;
	}

	public String getIdIssue() {
		return idIssue;
	}

	public void setIdIssue(String idIssue) {
		this.idIssue = idIssue;
	}

	public String getIdEfficb() {
		return idEfficb;
	}

	public void setIdEfficb(String idEfficb) {
		this.idEfficb = idEfficb;
	}

	public String getIdEffice() {
		return idEffice;
	}

	public void setIdEffice(String idEffice) {
		this.idEffice = idEffice;
	}

	public String getFaceImgByBase64() {
		return faceImgByBase64;
	}

	public void setFaceImgByBase64(String faceImgByBase64) {
		this.faceImgByBase64 = faceImgByBase64;
	}

	public String getIdCardImgByBase64() {
		return idCardImgByBase64;
	}

	public void setIdCardImgByBase64(String idCardImgByBase64) {
		this.idCardImgByBase64 = idCardImgByBase64;
	}

	public String getFrameImgByBase64() {
		return frameImgByBase64;
	}

	public void setFrameImgByBase64(String frameImgByBase64) {
		this.frameImgByBase64 = frameImgByBase64;
	}

	public int getCameraFaceMode() {
		return cameraFaceMode;
	}

	public void setCameraFaceMode(int cameraFaceMode) {
		this.cameraFaceMode = cameraFaceMode;
	}

	public int getCameraPosition() {
		return cameraPosition;
	}

	public void setCameraPosition(int cameraPosition) {
		this.cameraPosition = cameraPosition;
	}

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	private String gateIp;

	public String getGateIp() {
		return gateIp;
	}

	public void setGateIp(String gateIp) {
		this.gateIp = gateIp;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getEventDirection() {
		return eventDirection;
	}

	public void setEventDirection(int eventDirection) {
		this.eventDirection = eventDirection;
	}

	public float getFacePosePitch() {
		return facePosePitch;
	}

	public void setFacePosePitch(float facePosePitch) {
		this.facePosePitch = facePosePitch;
	}

	public float getFacePoseRoll() {
		return facePoseRoll;
	}

	public void setFacePoseRoll(float facePoseRoll) {
		this.facePoseRoll = facePoseRoll;
	}

	public float getFacePoseYaw() {
		return facePoseYaw;
	}

	public void setFacePoseYaw(float facePoseYaw) {
		this.facePoseYaw = facePoseYaw;
	}

	public String getPitStation() {
		return pitStation;
	}

	public void setPitStation(String pitStation) {
		this.pitStation = pitStation;
	}

	public String getPitDate() {
		return pitDate;
	}

	public void setPitDate(String pitDate) {
		this.pitDate = pitDate;
	}

	public String getPitTime() {
		return pitTime;
	}

	public void setPitTime(String pitTime) {
		this.pitTime = pitTime;
	}

	public int getUseTime() {
		return useTime;
	}

	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}

	public float getFaceDistance() {
		return faceDistance;
	}

	public void setFaceDistance(float faceDistance) {
		this.faceDistance = faceDistance;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public float getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(float verifyResult) {
		this.verifyResult = verifyResult;
	}

	public byte[] getFaceImg() {
		return faceImg;
	}

	public void setFaceImg(byte[] faceImg) {
		this.faceImg = faceImg;
	}

	public byte[] getIdCardImg() {
		return idCardImg;
	}

	public void setIdCardImg(byte[] idCardImg) {
		this.idCardImg = idCardImg;
	}

	public byte[] getFrameImg() {
		return frameImg;
	}

	public void setFrameImg(byte[] frameImg) {
		this.frameImg = frameImg;
	}

	// public BufferedImage getIdCardBufImg() {
	// return idCardBufImg;
	// }
	//
	// public void setIdCardBufImg(BufferedImage idCardBufImg) {
	// this.idCardBufImg = idCardBufImg;
	// }
	//
	// public BufferedImage getFaceBufImg() {
	// return faceBufImg;
	// }
	//
	// public void setFaceBufImg(BufferedImage faceBufImg) {
	// this.faceBufImg = faceBufImg;
	// }
	//
	// public BufferedImage getFrameBufImg() {
	// return frameBufImg;
	// }
	//
	// public void setFrameBufImg(BufferedImage frameBufImg) {
	// this.frameBufImg = frameBufImg;
	// }

	public PITVerifyData(PITData pd) {
		if (pd == null)
			return;
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		this.frameImg = ImageToolkit.getImageBytes(pd.getFrame(), "JPEG");

		try{
		if (pd.getIdCard() != null) {
			// this.idCardBufImg = pd.getIdCard().getCardImage();
			// this.frameBufImg = pd.getFrame();
			// this.faceBufImg = pd.getFaceImage();
			this.idCard = pd.getIdCard();
			this.idCardImg = pd.getIdCard().getCardImageBytes();
			this.idCardImgByBase64 = BASE64Util.encryptBASE64(pd.getIdCard().getCardImageBytes());
			this.idNo = pd.getIdCard().getIdNo();
			this.personName = pd.getIdCard().getPersonName();
			this.age = pd.getIdCard().getAge();
			this.gender = pd.getIdCard().getGender();
		}
		this.cameraPosition = pd.getCameraPosition();
		this.cameraFaceMode = pd.getCameraFaceMode();
		this.ticket = pd.getTicket();
		this.faceImg = ImageToolkit.getImageBytes(pd.getFaceImage(), "JPEG");
		this.faceImgByBase64 = BASE64Util.encryptBASE64(ImageToolkit.getImageBytes(pd.getFaceImage(), "JPEG"));
		this.faceDistance = pd.getFaceDistance();
		Date now = new Date();
		this.pitDate = sf.format(now);
		this.pitTime = sf2.format(now);
		this.pitStation = pd.getPitStation();
		this.facePosePitch = pd.getFacePosePitch();
		this.facePoseRoll = pd.getFacePoseRoll();
		this.facePoseYaw = pd.getFacePoseYaw();
		this.setFaceQuality(pd.getFaceQuality());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public PITVerifyData() {

	}

	@Override
	public String toString() {
		if (idCardImg != null && faceImg != null) {
			byte[] idcardImgArray = null, faceImgArray = null, frameArray = null;
			try {
				idcardImgArray = BASE64Util.decryptBASE64(idCardImgByBase64);
				faceImgArray = BASE64Util.decryptBASE64(faceImgByBase64);
				frameArray = BASE64Util.decryptBASE64(frameImgByBase64);
			} catch (Exception e) {
				Log.error("", e);
			}
			int idcardImgLen = 0, faceImgLen = 0, frameImgLen = 0;
			if (idcardImgArray != null) {
				idcardImgLen = idcardImgArray.length;
			}
			if (faceImgArray != null) {
				faceImgLen = faceImgArray.length;
			}
			if (frameArray != null) {
				frameImgLen = frameArray.length;
			}
			return "idNo=" + idNo + "   idcardImg.length=" + idcardImgLen + " faceImg.length=" + faceImgLen + " frameImg.length=" + frameImgLen + " verifyResult="
					+ verifyResult;
		} else {
			return "idNo=" + idNo + " 图像数据缺失: idCardImg=" + idCardImg + " faceImg=" + faceImg;
		}
	}

	@Override
	public int compareTo(PITVerifyData o) {
		if (this.verifyResult > o.getVerifyResult())
			return 1;
		else
			return -1;
	}

	public int getFaceQuality() {
		return faceQuality;
	}

	public void setFaceQuality(int faceQuality) {
		this.faceQuality = faceQuality;
	}
}
