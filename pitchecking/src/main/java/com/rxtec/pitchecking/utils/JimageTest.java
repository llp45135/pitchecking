package com.rxtec.pitchecking.utils;

import magick.ImageInfo;
import magick.MagickApiException;
import magick.MagickException;
import magick.MagickImage;

public class JimageTest {

	public JimageTest() {
		// TODO Auto-generated constructor stub
	}

	public static void resetsize(String picFrom, String picTo) {
		try {
			ImageInfo info = new ImageInfo(picFrom);
			MagickImage image = new MagickImage(new ImageInfo(picFrom));
			System.out.println(""+info.getQuality());
			System.out.println(""+image.getXResolution());
			System.out.println(""+image.getYResolution());
			System.out.println(""+image.getDimension().getWidth());
			System.out.println(""+image.getDimension().getHeight());
			MagickImage scaled = image.scaleImage(120, 97);
			scaled.setFileName(picTo);
			scaled.writeImage(info);
		} catch (MagickApiException ex) {
			ex.printStackTrace();
		} catch (MagickException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		resetsize("D:/pitchecking/jmagick/test.jpg", "D:/pitchecking/jmagick/new.jpg");
	}
}
