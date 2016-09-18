package com.rxtec.pitchecking.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class VideoPanel extends JPanel{
	private static final long serialVersionUID = 2362485545008256443L;
	public BufferedImage image;
	
	private int videoType = 0;

	public VideoPanel(int width,int height){
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
//	public void paint(Graphics g) {
//		((Graphics2D) g).drawImage(image, 0, 0, null);
//	}
//	
	
	public void paintImg(){
		Graphics2D g = (Graphics2D) this.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}
	
	/**
	 * 仅供测试用
	 */
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		try {
//			g.drawImage(new ImageIcon("./img/aa.jpg").getImage(), 0, 0, null);// 画窗口背景图
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//	}

}
