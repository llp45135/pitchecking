package com.rxtec.pitchecking.gui.ticketgui;

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

public class VerifyWaitPanel extends JPanel {
	private JLabel waitmsgLabel;
	private JLabel label;
	/**
	 * Create the panel.
	 */
	public VerifyWaitPanel() {
		setSize(new Dimension(1024, 608));
		setBackground(Color.WHITE);
		setMinimumSize(new Dimension(1024, 608));
		setMaximumSize(new Dimension(1024, 608));
		setLayout(null);
		
		waitmsgLabel = new JLabel("请取走车票和身份证！");
		waitmsgLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		waitmsgLabel.setForeground(Color.RED);
		waitmsgLabel.setFont(new Font("微软雅黑", Font.PLAIN, 100));
		waitmsgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		waitmsgLabel.setBounds(10, 124, 1004, 148);
		add(waitmsgLabel);
		
		label = new JLabel("往前走进通道");
		label.setBorder(new LineBorder(new Color(0, 0, 0)));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(Color.RED);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 100));
		label.setBounds(10, 279, 1004, 155);
		add(label);
		
		
	}
	
	public void showWaitMsg(String msg1,String msg2){
		this.waitmsgLabel.setText(msg1);
		this.waitmsgLabel.setBorder(null);
		this.label.setText(msg2);
		this.label.setBorder(null);
	}
}
