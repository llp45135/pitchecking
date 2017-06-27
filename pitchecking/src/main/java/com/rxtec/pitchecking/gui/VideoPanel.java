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

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.task.FaceScreenListener;
import com.rxtec.pitchecking.task.LuminanceListenerTask;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class VideoPanel extends JPanel {
	private static final long serialVersionUID = 2362485545008256443L;
	public BufferedImage image;

	private int videoType = 0;

	public VideoPanel(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	// public void paint(Graphics g) {
	// ((Graphics2D) g).drawImage(image, 0, 0, null);
	// }
	//

	public void paintImg() {
		// 通过MQ发送帧画面
		if (!FaceCheckingService.getInstance().isFrontCamera()) { // 后置摄像头
			if (Config.getInstance().getIsUseManualMQ() == 1 && Config.getInstance().getIsSendFrame() == 1) {
				RemoteMonitorPublisher.getInstance().offerFrameData(ImageToolkit.getImageBytes(image, "jpeg"));
			}
		}

		FaceScreenListener.getInstance().setFrameImage(image); // 此帧图片是给光亮度分析任务使用

		Graphics2D g = (Graphics2D) this.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}

	/**
	 * 仅供测试用
	 */
	// public void paintComponent(Graphics g) {
	// super.paintComponent(g);
	// try {
	// g.drawImage(new ImageIcon("./img/aa.jpg").getImage(), 0, 0, null);//
	// 画窗口背景图
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

}
