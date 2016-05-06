package com.rxtec.pitchecking.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import java.awt.Toolkit;
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.BoxLayout;

public class FaceCheckFrame extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JButton idCardImage;
	private Timer timer = new Timer(1000, this);
	private JLabel label_title = new JLabel("\u8BF7\u5E73\u89C6\u6444\u50CF\u5934");
	private JPanel panel_title = new JPanel();
	private JPanel panel_bottom = new JPanel();
	private VideoPanel videoPanel = new VideoPanel(640,480);
	private JPanel panel_idCardImage = new JPanel();
	JLabel label_result = new JLabel("");


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


	
	
	
	public VideoPanel getVideoPanel() {
		return videoPanel;
	}

	/**
	 * Create the frame.
	 */
	public FaceCheckFrame() {
		setMinimumSize(new Dimension(1024, 768));
		setMaximumSize(new Dimension(1024, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS)); 
		panel_idCardImage.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		
		
		panel_idCardImage.setName("");
		panel_idCardImage.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_idCardImage.setToolTipText("");
		panel_idCardImage.setMinimumSize(new Dimension(150, 180));
		panel_idCardImage.setMaximumSize(new Dimension(150, 180));
		panel_idCardImage.setLayout(new BorderLayout(0, 0));

		idCardImage = new JButton("");
		idCardImage.setMinimumSize(new Dimension(140, 170));
		idCardImage.setMaximumSize(new Dimension(140, 170));

		panel_idCardImage.add(idCardImage);
		
		JPanel panel_center = new JPanel();
		panel_center.setLayout(new BoxLayout(panel_center, BoxLayout.X_AXIS));
		panel_center.setMinimumSize(new Dimension(1024, 568));
		panel_center.setMaximumSize(new Dimension(1024, 568));
		
		
		videoPanel.setBorder(new LineBorder(Color.GREEN));
		videoPanel.setMinimumSize(new Dimension(640, 480));
		videoPanel.setMaximumSize(new Dimension(640, 480));

		panel_center.add(Box.createHorizontalStrut(100));  
		panel_center.add(panel_idCardImage);
		panel_center.add(Box.createHorizontalStrut(100));  
		panel_center.add(videoPanel);
		
		JPanel panel_title = new JPanel();
		panel_title.setBackground(Color.ORANGE);
		panel_title.setMinimumSize(new Dimension(1024, 100));
		panel_title.setMaximumSize(new Dimension(1024, 100));
		//contentPane.add(panel_title);
		
//		contentPane.add(showBmp);

		label_title.setHorizontalTextPosition(SwingConstants.CENTER);
		label_title.setHorizontalAlignment(SwingConstants.CENTER);
		label_title.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel_title.add(label_title,BorderLayout.CENTER);
		label_title.setFont(new Font("微软雅黑", Font.PLAIN, 48));
		
		label_result.setFont(new Font("微软雅黑", Font.PLAIN, 42));
		panel_bottom.setMinimumSize(new Dimension(1024, 100));
		panel_bottom.setMaximumSize(new Dimension(1024, 100));
		//contentPane.add(panel_bottom);
		panel_bottom.add(label_result);
		
		
		
		
		
		contentPane.add(panel_title);
		contentPane.add(panel_center);
		contentPane.add(panel_bottom);
		
		
		showDefaultContent();

		//this.setLocationRelativeTo(null);
		setUndecorated(true);
		// 取得屏幕大小
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle bounds = new Rectangle( screenSize );

		setBounds( bounds );
		
	}

	public void showIDCardImage(ImageIcon icon) {
		idCardImage.setIcon(icon);
		//idCardImage.repaint();
		//panel_idCardImage.repaint();
	}
	

	
	
	public void showFaceCheckPassContent(){
		
		label_title.setText("验证通过");
		label_result.setText("");
		timeIntevel = 5;
		timer.stop();
		panel_title.setBackground(Color.GREEN);
		panel_bottom.setBackground(Color.GREEN);;
		//panel_title.repaint();
		//panel_bottom.repaint();
	}
	
	public void showCheckFailedContent(){
		timer.stop();
		timeIntevel = 5;
		panel_bottom.setVisible(true);
		label_title.setText("验证失败");
		label_result.setText("请从边门离开或按求助按钮");
		panel_bottom.setBackground(Color.RED);
		panel_title.setBackground(Color.RED);
		//panel_title.repaint();
		//panel_bottom.repaint();
		
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
		this.showIDCardImage(null);
		timeIntevel = 5;
//		panel_title.repaint();
//		panel_bottom.repaint();

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
