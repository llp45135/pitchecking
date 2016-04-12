package com.rxtec.pitchecking.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class FaceCheckFrame extends JFrame {

	private JPanel contentPane;
	JButton showBmp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceCheckFrame frame = new FaceCheckFrame();
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
	public FaceCheckFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("\u8BF7\u5E73\u89C6\u6444\u50CF\u5934");
		lblNewLabel.setFont(new Font("宋体", Font.PLAIN, 18));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(204, 30, 222, 62);
		contentPane.add(lblNewLabel);
		
		
		showBmp = new JButton();
		showBmp.setBounds(427, 94, 100, 130);
		contentPane.add(showBmp);
	}
	
	public void setIdcardBmp(ImageIcon icon){
		this.showBmp.setIcon(icon);
	}

}
