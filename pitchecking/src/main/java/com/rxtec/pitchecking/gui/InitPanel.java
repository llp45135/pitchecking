package com.rxtec.pitchecking.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

import com.rxtec.pitchecking.device.DeviceConfig;

public class InitPanel extends JPanel {
	private JLabel initImgLabel;
	/**
	 * Create the panel.
	 */
	public InitPanel() {
		setSize(new Dimension(1280, 608));
		setBackground(Color.white);
		setMinimumSize(new Dimension(1280, 608));
		setMaximumSize(new Dimension(1280, 608));
		setLayout(null);
		
		initImgLabel = new JLabel("");
		initImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		initImgLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		initImgLabel.setBounds(257, 0, 738, 608);
		add(initImgLabel);
		
		ImageIcon initImg = new ImageIcon(DeviceConfig.initImgPath);
		initImgLabel.setIcon(initImg);
		
//		JLabel lblNewLabel = new JLabel("请将车票和二代证一起插入刷卡区");
//		lblNewLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
//		lblNewLabel.setForeground(Color.RED);
//		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 56));
//		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		lblNewLabel.setBounds(123, 172, 1018, 110);
//		add(lblNewLabel);
//		
//		JLabel label = new JLabel("（车票正面朝上，保持二维码可见）");
//		label.setHorizontalAlignment(SwingConstants.CENTER);
//		label.setForeground(Color.RED);
//		label.setFont(new Font("微软雅黑", Font.PLAIN, 56));
//		label.setBorder(new LineBorder(new Color(0, 0, 0)));
//		label.setBounds(123, 292, 1018, 110);
//		add(label);
		
		
	}
}
