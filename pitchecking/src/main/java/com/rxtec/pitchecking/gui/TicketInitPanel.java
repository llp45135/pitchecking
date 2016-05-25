package com.rxtec.pitchecking.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Color;

public class TicketInitPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public TicketInitPanel() {
		setBackground(Color.LIGHT_GRAY);
		setMinimumSize(new Dimension(1024, 568));
		setMaximumSize(new Dimension(1024, 568));
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("请刷票");
		lblNewLabel.setBounds(466, 104, 90, 40);
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel);
	}

}
