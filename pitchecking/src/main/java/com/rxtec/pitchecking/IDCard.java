package com.rxtec.pitchecking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDCard implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String idNo="";
	private String personName="";
	private int gender;
	private String genderCH;
	private int age;
	private String IDBirth = ""; // 出生日期
	private String IDNation = ""; // 民族
	private String IDNationCH = "";
	private String IDDwelling = "";
	private String IDIssue = "";
	private String IDEfficb = "";
	private String IDEffice = "";
	private String IDNewAddr = "";
	private static Logger log = LoggerFactory.getLogger("IDCard");
	private byte[] cardImageBytes = null;
	private BufferedImage cardImage;

	public String getIDNationCH() {
		return IDNationCH;
	}

	public void setIDNationCH(String iDNationCH) {
		IDNationCH = iDNationCH;
	}

	public String getIDBirth() {
		return IDBirth;
	}

	public void setIDBirth(String iDBirth) {
		IDBirth = iDBirth;
	}

	public String getIDNation() {
		return IDNation;
	}

	public void setIDNation(String iDNation) {
		IDNation = iDNation;
	}

	public String getIDDwelling() {
		return IDDwelling;
	}

	public void setIDDwelling(String iDDwelling) {
		IDDwelling = iDDwelling;
	}

	public String getIDIssue() {
		return IDIssue;
	}

	public void setIDIssue(String iDIssue) {
		IDIssue = iDIssue;
	}

	public String getIDEfficb() {
		return IDEfficb;
	}

	public void setIDEfficb(String iDEfficb) {
		IDEfficb = iDEfficb;
	}

	public String getIDEffice() {
		return IDEffice;
	}

	public void setIDEffice(String iDEffice) {
		IDEffice = iDEffice;
	}

	public String getIDNewAddr() {
		return IDNewAddr;
	}

	public void setIDNewAddr(String iDNewAddr) {
		IDNewAddr = iDNewAddr;
	}

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

	public BufferedImage getCardImage() {
		return cardImage;
	}

	public void setCardImage(BufferedImage cardImage) {
		this.cardImage = cardImage;
	}

	public byte[] getManualImageBytes() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buff = null;
		try {
			ImageIO.write(cardImage, "JPEG", ImageIO.createImageOutputStream(output));
			buff = output.toByteArray();
		} catch (Exception e) {
			log.error("ImageIO.write error!", e);
		}

		return buff;
	}
}
