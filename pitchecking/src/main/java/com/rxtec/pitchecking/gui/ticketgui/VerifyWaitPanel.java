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
	private JLabel waitmsgLabelUp;
	private JLabel waitmsgLabelDown;
	/**
	 * Create the panel.
	 */
	public VerifyWaitPanel() {
		setSize(new Dimension(1024, 608));
		setBackground(Color.WHITE);
		setMinimumSize(new Dimension(1024, 608));
		setMaximumSize(new Dimension(1024, 608));
		setLayout(null);
		
		waitmsgLabelUp = new JLabel("请取走车票和身份证");
		waitmsgLabelUp.setBorder(new LineBorder(new Color(0, 0, 0)));
		waitmsgLabelUp.setForeground(Color.RED);
		waitmsgLabelUp.setFont(new Font("微软雅黑", Font.PLAIN, 100));
		waitmsgLabelUp.setHorizontalAlignment(SwingConstants.CENTER);
		waitmsgLabelUp.setBounds(10, 65, 1004, 148);
		add(waitmsgLabelUp);
		
		waitmsgLabelDown = new JLabel("往前走进通道");
		waitmsgLabelDown.setBorder(new LineBorder(new Color(0, 0, 0)));
		waitmsgLabelDown.setHorizontalAlignment(SwingConstants.CENTER);
		waitmsgLabelDown.setForeground(Color.RED);
		waitmsgLabelDown.setFont(new Font("微软雅黑", Font.PLAIN, 100));
		waitmsgLabelDown.setBounds(10, 240, 1004, 155);
		add(waitmsgLabelDown);
		
		
	}
	
	public void showWaitMsg(String msg1,String msg2){
		this.waitmsgLabelUp.setText(msg1);
		this.waitmsgLabelUp.setBorder(null);
		this.waitmsgLabelDown.setText(msg2);
		this.waitmsgLabelDown.setBorder(null);
	}
}
