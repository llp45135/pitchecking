package com.rxtec.pitchecking;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.gui.FaceCheckFrame;

/**
 * AppForFaceCheccking
 *
 */
public class AppForFaceCheccking {

	private Logger log = LoggerFactory.getLogger("FaceTrackingService");

	public static void main(String[] args) {
		FaceCheckFrame f = new FaceCheckFrame();
	}
	
	private static BufferedImage getImg(String fnm) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fnm));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bi;
	}
}
