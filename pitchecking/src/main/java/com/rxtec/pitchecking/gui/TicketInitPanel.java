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

public class TicketInitPanel extends JPanel {
	private JButton holdTicketImg;
	private JButton holdCardImg;
	/**
	 * Create the panel.
	 */
	public TicketInitPanel() {
		setSize(new Dimension(1024, 568));
		setBackground(Color.WHITE);
		setMinimumSize(new Dimension(1024, 568));
		setMaximumSize(new Dimension(1024, 568));
		setLayout(null);
		
		holdTicketImg = new JButton("");
		holdTicketImg.setBorder(new LineBorder(new Color(0, 0, 0)));
		holdTicketImg.setBounds(612, 100, 318, 349);
		add(holdTicketImg);
		
		ImageIcon qrzoneIcon = new ImageIcon(DeviceConfig.qrReaderImgPath);
		holdTicketImg.setIcon(qrzoneIcon);
		
		holdCardImg = new JButton("");
		holdCardImg.setBorder(new LineBorder(new Color(0, 0, 0)));
		holdCardImg.setBounds(98, 100, 318, 349);
		add(holdCardImg);
		
		ImageIcon idzoneIcon = new ImageIcon(DeviceConfig.idReaderImgPath);
		holdCardImg.setIcon(idzoneIcon);
		
	}
	
	public void showIDCardImage(ImageIcon icon) {
		holdCardImg.setIcon(icon);
	}
	
	public void showTicketInfo(ImageIcon icon) {
		holdTicketImg.setIcon(icon);
	}
}
