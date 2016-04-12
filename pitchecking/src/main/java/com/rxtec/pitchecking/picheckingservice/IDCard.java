package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;

public class IDCard {
	private String idNo;

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	
	
	private BufferedImage cardImage;

	public BufferedImage getCardImage() {
		return cardImage;
	}

	public void setCardImage(BufferedImage cardImage) {
		this.cardImage = cardImage;
	}
}
