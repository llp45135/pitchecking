package com.rxtec.pitchecking.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;

public class TicketCheckFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TicketCheckFrame frame = new TicketCheckFrame();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TicketCheckFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setBounds(100, 100, 1024, 768);
		setMinimumSize(new Dimension(1024, 768));
		setMaximumSize(new Dimension(1024, 768));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel labelFz = new JLabel("广州南");
		labelFz.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelFz.setBounds(301, 106, 110, 54);
		contentPane.add(labelFz);
		
		JLabel labelDz = new JLabel("长沙南");
		labelDz.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelDz.setBounds(504, 106, 110, 54);
		contentPane.add(labelDz);
		
		JLabel label = new JLabel("至");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label.setBounds(421, 106, 48, 54);
		contentPane.add(label);
		
		JLabel labelTrainCode = new JLabel("G6612");
		labelTrainCode.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelTrainCode.setBounds(181, 106, 110, 54);
		contentPane.add(labelTrainCode);
		
		JLabel lableImg = new JLabel("");
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setBounds(181, 501, 140, 180);
		contentPane.add(lableImg);
		
		JLabel label_1 = new JLabel("乘车日期");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label_1.setBounds(208, 185, 134, 54);
		contentPane.add(label_1);
		
		JLabel label_2 = new JLabel("2016年5月16日");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label_2.setBounds(352, 185, 262, 54);
		contentPane.add(label_2);
		
		JLabel label_3 = new JLabel("二等软座");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label_3.setBounds(277, 260, 134, 54);
		contentPane.add(label_3);
		
		JLabel label_4 = new JLabel("全票");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label_4.setBounds(420, 260, 74, 54);
		contentPane.add(label_4);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 10, 1004, 86);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel labelTitle = new JLabel("请将车票的二维码朝下平放");
		labelTitle.setBounds(284, 10, 432, 76);
		panel.add(labelTitle);
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 36));
		labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(48, 239, 10, 10);
		contentPane.add(panel_1);
		
		//
		setUndecorated(true);
	}
}
