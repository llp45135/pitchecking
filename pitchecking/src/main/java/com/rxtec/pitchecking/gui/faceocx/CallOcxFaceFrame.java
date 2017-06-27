package com.rxtec.pitchecking.gui.faceocx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.SingleOcxMQReceiver;

public class CallOcxFaceFrame extends JFrame implements ActionListener {
	private Logger log = LoggerFactory.getLogger("CallOcxFaceFrame");
	private int cameraNo = 0;
	
	
	int faceTimeIntevel = Config.getInstance().getFaceCheckDelayTime();
	int ticketTimeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	private Timer timer = new Timer(1000, this);
	private JPanel contentPane;
	private JPanel panelCamera;
	private JLabel lableTicket;
	private JPanel panelTitle;
	private JLabel labelTitle;
	private int ticketFontSize = 100;

	private int faceTitleStrType = 0;
	private int ticketTitleStrType = 0;
	private int ticketBackPanelType = 0;
	
	private int cameraCanvasWidth = Config.getInstance().getUsbCameraCanvasWidth();
	private int cameraCanvasHeight = Config.getInstance().getUsbCameraCanvasHeight();

	private String displayMsg = "";

	public int getCameraNo() {
		return cameraNo;
	}

	public void setCameraNo(int cameraNo) {
		this.cameraNo = cameraNo;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Config appConfig = Config.getInstance();
		appConfig.setCameraNum(2);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice gd = gs[0];
		// log.debug("GraphicsDevice=="+gd);
		if (gd != null) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			CallOcxFaceFrame frame = new CallOcxFaceFrame();
			frame.setVisible(true);
			frame.initActiveX();
			frame.setLocation(xOff, yOff);
			
			frame.showSuccWait("人脸核验中", "后面的旅客切勿刷证");
		}

//		SingleOcxMQReceiver.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());// 启动PITEventTopic本地监听
	}

	/**
	 * Create the frame.
	 */
	public CallOcxFaceFrame() {
		setResizable(false);
		Color bgColor = new Color(0, 142, 240);
		setBackground(bgColor);
		setMinimumSize(new Dimension(1080, 1920));
		setMaximumSize(new Dimension(1080, 1920));
		setTitle("人脸检测");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 1080, 1920);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setMinimumSize(new Dimension(1080, 1920));
		contentPane.setMaximumSize(new Dimension(1080, 1920));
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		panelCamera = new JPanel();
		panelCamera.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelCamera.setBounds(23, 91, 1024, 1280);
		contentPane.add(panelCamera);

		JPanel panelTicket = new JPanel();
		panelTicket.setBackground(Color.BLACK);
		panelTicket.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelTicket.setBounds(0, 1375, 1080, 516);
		contentPane.add(panelTicket);
		panelTicket.setLayout(null);

		lableTicket = new JLabel();
		lableTicket.setHorizontalTextPosition(SwingConstants.CENTER);
		lableTicket.setBackground(Color.BLACK);
//		lableTicket.setText("请将二代证摆在验票区");
		lableTicket.setBorder(new LineBorder(Color.BLUE));
		lableTicket.setHorizontalAlignment(SwingConstants.CENTER);
		lableTicket.setFont(new Font("微软雅黑", Font.PLAIN, ticketFontSize));
		lableTicket.setBounds(23, 0, 1024, 516);
		panelTicket.add(lableTicket);

		ImageIcon initImg = new ImageIcon(DeviceConfig.initImgPath);
		lableTicket.setIcon(initImg);

		panelTitle = new JPanel();
		panelTitle.setBackground(bgColor);
		panelTitle.setBounds(0, 0, 1080, 90);
		contentPane.add(panelTitle);
		panelTitle.setLayout(null);

		labelTitle = new JLabel("请平视摄像头");
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 75));
		labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		labelTitle.setBounds(10, 5, 1060, 85);
		panelTitle.add(labelTitle);
		
		if (Config.getInstance().getIsVerifyScreenAlwaysTop() == 1)
			setAlwaysOnTop(true);

		faceTimeIntevel = -1;
		ticketTimeIntevel = -1;
		timer.start();
	}

	/**
	 * 人脸采集控件的SWT图形控件
	 */
	public void initActiveX() {
		BrowserCanvas browserCanvas = new BrowserCanvas(getCameraNo(),this.cameraCanvasWidth,this.cameraCanvasHeight);
		browserCanvas.setPreferredSize(new Dimension(cameraCanvasWidth, cameraCanvasHeight));
		panelCamera.add(browserCanvas, BorderLayout.CENTER);
		browserCanvas.initFaceVideoControl();
	}

	/**
	 * 单门模式初始界面
	 */
	public void showFaceDefaultContent() {

		Color bgColor = new Color(0, 142, 240);
		panelTitle.setBackground(bgColor);
		labelTitle.setBackground(bgColor);

		labelTitle.setForeground(Color.WHITE);
		labelTitle.setText("");

		faceTimeIntevel = -1;
	}

	/**
	 * 开始检脸
	 */
	public void showBeginCheckFaceContent() {

		panelTitle.setBackground(Color.ORANGE);
		labelTitle.setBackground(Color.ORANGE);
		labelTitle.setForeground(Color.BLACK);
		labelTitle.setText("");

		faceTimeIntevel = Config.getInstance().getFaceCheckDelayTime();
		this.faceTitleStrType = 2;
		labelTitle.setText("请平视摄像头和屏幕     " + faceTimeIntevel);
	}

	/**
	 * 验证通过处理
	 */
	public void showFaceCheckPassContent() {

	}

	/**
	 * 检脸失败处理
	 */
	public void showCheckFailedContent() {
		// faceTimeIntevel = 0;
		//
		// // panel_title.setBackground(Color.RED);
		// // panel_bottom.setBackground(Color.RED);
		// // label_title.setBackground(Color.RED);
		// //
		// // label_title.setForeground(Color.BLACK);
		// // label_title.setText("人脸识别失败！请从侧门离开");
		// // label_title.setBackground(null);
		// // panel_bottom.setVisible(true);
		//
		// this.cameraPanel.setVisible(false);
		// this.videoPanel.setVisible(false);
		//
		// msgPanel.setVisible(true);
		// msgPanel.setBackground(null); // 把背景设置为空
		// msgPanel.setOpaque(false); // 设置为透明
		// msgTopLabel.setBorder(null);
		// msgTopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 150));
		//
		// faceTimeIntevel = 5;
		// faceTitleStrType = 1;
		// this.msgTopLabel
		// .setText("<html><div align='center'>人脸识别失败</div><div
		// align='center'>请从侧门离开</div><div align='center'>"
		// + faceTimeIntevel + "</div></html>");
	}

	/**
	 * 主控端通过调用dll接口方式传输
	 */
	public void showFaceDisplayFromTK() {
		faceTimeIntevel = 0;

		String displayStr = DeviceConfig.getInstance().getFaceScreenDisplay();

		log.info("displayStr = " + displayStr);

		if (displayStr.indexOf("成功") != -1) {
			// ImageIcon icon = new ImageIcon(DeviceConfig.allowImgPath);
			// this.showPassStatusImage(icon);
			faceTimeIntevel = DeviceConfig.getInstance().getFaceScreenDisplayTimeout();
			// faceTimeIntevel =
			// MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).getFaceScreenDisplayTimeout();//
			// 5;
			// //
			// 成功时的提示信息存在时间
			// 暂时设置为5s
		} else {
			// ImageIcon icon = new ImageIcon(DeviceConfig.forbidenImgPath);
			// this.showPassStatusImage(icon);
			faceTimeIntevel = DeviceConfig.getInstance().getFaceScreenDisplayTimeout();
			// faceTimeIntevel =
			// MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum()).getFaceScreenDisplayTimeout();
		}
		faceTitleStrType = 4; // 4:覆盖一层panel 5：不覆盖

		if (displayStr.indexOf("成功") != -1 || displayStr.indexOf("succed") != -1) {
			labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 75));
			displayStr = "请通过    ";
		} else {
			displayStr = "核验失败!请走人工通道    ";
		}

		log.info("displayStr = " + displayStr);
		displayMsg = displayStr;
		this.labelTitle.setText(displayMsg + faceTimeIntevel);

		// if (displayStr.indexOf("#") != -1) { // 由#号，分两行
		// int kk = displayStr.indexOf("#");
		// String displayMsg1 = displayStr.substring(0, kk);
		// String displayMsg2 = displayStr.substring(kk + 1);
		// this.labelTitle.setText(displayMsg + " " + faceTimeIntevel);
		// } else {
		// this.displayMsg = "<html><div align='center'>" + displayStr +
		// "</div>";
		// this.labelTitle.setText(displayMsg + " " + faceTimeIntevel);
		// }
	}

	/**
	 * 
	 */
	public void showTicketDefaultContent() {
		ticketTimeIntevel = -1;
		ImageIcon initImg = new ImageIcon(DeviceConfig.initImgPath);
		lableTicket.setText("");
		lableTicket.setIcon(initImg);
	}

	/**
	 * 
	 * @param ticket
	 * @param idCard
	 * @param titleStrType
	 * @param backPanelType
	 */
	public void showWaitInputContent(Ticket ticket, IDCard idCard, int titleStrType, int backPanelType) {

		this.ticketTitleStrType = titleStrType;
		this.ticketBackPanelType = backPanelType;

		if (idCard != null) {
			this.labelTitle.setText("还需扫火车票二维码");
		}
		if (ticket != null) {
			this.labelTitle.setText("还需刷第二代身份证");
		}

		ticketTimeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	}

	/**
	 * 
	 * @param msg1
	 * @param msg2
	 */
	public void showSuccWait(String msg1, String msg2) {
		lableTicket.setIcon(null);
		lableTicket.setFont(new Font("微软雅黑", Font.PLAIN, ticketFontSize));
		lableTicket.setForeground(Color.RED);
		lableTicket.setText(
				"<html><div align='center'>" + msg1 + "</div><div align='center'>" + msg2 + "</div></html>");
		ticketTimeIntevel = -1;
	}

	/**
	 * 
	 * @param deviceConfig
	 * @param titleStrType
	 * @param exMsg
	 */
	public void showExceptionContent(DeviceConfig deviceConfig, int titleStrType, String exMsg) {
		this.ticketTitleStrType = titleStrType;
		if (exMsg.equals("暂停服务")) {
			labelTitle.setText("");
		} else {
			labelTitle.setText("设备故障");
		}
		lableTicket.setText("<html><div align='center'>" + exMsg + "</div></html>");

		ticketTimeIntevel = -1;
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
			
			ticketTimeIntevel = 3;

			lableTicket.setIcon(null);
			lableTicket.setFont(new Font("微软雅黑", Font.PLAIN, ticketFontSize));
			lableTicket.setForeground(Color.RED);
			lableTicket.setText(
					"<html><div align='center'>" + msg1 + "</div><div align='center'>" + msg2 + "</div>" + "</html>");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// this.timeRefresh();

		if (faceTimeIntevel > 0) {
			if (this.faceTitleStrType == 0) {
				// label_title.setText("人脸识别成功！请通过" + " " + (timeIntevel - 1));

				labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 75));
				labelTitle.setText("请通过    " + (faceTimeIntevel - 1));
			} else if (this.faceTitleStrType == 1) {
				// label_title.setText("人脸识别失败！请从侧门离开" + " " + (timeIntevel -
				// 1));

				labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 75));
				labelTitle.setText("核验失败!请走人工通道    " + (faceTimeIntevel - 1));
			} else if (this.faceTitleStrType == 4) {
				labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 75));
				labelTitle.setText(displayMsg + (faceTimeIntevel - 1));
			} else if (this.faceTitleStrType == 5) {
				labelTitle.setText(displayMsg + (faceTimeIntevel - 1));
				// label_title.setBackground(null);
			} else {
				labelTitle.setText("请平视摄像头和屏幕    " + (faceTimeIntevel - 1));
			}
		}
		if (faceTimeIntevel == 0) {
			this.showFaceDefaultContent();
			// this.showTicketDefaultContent();
		}

		if (faceTimeIntevel-- < 0)
			faceTimeIntevel = -1;

		/**
		 * 
		 */
		if (ticketTimeIntevel >= 0) {
			if (this.ticketTitleStrType == 1) {
				labelTitle.setText("还需扫火车票二维码 " + (ticketTimeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.ticketTitleStrType == 2) {
				labelTitle.setText("还需刷第二代身份证 " + (ticketTimeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.ticketTitleStrType == 3) {
				labelTitle.setText("票证核验成功 " + (ticketTimeIntevel - 1));
				labelTitle.setForeground(Color.GREEN);
			} else if (this.ticketTitleStrType == 4) {
				labelTitle.setText("票证核验失败 " + (ticketTimeIntevel - 1));
				labelTitle.setForeground(Color.RED);
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
			} else {
				showTicketDefaultContent();
			}
		}
		if (ticketTimeIntevel-- < 0)
			ticketTimeIntevel = -1;
	}
}
