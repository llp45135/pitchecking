package com.rxtec.pitchecking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

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
	
	public byte[] getImageBytes(){
		ByteArrayOutputStream output = new ByteArrayOutputStream ();
		try {
			ImageIO.write(cardImage, "JPEG", ImageIO.createImageOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] buff = output.toByteArray();
		return buff;
	} 
}
