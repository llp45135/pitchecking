package com.rxtec.pitchecking.gui;

import java.awt.AlphaComposite;
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
import java.awt.Composite;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.LineBorder;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.DateUtils;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MediaTracker;
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
	private JLabel msgTopLabel;

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
					frame.showBeginCheckFaceContent();
//					 frame.showFaceCheckPassContent();
//					 frame.showCheckFailedContent();
//					frame.showDefaultContent();
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

		msgPanel = new JPanel() {
			private static final long serialVersionUID = -3812942899603254185L;
			private Image localImg;
			private MediaTracker mt;
			private int w;
			private int h;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				try {
					g.drawImage(this.getBgImage(DeviceConfig.faceBgImgPath), 0, 0, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 画窗口背景图
			}

			/**
			 * 
			 * @param name
			 * @param imagePath
			 * @return
			 */
			public Image getBgImage(String imagePath) {
				Image bgImage = null;
				try {
					localImg = new ImageIcon(imagePath).getImage(); // 读取本地图片
					mt = new MediaTracker(this);// 为此按钮添加媒体跟踪器
					mt.addImage(localImg, 0);// 在跟踪器添加图片，下标为0
					mt.waitForAll(); // 等待加载
					w = localImg.getWidth(this);// 读取图片长度
					h = localImg.getHeight(this);// 读取图片宽度

					GraphicsConfiguration gc = new JFrame().getGraphicsConfiguration(); // 本地图形设备
					bgImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);// 建立透明画布
					Graphics2D g = (Graphics2D) bgImage.getGraphics(); // 在画布上创建画笔

					Composite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
							Config.getInstance().getFaceFrameTransparency()); // 指定透明度为半透明90%
					g.setComposite(alpha);
					g.drawImage(localImg, 0, 0, this); // 注意是,将image画到g画笔所在的画布上
					g.setColor(Color.black);// 设置颜色为黑色
					g.dispose(); // 释放内存
				} catch (Exception e) {
					e.printStackTrace();
				}

				return bgImage;
			}
		};
		contentPane.add(msgPanel, "name_1726792116426379");
		msgPanel.setLayout(null);

		msgTopLabel = new JLabel("请平视摄像头!");
		msgTopLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		msgTopLabel.setForeground(Color.RED);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 125));
		msgTopLabel.setHorizontalAlignment(SwingConstants.CENTER);
		msgTopLabel.setBounds(10, 235, 1004, 274);
		msgPanel.add(msgTopLabel);

		cameraPanel = new JPanel();
		contentPane.add(cameraPanel, "name_1726743998786655");
		cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));

		JPanel panel_center = new JPanel();
		panel_center.setMinimumSize(new Dimension(1024, 568));
		panel_center.setMaximumSize(new Dimension(1024, 568));
		panel_center.setLayout(new BoxLayout(panel_center, BoxLayout.Y_AXIS));

		// contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
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
	 * 验证通过处理
	 */
	public void showFaceCheckPassContent() {
		timeIntevel = 0;
		
		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		this.cameraPanel.setVisible(true);
		this.videoPanel.setVisible(false);
		msgTopLabel.setBorder(null);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 100));
		this.msgTopLabel.setText("<html><div align='center'>验证通过!</div><div align='center'>" + "请尽快通过闸门进站!"+"</di></html>");
		
		label_title.setText("");
		label_result.setText("");
		// timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		// timer.stop();
		panel_title.setBackground(Color.GREEN);
		panel_bottom.setBackground(Color.GREEN);
		;
		// panel_title.repaint();
		// panel_bottom.repaint();
	}

	/**
	 * 检脸失败处理
	 */
	public void showCheckFailedContent() {
		timeIntevel = 0;
		// timer.stop();
		// timeIntevel = Config.getInstance().getFaceCheckDelayTime();
		
		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		this.cameraPanel.setVisible(true);
		this.videoPanel.setVisible(false);
		msgTopLabel.setBorder(null);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 76));
		this.msgTopLabel.setText("<html><div align='center'>验证失败!</div><div align='center'>" + "请从边门离开或按求助按钮!"+"</di></html>");
		
		panel_bottom.setVisible(true);
//		label_title.setText("验证失败");
//		label_result.setText("请从边门离开或按求助按钮");
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
		
		this.msgPanel.setVisible(false);
		this.cameraPanel.setVisible(true);
		this.videoPanel.setVisible(true);
		
		panel_title.setBackground(Color.orange);
		label_title.setText("请摘下眼镜后平视摄像头");
		label_result.setText("");
		panel_bottom.setBackground(Color.ORANGE);
		// this.showIDCardImage(null);
		// timeIntevel = Config.getInstance().getFaceCheckDelayTime();

		timeIntevel = 0;
		// panel_title.repaint();
		// panel_bottom.repaint();

		timer.start();

	}

	/**
	 * 开始检脸
	 */
	public void showBeginCheckFaceContent() {
		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		this.cameraPanel.setVisible(true);
		msgTopLabel.setBorder(null);
		
		panel_title.setBackground(Color.ORANGE);
		panel_bottom.setBackground(Color.ORANGE);
//		label_title.setText("请平视摄像头    ");
		label_title.setText("");
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
//		timeRefresh();

		if (timeIntevel > 0) {
			label_title.setText("请平视摄像头     " + (timeIntevel - 1));
//			msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 120));
//			msgTopLabel.setText("<html><div align='center'>请平视摄像头!</div><div align='center'>" + (timeIntevel - 1)+"</di></html>");
		}
		if(timeIntevel==0){
//			this.showDefaultContent();
//			this.showCheckFailedContent();
//			this.showFaceCheckPassContent();
//			this.showDefaultContent();
		}

		if (timeIntevel-- < 0)
			timeIntevel = 0;
	}
}
