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
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.DateUtils;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.CardLayout;

public class FaceCheckFrame extends JFrame implements ActionListener {

	private JPanel contentPane;
	private Timer timer = new Timer(1000, this);
	private JLabel label_title = new JLabel("\u8BF7\u5E73\u89C6\u6444\u50CF\u5934");
	private JPanel panel_title = new JPanel();
	private JPanel panel_bottom = new JPanel();
	private VideoPanel videoPanel = new VideoPanel(Config.FrameWidth, Config.FrameHeigh);
	
	private JPanel cameraPanel;
	private JPanel msgPanel;
	
	int timeIntevel = Config.getInstance().getFaceCheckDelayTime();
	JLabel label_result = new JLabel("");
	JLabel timelabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceCheckFrame frame = new FaceCheckFrame();
					frame.setVisible(true);
//					frame.showBeginCheckFaceContent();
//					frame.showFaceCheckPassContent();
//					frame.showCheckFailedContent();
					frame.showDefaultContent();
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
		// 取得屏幕大小
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle bounds = new Rectangle(screenSize);

		setBounds(new Rectangle(0, 0, 1024, 768));
		setMinimumSize(new Dimension(1024, 768));
		setMaximumSize(new Dimension(1024, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		msgPanel = new JPanel();
		contentPane.add(msgPanel, "name_1726792116426379");
		msgPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("请通过!");
		lblNewLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblNewLabel.setForeground(Color.RED);
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 235, 1004, 274);
		msgPanel.add(lblNewLabel);
		
		cameraPanel = new JPanel();
		contentPane.add(cameraPanel, "name_1726743998786655");
		cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));

		JPanel panel_center = new JPanel();
		panel_center.setMinimumSize(new Dimension(1024, 568));
		panel_center.setMaximumSize(new Dimension(1024, 568));
		panel_center.setLayout(new BoxLayout(panel_center, BoxLayout.Y_AXIS));

//		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		// panel_center.add(Box.createVerticalStrut(35));
		panel_center.add(Box.createVerticalStrut(0));
		videoPanel.setBorder(new LineBorder(Color.GREEN));
		videoPanel.setMinimumSize(new Dimension(Config.FrameWidth, Config.FrameHeigh));
		videoPanel.setMaximumSize(new Dimension(Config.FrameWidth, Config.FrameHeigh));

		panel_center.add(videoPanel);

		JPanel panel_title = new JPanel();
		panel_title.setBackground(Color.ORANGE);
		panel_title.setMinimumSize(new Dimension(1024, 100));
		panel_title.setMaximumSize(new Dimension(1024, 100));
		panel_title.setLayout(null);
		label_title.setBounds(248, 5, 528, 64);

		label_title.setHorizontalTextPosition(SwingConstants.CENTER);
		label_title.setHorizontalAlignment(SwingConstants.CENTER);
		label_title.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel_title.add(label_title);
		label_title.setFont(new Font("微软雅黑", Font.PLAIN, 48));

		label_result.setFont(new Font("微软雅黑", Font.PLAIN, 42));
		panel_bottom.setMinimumSize(new Dimension(1024, 100));
		panel_bottom.setMaximumSize(new Dimension(1024, 100));
		panel_bottom.add(label_result);

		

		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.BLUE);
		timelabel.setFont(new Font("微软雅黑 Light", Font.PLAIN, 22));
		timelabel.setBounds(797, 5, 227, 49);
		panel_title.add(timelabel);
		
		cameraPanel.add(panel_title);
		cameraPanel.add(panel_center);
		cameraPanel.add(panel_bottom);

		showDefaultContent();

		// this.setLocationRelativeTo(null);
		setUndecorated(true);

	}

	// public void showIDCardImage(ImageIcon icon) {
	// //idCardImage.repaint();
	// //panel_idCardImage.repaint();
	// }

	/**
	 * 
	 */
	public void showFaceCheckPassContent() {
		timeIntevel = 0;
		label_title.setText("验证通过");
		label_result.setText("");
//		timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		// timer.stop();
		panel_title.setBackground(Color.GREEN);
		panel_bottom.setBackground(Color.GREEN);
		;
		// panel_title.repaint();
		// panel_bottom.repaint();
	}

	/**
	 * 
	 */
	public void showCheckFailedContent() {
		timeIntevel = 0;
		// timer.stop();
//		timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		panel_bottom.setVisible(true);
		label_title.setText("验证失败");
		label_result.setText("请从边门离开或按求助按钮");
		panel_bottom.setBackground(Color.RED);
		panel_title.setBackground(Color.RED);
		// panel_title.repaint();
		// panel_bottom.repaint();

	}

	/**
	 * 
	 */
	public void showDefaultContent() {
		try {
			Thread.sleep(Config.getInstance().getDefaultFaceCheckScreenDeley());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		panel_title.setBackground(Color.orange);
		label_title.setText("请摘下眼镜后平视摄像头");
		label_result.setText("");
		panel_bottom.setBackground(Color.ORANGE);
		// this.showIDCardImage(null);
		// timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		
		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false);
		this.cameraPanel.setVisible(true);
		
		timeIntevel = 0;
		// panel_title.repaint();
		// panel_bottom.repaint();

		timer.start();

	}

	/**
	 * 
	 */
	public void showBeginCheckFaceContent() {
		panel_title.setBackground(Color.ORANGE);
		panel_bottom.setBackground(Color.ORANGE);
		label_title.setText("请平视摄像头    ");
		label_result.setText("");
		timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		// timer.start();
	}

	private void timeRefresh() {
		String now = DateUtils.getStringDate();
		timelabel.setText(now);
	}

	/**
	 * 执行Timer要执行的部分，
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		timeRefresh();

		if (timeIntevel > 0) {
			label_title.setText("请平视摄像头     " + (timeIntevel - 1));
		}
		
		if (timeIntevel-- < 0)
			timeIntevel = 0;
	}
}
