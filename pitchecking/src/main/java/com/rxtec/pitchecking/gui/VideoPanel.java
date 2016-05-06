package com.rxtec.pitchecking.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class VideoPanel extends JPanel{
	private static final long serialVersionUID = 2362485545008256443L;
	public BufferedImage image;

	public VideoPanel(int width,int height){
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
//	public void paint(Graphics g) {
//		((Graphics2D) g).drawImage(image, 0, 0, null);
//	}
//	

}
