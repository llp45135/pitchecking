package com.rxtec.pitchecking.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

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
		initGui();
	}
	
	public void setIdcardBmp(ImageIcon icon){
		this.showBmp.setIcon(icon);
	}
	
	
	private JPanel videoPanel;
	

	public JPanel getVideoPanel() {
		return videoPanel;
	}


	private JLabel resultLabel;

	
	private void initGui(){
		videoPanel = new JPanel();
		videoPanel.setSize(320, 480);
		Panel resultPanel = new Panel();
		resultLabel = new JLabel("0");
		resultLabel.setFont(new Font("Times New Roman",Font.ITALIC,24));
		resultPanel.add(resultLabel);
		Panel p = new Panel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(videoPanel);
		p.add(resultLabel);
		this.add(p);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);

		this.setBounds(10,10,500,500);
		//this.pack();
		this.setVisible(true);
		
	}
	

	
	public void setResultValue(float value){
		resultLabel.setText(String.valueOf(value));
	}
	
	
	

}
