package com.rxtec.pitchecking.gui.singledoor;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.SingleFaceTrackingScreen;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttSenderBroker;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.CardLayout;

public class SingleVerifyFrame extends JFrame implements ActionListener {
	private Logger log = LoggerFactory.getLogger("SingleVerifyFrame");
	private Timer timer = new Timer(1000, this);
	int faceTimeIntevel = Config.getInstance().getFaceCheckDelayTime();
	private int faceTitleStrType = 0;

	int ticketTimeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	private int ticketTitleStrType = 0;
	private int ticketBackPanelType = 0;

	private JPanel contentPane;
	private JPanel facePanel;
	private JPanel ticketPanel;

	private JLabel face_label_title = new JLabel("\u8BF7\u5E73\u89C6\u6444\u50CF\u5934");
	private JPanel panel_title;
	private JPanel panel_bottom;
	private JPanel panel_center;

	private VideoPanel videoPanel = new VideoPanel(Config.FrameWidth, Config.FrameHeigh);

	private JPanel cameraPanel;
	private JPanel msgPanel;
	private JLabel msgTopLabel;
	private JLabel passImgLabel;
	private String displayMsg = "";

	private JPanel ticketTopPanel;
	private JPanel ticketInfoPanel;
	private SingleTicketInitPanel singleTicketInitPanel;
	private SingleWaitPanel singleWaitPanel;
	private SingleReadedPanel singleReadedPanel;
	private JPanel ticketBottomPanel;

	private JLabel ticketLabelTitle;

	private JLabel labelFz;
	private JLabel labelZhi;
	private JLabel labelDz;
	private JLabel labelTrainCode;
	private JLabel lableImg;
	private JLabel label_rq;
	private JLabel labelTrainDate;
	private JLabel labelTicketType;
	private JLabel labelSeatType;
	private JLabel lableWarnmsg;
	private JLabel labelVersion;
	private JLabel lblIp;
	private JLabel bootTime;
	private JLabel timelabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// 语音调用线程
					ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
					scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

					SingleFaceTrackingScreen.getInstance();
					SingleVerifyFrame frame = new SingleVerifyFrame();
					SingleFaceTrackingScreen.getInstance().setFaceFrame(frame);
					SingleFaceTrackingScreen.getInstance().initUI(0);
					frame.showFaceDefaultContent();
					frame.showTicketDefaultContent();
					CommUtil.sleep(3 * 1000);
					// frame.setVisible(true);
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).setFaceScreenDisplayTimeout(10);
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).setFaceScreenDisplay("人脸识别成功#请通过");
					// MqttSenderBroker.getInstance().setFaceScreenDisplay("人脸识别失败#请从侧门离开");
					frame.showFaceDisplayFromTK();
					// AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag);
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum())
							.sendDoorCmd(ProcessUtil.createAudioJson(DeviceConfig.AudioTakeTicketFlag, "FaceAudio"));
					// frame.showBeginCheckFaceContent();
					// frame.showFaceCheckPassContent();
					// frame.showCheckFailedContent();
					// frame.showDefaultContent();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @return
	 */
	public VideoPanel getVideoPanel() {
		return videoPanel;
	}

	/**
	 * Create the frame.
	 */
	public SingleVerifyFrame() {
		setMinimumSize(new Dimension(1080, 1920));
		setMaximumSize(new Dimension(1080, 1920));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1080, 1920);
		contentPane = new JPanel();
		contentPane.setMinimumSize(new Dimension(1080, 1920));
		contentPane.setMaximumSize(new Dimension(1080, 1920));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		/**
		 * 车票界面
		 */
		ticketPanel = new JPanel();
		ticketPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		ticketPanel.setBounds(10, 10, 1060, 925);
		ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));
		contentPane.add(ticketPanel);		

		Color bgcolor = new Color(0, 113, 205);

		ticketTopPanel = new JPanel();
		ticketTopPanel.setBackground(bgcolor);
		ticketTopPanel.setMinimumSize(new Dimension(1024, DeviceConfig.TICKET_FRAME_TOPHEIGHT));
		ticketTopPanel.setMaximumSize(new Dimension(1024, DeviceConfig.TICKET_FRAME_TOPHEIGHT));
		ticketPanel.add(ticketTopPanel);
		ticketTopPanel.setLayout(null);

		ticketLabelTitle = new JLabel("");
		ticketLabelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		ticketLabelTitle.setForeground(Color.YELLOW);
		ticketLabelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 50));
		ticketLabelTitle.setBounds(225, 20, 551, 62);
		ticketTopPanel.add(ticketLabelTitle);
		
		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.YELLOW);
		timelabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		timelabel.setBounds(786, 26, 218, 45);
		ticketTopPanel.add(timelabel);
		/*****/
		ticketInfoPanel = new JPanel();
		ticketInfoPanel.setBackground(Color.WHITE);
		ticketInfoPanel.setMinimumSize(new Dimension(1024, 608));
		ticketInfoPanel.setMaximumSize(new Dimension(1024, 608));
		ticketPanel.add(ticketInfoPanel);
		ticketInfoPanel.setLayout(null);

		/**
		 * 初始化启动界面
		 */
		singleReadedPanel = new SingleReadedPanel();
		singleTicketInitPanel = new SingleTicketInitPanel();
		singleWaitPanel = new SingleWaitPanel();

		labelTrainCode = new JLabel("G6612");
		labelTrainCode.setHorizontalAlignment(SwingConstants.CENTER);
		labelTrainCode.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTrainCode.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTrainCode.setBounds(215, 67, 200, 72);
		ticketInfoPanel.add(labelTrainCode);

		labelFz = new JLabel("广州南");
		labelFz.setHorizontalAlignment(SwingConstants.CENTER);
		labelFz.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelFz.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelFz.setBounds(425, 67, 245, 72);
		ticketInfoPanel.add(labelFz);

		labelZhi = new JLabel("至");
		labelZhi.setHorizontalAlignment(SwingConstants.CENTER);
		labelZhi.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelZhi.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelZhi.setBounds(680, 67, 87, 72);
		ticketInfoPanel.add(labelZhi);

		labelDz = new JLabel("长沙南");
		labelDz.setHorizontalAlignment(SwingConstants.CENTER);
		labelDz.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelDz.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelDz.setBounds(777, 67, 237, 72);
		ticketInfoPanel.add(labelDz);

		label_rq = new JLabel("乘车日期");
		label_rq.setHorizontalAlignment(SwingConstants.CENTER);
		label_rq.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// label_rq.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		label_rq.setBounds(246, 190, 271, 72);
		ticketInfoPanel.add(label_rq);

		labelTrainDate = new JLabel("2016年10月16日");
		labelTrainDate.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTrainDate.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTrainDate.setBounds(557, 190, 447, 72);
		ticketInfoPanel.add(labelTrainDate);

		labelSeatType = new JLabel("二等软座");
		labelSeatType.setHorizontalAlignment(SwingConstants.CENTER);
		labelSeatType.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelSeatType.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelSeatType.setBounds(268, 299, 351, 72);
		ticketInfoPanel.add(labelSeatType);

		labelTicketType = new JLabel("全票");
		labelTicketType.setHorizontalAlignment(SwingConstants.CENTER);
		labelTicketType.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTicketType.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTicketType.setBounds(667, 299, 245, 72);
		ticketInfoPanel.add(labelTicketType);

		lableImg = new JLabel("");
		lableImg.setHorizontalAlignment(SwingConstants.CENTER);
		lableImg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableImg.setBounds(31, 172, 180, 215);
		ticketInfoPanel.add(lableImg);

		lableWarnmsg = new JLabel("请通行！");
		lableWarnmsg.setForeground(Color.RED);
		lableWarnmsg.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		lableWarnmsg.setHorizontalAlignment(SwingConstants.CENTER);
		// lableWarnmsg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableWarnmsg.setBounds(231, 418, 783, 82);
		ticketInfoPanel.add(lableWarnmsg);

		ticketBottomPanel = new JPanel();
		ticketBottomPanel.setBackground(bgcolor);
		ticketBottomPanel.setMinimumSize(new Dimension(1024, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT));
		ticketBottomPanel.setMaximumSize(new Dimension(1024, 60));
		ticketBottomPanel.setLayout(null);
		ticketPanel.add(ticketBottomPanel);

		labelVersion = new JLabel("版本号：pitcheck160709.02");
		labelVersion.setForeground(Color.BLACK);
		labelVersion.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelVersion.setBounds(31, 0, 302, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		ticketBottomPanel.add(labelVersion);

		lblIp = new JLabel("IP地址：192.168.1.5");
		lblIp.setForeground(Color.BLACK);
		lblIp.setHorizontalAlignment(SwingConstants.CENTER);
		lblIp.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		lblIp.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lblIp.setBounds(734, 0, 276, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		ticketBottomPanel.add(lblIp);

		bootTime = new JLabel("启动时间：2016-08-13 17:45:45");
		bootTime.setHorizontalAlignment(SwingConstants.CENTER);
		bootTime.setForeground(Color.BLACK);
		bootTime.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		bootTime.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		bootTime.setBounds(357, 0, 367, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		ticketBottomPanel.add(bootTime);

		/**
		 * 
		 */
		facePanel = new JPanel();
		facePanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		facePanel.setBounds(10, 946, 1060, 925);		
		facePanel.setLayout(new CardLayout(0, 0));
		contentPane.add(facePanel);

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

					GraphicsConfiguration gc = new JFrame().getGraphicsConfiguration();
					// 本地图形设备
					bgImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
					// 建立透明画布
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

		facePanel.add(msgPanel, "name_1726792116426379");
		msgPanel.setLayout(null);

		msgTopLabel = new JLabel(
				"<html><div align='center'>人脸识别成功</div><div align='center'>请通过</div><div align='center'>10</div></html>");
		msgTopLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		msgTopLabel.setForeground(Color.YELLOW);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
		msgTopLabel.setHorizontalAlignment(SwingConstants.CENTER);
		msgTopLabel.setBounds(10, 104, 1004, 553);
		msgPanel.add(msgTopLabel);

		//
		cameraPanel = new JPanel();
		facePanel.add(cameraPanel, "name_219670385833610");
		cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));

		panel_center = new JPanel();
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

		panel_title = new JPanel();
		panel_title.setBackground(null);
		panel_title.setMinimumSize(new Dimension(1024, 100));
		panel_title.setMaximumSize(new Dimension(1024, 100));
		panel_title.setLayout(null);
		face_label_title.setBounds(10, 5, 1004, 85);

		face_label_title.setHorizontalTextPosition(SwingConstants.CENTER);
		face_label_title.setHorizontalAlignment(SwingConstants.CENTER);
		face_label_title.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel_title.add(face_label_title);
		face_label_title.setFont(new Font("微软雅黑", Font.PLAIN, 60));

		panel_bottom = new JPanel();
		panel_bottom.setMinimumSize(new Dimension(1024, 100));
		panel_bottom.setMaximumSize(new Dimension(1024, 100));
		panel_bottom.setLayout(null);

		cameraPanel.add(panel_title);
		cameraPanel.add(panel_center);
		cameraPanel.add(panel_bottom);

		

//		setUndecorated(true);
		setAlwaysOnTop(true);

		this.showFaceDefaultContent();
//		this.showTicketDefaultContent();
		this.showSuccWait("", "系统启动中...");

		faceTimeIntevel = -1;
		ticketTimeIntevel = -1;
		timer.start();

		setSoftVersion("软件版本号：" + DeviceConfig.softVersion);
		setGateIP("IP地址：" + DeviceConfig.getInstance().getIpAddress());
		this.bootTime.setText("启动时间:" + CalUtils.getStringDate());
		this.bootTime.setBorder(null);
	}

	public void setSoftVersion(String verText) {
		labelVersion.setBorder(null);
		labelVersion.setText(verText);
	}

	public void setGateIP(String ipText) {
		lblIp.setBorder(null);
		lblIp.setText(ipText);
	}

	/**
	 * 单门模式初始界面
	 */
	public void showFaceDefaultContent() {
		this.msgPanel.setVisible(false);
		this.cameraPanel.setVisible(true);
		this.videoPanel.setVisible(true);

		Color bgColor = new Color(0, 142, 240);
		panel_title.setBackground(bgColor);
		panel_bottom.setBackground(bgColor);
		face_label_title.setBackground(bgColor);

		face_label_title.setForeground(Color.WHITE);
		face_label_title.setText("");

		faceTimeIntevel = -1;
	}

	/**
	 * 
	 */
	public void showTicketDefaultContent() {
		ticketTimeIntevel = -1;
		this.ticketLabelTitle.setText("");
		/**
		 * 
		 */
		singleReadedPanel.setVisible(false);
		ticketInfoPanel.setVisible(false);
		singleWaitPanel.setVisible(false);
		singleTicketInitPanel.setVisible(true);
		singleTicketInitPanel.setBackground(Color.black);
		ticketPanel.remove(singleWaitPanel);
		ticketPanel.remove(ticketInfoPanel);
		ticketPanel.remove(ticketBottomPanel);
		ticketPanel.remove(singleReadedPanel);
		ticketPanel.add(singleTicketInitPanel);
		ticketPanel.add(ticketBottomPanel);

		ticketPanel.repaint();
	}

	/**
	 * 主控端通过调用dll接口方式传输
	 */
	public void showFaceDisplayFromTK() {
		faceTimeIntevel = 0;

		String displayStr = MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).getFaceScreenDisplay();

		// this.displayMsg = displayStr.replace("#", "！");
		//
		// if (displayStr.indexOf("成功") != -1) {
		// panel_title.setBackground(Color.GREEN);
		// panel_bottom.setBackground(Color.GREEN);
		// label_title.setBackground(Color.GREEN);
		// label_title.setForeground(Color.BLACK);
		// } else {
		// panel_title.setBackground(Color.RED);
		// panel_bottom.setBackground(Color.RED);
		// label_title.setBackground(Color.RED);
		// label_title.setForeground(Color.BLACK);
		// }
		// label_title.setText(displayMsg);

		this.cameraPanel.setVisible(false);
		this.videoPanel.setVisible(false);

		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		msgTopLabel.setBorder(null);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));

		if (displayStr.indexOf("成功") != -1) {
			// ImageIcon icon = new ImageIcon(DeviceConfig.allowImgPath);
			// this.showPassStatusImage(icon);
			faceTimeIntevel = MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).getFaceScreenDisplayTimeout();// 5;
																							// //
																							// 成功时的提示信息存在时间
																							// 暂时设置为5s
		} else {
			// ImageIcon icon = new ImageIcon(DeviceConfig.forbidenImgPath);
			// this.showPassStatusImage(icon);
			faceTimeIntevel = MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).getFaceScreenDisplayTimeout();
		}
		faceTitleStrType = 4; // 4:覆盖一层panel 5：不覆盖

		if (displayStr.indexOf("#") != -1) { // 由#号，分两行
			int kk = displayStr.indexOf("#");
			String displayMsg1 = displayStr.substring(0, kk);
			String displayMsg2 = displayStr.substring(kk + 1);
			this.displayMsg = "<html><div align='center'>" + displayMsg1 + "</div>" + "<div align='center'>"
					+ displayMsg2 + "</div>";
			this.msgTopLabel.setText(displayMsg + "<div align='center'>" + faceTimeIntevel + "</div>" + "</html>");
		} else {
			this.displayMsg = "<html><div align='center'>" + displayStr + "</div>";
			this.msgTopLabel.setText(displayMsg + "<div align='center'>" + faceTimeIntevel + "</div>" + "</html>");
		}
	}

	/**
	 * 验证通过处理
	 */
	public void showFaceCheckPassContent() {
		faceTimeIntevel = 0;

		// panel_title.setBackground(Color.GREEN);
		// panel_bottom.setBackground(Color.GREEN);
		// label_title.setBackground(Color.GREEN);
		//
		// label_title.setForeground(Color.BLACK);
		// label_title.setText("人脸识别成功！请通过");

		this.cameraPanel.setVisible(false);
		this.videoPanel.setVisible(false);

		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		msgTopLabel.setBorder(null);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));

		faceTimeIntevel = 3;
		faceTitleStrType = 0;
		this.msgTopLabel
				.setText("<html><div align='center'>人脸识别成功</div><div align='center'>请通过</div><div align='center'>"
						+ faceTimeIntevel + "</div></html>");
	}

	/**
	 * 检脸失败处理
	 */
	public void showCheckFailedContent() {
		faceTimeIntevel = 0;

		// panel_title.setBackground(Color.RED);
		// panel_bottom.setBackground(Color.RED);
		// label_title.setBackground(Color.RED);
		//
		// label_title.setForeground(Color.BLACK);
		// label_title.setText("人脸识别失败！请从侧门离开");
		// label_title.setBackground(null);
		// panel_bottom.setVisible(true);

		this.cameraPanel.setVisible(false);
		this.videoPanel.setVisible(false);

		msgPanel.setVisible(true);
		msgPanel.setBackground(null); // 把背景设置为空
		msgPanel.setOpaque(false); // 设置为透明
		msgTopLabel.setBorder(null);
		msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));

		faceTimeIntevel = 5;
		faceTitleStrType = 1;
		this.msgTopLabel
				.setText("<html><div align='center'>人脸识别失败</div><div align='center'>请从侧门离开</div><div align='center'>"
						+ faceTimeIntevel + "</div></html>");
	}

	/**
	 * 开始检脸
	 */
	public void showBeginCheckFaceContent() {

		msgPanel.setVisible(false);
		this.cameraPanel.setVisible(true);
		this.videoPanel.setVisible(true);

		panel_title.setBackground(Color.ORANGE);
		panel_bottom.setBackground(Color.ORANGE);
		face_label_title.setBackground(Color.ORANGE);
		face_label_title.setForeground(Color.BLACK);
		face_label_title.setText("");

		faceTimeIntevel = Config.getInstance().getFaceCheckDelayTime();
		this.faceTitleStrType = 2;
		face_label_title.setText("请平视摄像头和屏幕     " + faceTimeIntevel);
	}

	/**
	 * 等待输入信息界面
	 * 
	 * @param ticket
	 * @param idCard
	 */
	public void showWaitInputContent(Ticket ticket, IDCard idCard, int titleStrType, int backPanelType) {
		// DeviceManager.getInstance().repaintStop();
		this.ticketTitleStrType = titleStrType;
		this.ticketBackPanelType = backPanelType;
		// if (ticket == null) {
		// this.labelTitle.setText("请在二维码扫描区扫描车票二维码");
		// } else if (idCard == null) {
		// this.labelTitle.setText("请将二代身份证放入二代证刷卡区");
		// }
		// this.labelFz.setText("");
		// this.labelZhi.setText("");
		// this.labelDz.setText("");
		// this.labelTrainCode.setText("");
		// this.label_rq.setText("");
		// this.labelTrainDate.setText("");
		// this.labelTicketType.setText("");
		// this.labelSeatType.setText("");
		// this.labelMsg.setText("");
		// this.lableImg.setIcon(null);
		// this.lableImg.setBorder(null);

		ticketInfoPanel.setVisible(false);
		singleWaitPanel.setVisible(false);
		singleTicketInitPanel.setVisible(false);
		singleReadedPanel.setVisible(true);
		singleReadedPanel.setBackground(Color.WHITE);
		ticketPanel.remove(singleWaitPanel);
		ticketPanel.remove(ticketInfoPanel);
		ticketPanel.remove(singleTicketInitPanel);
		ticketPanel.remove(ticketBottomPanel);
		ticketPanel.add(singleReadedPanel);
		ticketPanel.add(ticketBottomPanel);

		if (idCard != null) {
			if (DeviceConfig.getInstance().getVersionFlag() == 1) {
				singleReadedPanel.showIDCardImage(new ImageIcon(DeviceConfig.readedIdImgPath));
			} else {
				singleReadedPanel.showIDCardImage(new ImageIcon(idCard.getCardImage()));
			}
		} else {
			// readedPanel.showIDCardImage(new
			// ImageIcon(DeviceConfig.idReaderImgPath));
			singleReadedPanel.showIDCardImage(null);
		}
		if (ticket != null) {
			singleReadedPanel.showTicketInfo(new ImageIcon(DeviceConfig.readedQRImgPath));
		} else {
			// readedPanel.showTicketInfo(new
			// ImageIcon(DeviceConfig.qrReaderImgPath));
			singleReadedPanel.showTicketInfo(null);
		}

		ticketPanel.repaint();

		ticketTimeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	}

	/**
	 * 成功核验车票信息界面
	 * 
	 * @param ticket
	 */
	public void showTicketContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType, int backPanelType) {
		this.ticketTitleStrType = titleStrType;
		this.ticketBackPanelType = backPanelType;
		try {
			// ticket.printTicket();
			// if (ticket != null) {
			// labelFz.setText(deviceConfig.getStationName(ticket.getFromStationCode()));
			// this.labelZhi.setText("至");
			// this.labelDz.setText(deviceConfig.getStationName(ticket.getEndStationCode()));
			// this.labelTrainCode.setText(ticket.getTrainCode());
			// this.label_rq.setText("乘车日期:");
			// String strTrainDate = ticket.getTrainDate().substring(0, 4) + "年"
			// + ticket.getTrainDate().substring(4, 6) + "月" +
			// ticket.getTrainDate().substring(6, 8) + "日";
			// this.labelTrainDate.setText(strTrainDate);
			// this.labelTicketType
			// .setText(deviceConfig.getTicketTypesMap().get(Integer.parseInt(ticket.getTicketType()))
			// + "票");
			// this.labelSeatType.setText(deviceConfig.getSeatTypesMap().get(ticket.getSeatCode()));
			// }
			// ImageIcon icon = new ImageIcon(DeviceConfig.allowImgPath);
			// lableWarnmsg.setForeground(Color.GREEN);
			// this.lableWarnmsg.setText("请通行!");
			// showStatusImage(icon);
			//
			// ticketPanel.setVisible(true);
			// verifyInitPanel.setVisible(false);
			// verifyWaitPanel.setVisible(false);
			// readedPanel.setVisible(false);
			// contentPane.remove(verifyInitPanel);
			// contentPane.remove(readedPanel);
			// contentPane.remove(verifyWaitPanel);
			// contentPane.remove(bottomPanel);
			// contentPane.add(ticketPanel);
			// contentPane.add(bottomPanel);
			// contentPane.repaint();

			// this.showSuccWait("请取走车票和身份证", "往前走进通道");
			this.showSuccWait("人脸核验中", "后面的旅客切勿刷票");

			ticketTimeIntevel = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 核验成功，等待前面旅客通过界面
	 * 
	 * @param deviceConfig
	 * @param ticket
	 * @param titleStrType
	 */
	public void showSuccWait(String msg1, String msg2) {
		try {
			ticketLabelTitle.setText("");

			singleWaitPanel.showWaitMsg(msg1, msg2);

			ticketInfoPanel.setVisible(false);
			singleWaitPanel.setVisible(true);
			singleReadedPanel.setVisible(false);
			singleTicketInitPanel.setVisible(false);
			ticketPanel.remove(singleTicketInitPanel);
			ticketPanel.remove(singleReadedPanel);
			ticketPanel.remove(ticketInfoPanel);
			ticketPanel.remove(ticketBottomPanel);
			ticketPanel.add(singleWaitPanel);
			ticketPanel.add(ticketBottomPanel);
			ticketPanel.repaint();

			ticketTimeIntevel = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 还原Tk信息
	 * 
	 * @param msg1
	 * @param msg2
	 * @param backPanelType
	 */
	public void showTKInfo(String msg1, String msg2, int backPanelType) {
		try {
			this.ticketTitleStrType = 5;
			this.ticketBackPanelType = backPanelType;

			singleWaitPanel.showWaitMsg(msg1, msg2);

			ticketInfoPanel.setVisible(false);
			singleWaitPanel.setVisible(true);
			singleReadedPanel.setVisible(false);
			singleTicketInitPanel.setVisible(false);
			ticketPanel.remove(singleTicketInitPanel);
			ticketPanel.remove(singleReadedPanel);
			ticketPanel.remove(ticketInfoPanel);
			ticketPanel.remove(ticketBottomPanel);
			ticketPanel.add(singleWaitPanel);
			ticketPanel.add(ticketBottomPanel);
			ticketPanel.repaint();

			ticketTimeIntevel = 3;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 核验失败信息界面
	 */
	public void showFailedContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType, int backPanelType,
			String failedMsg) {
		this.ticketTitleStrType = titleStrType;
		this.ticketBackPanelType = backPanelType;
		try {
			// ticket.printTicket();

			labelFz.setText(deviceConfig.getStationName(ticket.getFromStationCode()));
			this.labelZhi.setText("至");
			this.labelDz.setText(deviceConfig.getStationName(ticket.getEndStationCode()));
			this.labelTrainCode.setText(ticket.getTrainCode());
			this.label_rq.setText("乘车日期:");
			String strTrainDate = ticket.getTrainDate().substring(0, 4) + "年" + ticket.getTrainDate().substring(4, 6)
					+ "月" + ticket.getTrainDate().substring(6, 8) + "日";
			this.labelTrainDate.setText(strTrainDate);
			this.labelTicketType
					.setText(deviceConfig.getTicketTypesMap().get(Integer.parseInt(ticket.getTicketType())) + "票");
			this.labelSeatType.setText(deviceConfig.getSeatTypesMap().get(ticket.getSeatCode()));
			ImageIcon icon = new ImageIcon(DeviceConfig.forbidenImgPath);
			lableWarnmsg.setForeground(Color.RED);
			this.lableWarnmsg.setText(failedMsg);
			showStatusImage(icon);

			ticketInfoPanel.setVisible(true);
			singleWaitPanel.setVisible(false);
			singleReadedPanel.setVisible(false);
			singleTicketInitPanel.setVisible(false);
			ticketPanel.remove(singleTicketInitPanel);
			ticketPanel.remove(singleReadedPanel);
			ticketPanel.remove(singleWaitPanel);
			ticketPanel.remove(ticketBottomPanel);
			ticketPanel.add(ticketPanel);
			ticketPanel.add(ticketBottomPanel);
			ticketPanel.repaint();

			ticketTimeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示设备异常界面
	 * 
	 * @param config
	 * @param ticket
	 * @param titleStrType
	 * @param failedMsg
	 */
	public void showExceptionContent(DeviceConfig deviceConfig, int titleStrType, String exMsg) {
		this.ticketTitleStrType = titleStrType;
		try {
			// ticket.printTicket();
			if (exMsg.equals("暂停服务")) {
				ticketLabelTitle.setText("");
			} else {
				ticketLabelTitle.setText("设备故障");
			}
			ticketLabelTitle.setForeground(Color.RED);
			labelFz.setText("");
			this.labelZhi.setText("");
			this.labelDz.setText("");
			this.labelTrainCode.setText("");
			this.label_rq.setText("");
			this.labelTrainDate.setText("");
			this.labelTicketType.setText("");
			this.labelSeatType.setText("");
			ImageIcon icon = new ImageIcon(DeviceConfig.forbidenImgPath);
			this.lableWarnmsg.setForeground(Color.RED);
			this.lableWarnmsg.setText(exMsg);
			showStatusImage(icon);

			ticketInfoPanel.setVisible(true);
			singleTicketInitPanel.setVisible(false);
			singleWaitPanel.setVisible(false);
			singleReadedPanel.setVisible(false);
			ticketPanel.remove(singleTicketInitPanel);
			ticketPanel.remove(singleReadedPanel);
			ticketPanel.remove(singleWaitPanel);
			ticketPanel.remove(ticketBottomPanel);
			ticketPanel.add(ticketInfoPanel);
			ticketPanel.add(ticketBottomPanel);
			ticketPanel.repaint();

			ticketTimeIntevel = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showStatusImage(ImageIcon icon) {
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setIcon(icon);
	}
	
	private void timeRefresh() {
		String now = CalUtils.getStringDate();
		timelabel.setText(now);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		this.timeRefresh();
		
		
		if (faceTimeIntevel > 0) {
			if (this.faceTitleStrType == 0) {
				// label_title.setText("人脸识别成功！请通过" + " " + (timeIntevel - 1));

				msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
				msgTopLabel.setText(
						"<html><div align='center'>人脸识别成功</div><div align='center'>请通过</div><div align='center'>"
								+ (faceTimeIntevel - 1) + "</di></html>");
			} else if (this.faceTitleStrType == 1) {
				// label_title.setText("人脸识别失败！请从侧门离开" + " " + (timeIntevel -
				// 1));

				msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
				msgTopLabel.setText(
						"<html><div align='center'>人脸识别失败</div><div align='center'>请从侧门离开</div><div align='center'>"
								+ (faceTimeIntevel - 1) + "</di></html>");
			} else if (this.faceTitleStrType == 4) {
				msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
				msgTopLabel.setText(displayMsg + "<div align='center'>" + (faceTimeIntevel - 1) + "</div>" + "</html>");
			} else if (this.faceTitleStrType == 5) {
				face_label_title.setText(displayMsg + "  " + (faceTimeIntevel - 1));
				// label_title.setBackground(null);
			} else {
				face_label_title.setText("请平视摄像头和屏幕     " + (faceTimeIntevel - 1));
			}
		}
		if (faceTimeIntevel == 0) {
			this.showFaceDefaultContent();
			this.showTicketDefaultContent();
		}

		if (faceTimeIntevel-- < 0)
			faceTimeIntevel = -1;

		/**
		 * 
		 */
		if (ticketTimeIntevel >= 0) {
			if (this.ticketTitleStrType == 1) {
				ticketLabelTitle.setText("还需扫火车票二维码   " + (ticketTimeIntevel - 1));
				ticketLabelTitle.setForeground(Color.RED);
			} else if (this.ticketTitleStrType == 2) {
				ticketLabelTitle.setText("还需刷第二代身份证   " + (ticketTimeIntevel - 1));
				ticketLabelTitle.setForeground(Color.RED);
			} else if (this.ticketTitleStrType == 3) {
				ticketLabelTitle.setText("票证核验成功   " + (ticketTimeIntevel - 1));
				ticketLabelTitle.setForeground(Color.GREEN);
			} else if (this.ticketTitleStrType == 4) {
				ticketLabelTitle.setText("票证核验失败   " + (ticketTimeIntevel - 1));
				ticketLabelTitle.setForeground(Color.RED);
			}
		}

		if (ticketTimeIntevel == 0) {
			if (this.ticketBackPanelType == 0) {
				log.debug("等待结束，回到TicketFrame初始界面.. " + ticketTimeIntevel);
				if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 正式代码时必须启用
					try {
						// DeviceEventListener.getInstance().resetTicketAndIDCard();
						log.debug("等待结束，clean 已刷的ticket and idCard!");
					} catch (Exception ex) {
						log.error("TicketVerifyFrame showDefaultContent:", ex);
					}
				}
				showTicketDefaultContent();
			} else if (this.ticketBackPanelType == 1) {
				showTicketDefaultContent();
			} else {
				this.showSuccWait("人脸核验中", "后面的旅客切勿刷票");
			}
		}
		if (ticketTimeIntevel-- < 0)
			ticketTimeIntevel = -1;
	}
}
