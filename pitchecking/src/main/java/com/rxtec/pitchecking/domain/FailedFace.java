package com.rxtec.pitchecking.domain;


public class FailedFace implements java.io.Serializable {

	private static final long serialVersionUID = -5214268039951947928L;

	private String idNo;
	private String ipAddress;
	private String gateNo;
	private byte[] cardImage;
	private byte[] faceImage;


	public String getGateNo() {
		return gateNo;
	}

	public void setGateNo(String gateNo) {
		this.gateNo = gateNo;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public byte[] getCardImage() {
		return cardImage;
	}

	public void setCardImage(byte[] cardImage) {
		this.cardImage = cardImage;
	}

	public byte[] getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(byte[] faceImage) {
		this.faceImage = faceImage;
	}


	/*
	 * BufferedImage image = null; image = ImageIO.read(new File("zp.jpg"));
	 * synIDCard.setCardImage(image);
	 */

}
