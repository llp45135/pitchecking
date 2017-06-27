package com.rxtec.pitchecking.net.event.wharf;

public class PhotoReceiptBean {

	private int msgType = -1;      //消息类型
	private String idCardNo = "";   //身份证号
	private String ticketNo = "";   //票号
	private String ipAddress = "";  //ip地址
	private String pitDate = "";    //验证日期  yyyyMMdd
	private String pitTime = "";    //验证时间  yyyy-mm-dd hh:MM:ss.SSS	
	private float verifyResult = -1;         //比对分数
	private int isVerifyPassed = -1;        //是否核验通过
	private String citizenName = "";    //姓名
	private int gender = 0;            //性别  1：男  2：女
	private int age = 0;              //年龄
	private String ideffice = "";     //有效期截至
	private String idefficb = "";     //有效期起始
	private String idissue = "";      //签发单位
	private String iddwelling = "";   //住址
	private String idbirth = "";      //出生日期
	private String idnation = "";     //民族	
	private String idPicImageBase64 = "";   //身份证照片   Base64编码字符串
	private String faceImageBase64 = "";    //人脸照片    Base64编码字符串
	private String frameImageBase64 = "";   //现场本身照  Base64编码字符串
	
	private String idcardImagePath = "";
	private String faceImagePath = "";
	private String frameImagePath = "";

	public PhotoReceiptBean() {
		// TODO Auto-generated constructor stub
	}

	public String getIdcardImagePath() {
		return idcardImagePath;
	}

	public void setIdcardImagePath(String idcardImagePath) {
		this.idcardImagePath = idcardImagePath;
	}

	public String getFaceImagePath() {
		return faceImagePath;
	}

	public void setFaceImagePath(String faceImagePath) {
		this.faceImagePath = faceImagePath;
	}

	public String getFrameImagePath() {
		return frameImagePath;
	}

	public void setFrameImagePath(String frameImagePath) {
		this.frameImagePath = frameImagePath;
	}

	public String getTicketNo() {
		return ticketNo;
	}

	public void setTicketNo(String ticketNo) {
		this.ticketNo = ticketNo;
	}

	public String getPitDate() {
		return pitDate;
	}

	public void setPitDate(String pitDate) {
		this.pitDate = pitDate;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}


	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPitTime() {
		return pitTime;
	}

	public void setPitTime(String pitTime) {
		this.pitTime = pitTime;
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

	public float getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(float verifyResult) {
		this.verifyResult = verifyResult;
	}

	public int getIsVerifyPassed() {
		return isVerifyPassed;
	}

	public void setIsVerifyPassed(int isVerifyPassed) {
		this.isVerifyPassed = isVerifyPassed;
	}

	public String getCitizenName() {
		return citizenName;
	}

	public void setCitizenName(String citizenName) {
		this.citizenName = citizenName;
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

}
