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
	private String personName;
	private int gender;
	private String genderCH;
	private int age;


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
