package com.rxtec.pitchecking.mq.police.dao;

import java.io.Serializable;

public class PITInfoJmsObj implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int msgType = -1;
	private String idCardNo = "";
	private String qrCode="";
	private int idHashCode = -1;
	private int gender = 0;
	private int event = -1;
	private String ipAddress = "";
	private int age = 0;
	private String pitTime = "";
	private String idPicImageBase64 = "";
	private String faceImageBase64 = "";
	private String frameImageBase64 = "";
	private float similarity = -1;
	private int isVerifyPassed = -1;
	private String jsonStr = "";
	
	private String dbCode="";
	private String gateNo="";
	private String citizenName="";
	private String ideffice="";
	private String idefficb="";
	private String idissue="";
	private String iddwelling="";
	private String idbirth="";
	private String idnation="";
	/**
	 * 票面信息
	 */
	private String ticketNo = "";
	private String fromStationCode = "";
	private String endStationCode = "";
	private String changeStationCode = "";
	private String trainCode = "";
	private String coachNo = "";
	private String seatCode = "";
	private String ticketType = "";
	private String seatNo = "";
	private String trainDate = "";
	private String cardNo = "";
	private String passengerName = "";
	private String saleOfficeNo = "";// 售票处
	private String saleWindowNo = "";
	private String saleDate = "";

	public PITInfoJmsObj() {
		// TODO Auto-generated constructor stub
	}

	public String getDbCode() {
		return dbCode;
	}

	public void setDbCode(String dbCode) {
		this.dbCode = dbCode;
	}

	public String getGateNo() {
		return gateNo;
	}

	public void setGateNo(String gateNo) {
		this.gateNo = gateNo;
	}

	public String getCitizenName() {
		return citizenName;
	}

	public void setCitizenName(String citizenName) {
		this.citizenName = citizenName;
	}

	public String getIdeffice() {
		return ideffice;
	}

	public void setIdeffice(String ideffice) {
		this.ideffice = ideffice;
	}

	public String getIdefficb() {
		return idefficb;
	}

	public void setIdefficb(String idefficb) {
		this.idefficb = idefficb;
	}

	public String getIdissue() {
		return idissue;
	}

	public void setIdissue(String idissue) {
		this.idissue = idissue;
	}

	public String getIddwelling() {
		return iddwelling;
	}

	public void setIddwelling(String iddwelling) {
		this.iddwelling = iddwelling;
	}

	public String getIdbirth() {
		return idbirth;
	}

	public void setIdbirth(String idbirth) {
		this.idbirth = idbirth;
	}

	public String getIdnation() {
		return idnation;
	}

	public void setIdnation(String idnation) {
		this.idnation = idnation;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getTicketNo() {
		return ticketNo;
	}

	public void setTicketNo(String ticketNo) {
		this.ticketNo = ticketNo;
	}

	public String getFromStationCode() {
		return fromStationCode;
	}

	public void setFromStationCode(String fromStationCode) {
		this.fromStationCode = fromStationCode;
	}

	public String getEndStationCode() {
		return endStationCode;
	}

	public void setEndStationCode(String endStationCode) {
		this.endStationCode = endStationCode;
	}

	public String getChangeStationCode() {
		return changeStationCode;
	}

	public void setChangeStationCode(String changeStationCode) {
		this.changeStationCode = changeStationCode;
	}

	public String getTrainCode() {
		return trainCode;
	}

	public void setTrainCode(String trainCode) {
		this.trainCode = trainCode;
	}

	public String getCoachNo() {
		return coachNo;
	}

	public void setCoachNo(String coachNo) {
		this.coachNo = coachNo;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getTicketType() {
		return ticketType;
	}

	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}

	public String getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(String seatNo) {
		this.seatNo = seatNo;
	}

	public String getTrainDate() {
		return trainDate;
	}

	public void setTrainDate(String trainDate) {
		this.trainDate = trainDate;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getSaleOfficeNo() {
		return saleOfficeNo;
	}

	public void setSaleOfficeNo(String saleOfficeNo) {
		this.saleOfficeNo = saleOfficeNo;
	}

	public String getSaleWindowNo() {
		return saleWindowNo;
	}

	public void setSaleWindowNo(String saleWindowNo) {
		this.saleWindowNo = saleWindowNo;
	}

	public String getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}

	public String getPitTime() {
		return pitTime;
	}

	public void setPitTime(String pitTime) {
		this.pitTime = pitTime;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getIdHashCode() {
		return idHashCode;
	}

	public void setIdHashCode(int idHashCode) {
		this.idHashCode = idHashCode;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getIdPicImageBase64() {
		return idPicImageBase64;
	}

	public void setIdPicImageBase64(String idPicImageBase64) {
		this.idPicImageBase64 = idPicImageBase64;
	}

	public String getFaceImageBase64() {
		return faceImageBase64;
	}

	public void setFaceImageBase64(String faceImageBase64) {
		this.faceImageBase64 = faceImageBase64;
	}

	public String getFrameImageBase64() {
		return frameImageBase64;
	}

	public void setFrameImageBase64(String frameImageBase64) {
		this.frameImageBase64 = frameImageBase64;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public int getIsVerifyPassed() {
		return isVerifyPassed;
	}

	public void setIsVerifyPassed(int isVerifyPassed) {
		this.isVerifyPassed = isVerifyPassed;
	}

	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}

}
