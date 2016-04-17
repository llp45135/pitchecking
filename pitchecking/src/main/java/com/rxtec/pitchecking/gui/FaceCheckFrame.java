package com.rxtec.pitchecking.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JSeparator;
import java.awt.Canvas;
import java.awt.Color;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.LineBorder;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.FaceData;

import java.awt.Rectangle;
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.BoxLayout;

public class FaceCheckFrame extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JButton idCardImage;
	private Timer timer = new Timer(1000, this);
	JLabel label_title = new JLabel("\u8BF7\u5E73\u89C6\u6444\u50CF\u5934");
	JPanel panel_title = new JPanel();
	JPanel panel_bottom = new JPanel();


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

	private JPanel videoPanel = new JPanel();

	
	
	
	public JPanel getVideoPanel() {
		return videoPanel;
	}

	/**
	 * Create the frame.
	 */
	public FaceCheckFrame() {
		setMinimumSize(new Dimension(600, 800));
		setMaximumSize(new Dimension(600, 800));
		setBounds(new Rectangle(0, 0, 640, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 480);
		contentPane = new JPanel();
//		contentPane.setToolTipText("ddd");
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel_idCardImage = new JPanel();
		panel_idCardImage.setName("");
		panel_idCardImage.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_idCardImage.setToolTipText("");
		panel_idCardImage.setBounds(475, 332, 118, 152);
		panel_idCardImage.setLayout(null);
		contentPane.add(panel_idCardImage);
		
		idCardImage = new JButton("");
		idCardImage.setBounds(10, 10, 100, 130);
		panel_idCardImage.add(idCardImage);
		
		videoPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		videoPanel.setBounds(20, 151, 382, 480);
		contentPane.add(videoPanel);
		
		JPanel panel_title = new JPanel();
		panel_title.setBackground(Color.ORANGE);
		panel_title.setBounds(0, 0, 624, 74);
		contentPane.add(panel_title);
		
//		contentPane.add(showBmp);

		label_title.setHorizontalTextPosition(SwingConstants.CENTER);
		label_title.setHorizontalAlignment(SwingConstants.CENTER);
		label_title.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel_title.add(label_title,BorderLayout.CENTER);
		label_title.setFont(new Font("微软雅黑", Font.PLAIN, 48));
		
		panel_bottom.setBounds(0, 687, 624, 74);
		contentPane.add(panel_bottom);
		panel_bottom.add(label_result);
		
		
		label_result.setFont(new Font("微软雅黑", Font.PLAIN, 42));
		showDefaultContent();

		this.setLocationRelativeTo(null);
		
	}

	public void setIdcardBmp(ImageIcon icon) {
		this.idCardImage.setIcon(icon);
	}
	JLabel label_result = new JLabel("");
	

	
	
	public void showFaceCheckPassContent(){
		
		panel_title.setBackground(Color.GREEN);
		label_title.setText("验证通过");
		panel_bottom.setBackground(Color.GREEN);;
		label_result.setText("");
		timeIntevel = 5;
		timer.stop();
		panel_title.repaint();
		panel_bottom.repaint();
	}
	
	public void showCheckFailedContent(){
		timer.stop();
		timeIntevel = 5;
		panel_bottom.setVisible(true);
		label_title.setText("验证失败");
		label_result.setText("请从边门离开或按求助按钮");
		panel_bottom.setBackground(Color.RED);
		panel_title.setBackground(Color.RED);
		panel_title.repaint();
		panel_bottom.repaint();
		
	}
	
	
	
	
	
	public void showDefaultContent(){
		
		try {
			Thread.sleep(Config.getInstance().getDefaultFaceCheckScreenDeley());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		panel_title.setBackground(Color.orange);
		label_title.setText("请刷二代证");
		label_result.setText("");
		panel_bottom.setBackground(Color.ORANGE);
		this.setIdcardBmp(null);
		timeIntevel = 5;
		panel_title.repaint();
		panel_bottom.repaint();

	}
	
	
	int timeIntevel = 5;
	public void showBeginCheckFaceContent(){
		panel_title.setBackground(Color.ORANGE);
		panel_bottom.setBackground(Color.ORANGE);
		label_title.setText("请平视摄像头    ");
		label_result.setText("");
		timer.start();
	}
	
	/**
	 * 执行Timer要执行的部分，
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(timeIntevel--<0) timeIntevel = 0;
		label_title.setText("请平视摄像头     " + timeIntevel);
	}
	
	
	
	
}
