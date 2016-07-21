package com.rxtec.pitchecking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDCard {
	private String idNo;
	private String personName;
	private int gender;
	private String genderCH;
	private int age;
	private static Logger log = LoggerFactory.getLogger("IDCard");
	private byte[] cardImageBytes = null;


	public byte[] getCardImageBytes() {
		return cardImageBytes;
	}

	public void setCardImageBytes(byte[] cardImageBytes) {
		this.cardImageBytes = cardImageBytes;
	}

	public String getGenderCH() {
		return genderCH;
	}

	public void setGenderCH(String genderCH) {
		this.genderCH = genderCH;
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

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

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
	
	public byte[] getManualImageBytes(){
		ByteArrayOutputStream output = new ByteArrayOutputStream ();
		byte[] buff = null;
		try {
			ImageIO.write(cardImage, "JPEG", ImageIO.createImageOutputStream(output));
			buff = output.toByteArray();
		} catch (Exception e) {
			log.error("ImageIO.write error!",e);
		}
		
		return buff;
	} 
}
