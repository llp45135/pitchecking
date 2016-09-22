package com.rxtec.pitchecking.gui;

import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.DateUtils;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.SystemColor;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;

@SuppressWarnings("serial")
public class TicketCheckFrame extends JFrame implements ActionListener {

	private Logger log = LoggerFactory.getLogger("TicketCheckFrame");
	private Timer timer = new Timer(1000, this);
	private JPanel contentPane;
	private JPanel topPanel;
	private JPanel ticketPanel;
	private TicketInitPanel ticketInitPanel;
	private WaitPanel waitPanel;
	private InitPanel secondInitPanel;
	private JPanel bottomPanel;
	//
	private JLabel labelTitle;
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
	int timeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	private JLabel timelabel;
	private int titleStrType = 0;
	private int backPanelType = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DeviceConfig deviceConfig = DeviceConfig.getInstance();
					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					GraphicsDevice[] gs = ge.getScreenDevices();
					TicketCheckFrame frame = new TicketCheckFrame();
					frame.setVisible(true);
					gs[0].setFullScreenWindow(frame);

					Ticket ticket = new Ticket();
					ticket.setFromStationCode("KNQ");
					ticket.setEndStationCode("GGQ");
					ticket.setTrainCode("G6612");
					ticket.setTrainDate("20160405");
					ticket.setTicketType("1");
					ticket.setSeatCode("O");

					frame.showTicketContent(deviceConfig, ticket, 3, 0);

					// frame.showFailedContent(deviceConfig, ticket, 4,
					// "票证未通过核验，请重试!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public int getBackPanelType() {
		return backPanelType;
	}

	public void setBackPanelType(int backPanelType) {
		this.backPanelType = backPanelType;
	}

	/**
	 * Create the frame.
	 */
	public TicketCheckFrame() {
		setBounds(new Rectangle(0, 0, 1280, DeviceConfig.TICKET_FRAME_HEIGHT));
		setMinimumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_HEIGHT));
		setMaximumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_HEIGHT));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		topPanel = new JPanel();
		topPanel.setBackground(Color.CYAN);
		topPanel.setMinimumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_TOPHEIGHT));
		topPanel.setMaximumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_TOPHEIGHT));
		contentPane.add(topPanel);
		topPanel.setLayout(null);

		labelTitle = new JLabel("请刷二代证和车票二维码");
		labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 55));
		labelTitle.setBounds(318, 0, 644, 62);
		topPanel.add(labelTitle);

		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.RED);
		timelabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
		timelabel.setBounds(981, 15, 262, 45);
		topPanel.add(timelabel);

		ticketPanel = new JPanel();
		ticketPanel.setBackground(Color.WHITE);
		ticketPanel.setMinimumSize(new Dimension(1280, 608));
		ticketPanel.setMaximumSize(new Dimension(1280, 608));
		contentPane.add(ticketPanel);
		ticketPanel.setLayout(null);

		/**
		 * 初始化启动界面
		 */
		ticketInitPanel = new TicketInitPanel();
		secondInitPanel = new InitPanel();
		waitPanel = new WaitPanel();

		labelTrainCode = new JLabel("G6612");
		labelTrainCode.setHorizontalAlignment(SwingConstants.CENTER);
		labelTrainCode.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTrainCode.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTrainCode.setBounds(342, 67, 225, 72);
		ticketPanel.add(labelTrainCode);

		labelFz = new JLabel("广州南");
		labelFz.setHorizontalAlignment(SwingConstants.CENTER);
		labelFz.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelFz.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelFz.setBounds(593, 67, 245, 72);
		ticketPanel.add(labelFz);

		labelZhi = new JLabel("至");
		labelZhi.setHorizontalAlignment(SwingConstants.CENTER);
		labelZhi.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelZhi.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelZhi.setBounds(848, 67, 87, 72);
		ticketPanel.add(labelZhi);

		labelDz = new JLabel("长沙南");
		labelDz.setHorizontalAlignment(SwingConstants.CENTER);
		labelDz.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelDz.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelDz.setBounds(945, 67, 237, 72);
		ticketPanel.add(labelDz);

		label_rq = new JLabel("乘车日期");
		label_rq.setHorizontalAlignment(SwingConstants.CENTER);
		label_rq.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// label_rq.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		label_rq.setBounds(410, 190, 271, 72);
		ticketPanel.add(label_rq);

		labelTrainDate = new JLabel("2016年5月16日");
		labelTrainDate.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTrainDate.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTrainDate.setBounds(742, 190, 440, 72);
		ticketPanel.add(labelTrainDate);

		labelSeatType = new JLabel("二等软座");
		labelSeatType.setHorizontalAlignment(SwingConstants.CENTER);
		labelSeatType.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelSeatType.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelSeatType.setBounds(410, 299, 351, 72);
		ticketPanel.add(labelSeatType);

		labelTicketType = new JLabel("全票");
		labelTicketType.setHorizontalAlignment(SwingConstants.CENTER);
		labelTicketType.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		// labelTicketType.setBorder(new LineBorder(new Color(0, 0, 0), 1,
		// true));
		labelTicketType.setBounds(809, 299, 245, 72);
		ticketPanel.add(labelTicketType);

		lableImg = new JLabel("");
		lableImg.setHorizontalAlignment(SwingConstants.CENTER);
		lableImg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableImg.setBounds(90, 172, 180, 215);
		ticketPanel.add(lableImg);

		lableWarnmsg = new JLabel("请通行！");
		lableWarnmsg.setForeground(Color.RED);
		lableWarnmsg.setFont(new Font("微软雅黑", Font.PLAIN, 56));
		lableWarnmsg.setHorizontalAlignment(SwingConstants.CENTER);
		// lableWarnmsg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableWarnmsg.setBounds(342, 418, 840, 82);
		ticketPanel.add(lableWarnmsg);

		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.CYAN);
		bottomPanel.setMinimumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT));
		bottomPanel.setMaximumSize(new Dimension(1280, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT));
		bottomPanel.setLayout(null);

		labelVersion = new JLabel("版本号：pitcheck160709.02");
		labelVersion.setForeground(Color.BLACK);
		labelVersion.setFont(new Font("微软雅黑", Font.PLAIN, 23));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		labelVersion.setBounds(99, 0, 302, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		bottomPanel.add(labelVersion);

		lblIp = new JLabel("IP地址：192.168.1.5");
		lblIp.setForeground(Color.BLACK);
		lblIp.setHorizontalAlignment(SwingConstants.CENTER);
		lblIp.setFont(new Font("微软雅黑", Font.PLAIN, 23));
		lblIp.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lblIp.setBounds(902, 0, 276, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		bottomPanel.add(lblIp);

		bootTime = new JLabel("启动时间：2016-08-13 17:45:45");
		bootTime.setHorizontalAlignment(SwingConstants.CENTER);
		bootTime.setForeground(Color.BLACK);
		bootTime.setFont(new Font("微软雅黑", Font.PLAIN, 23));
		bootTime.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		bootTime.setBounds(474, 0, 367, DeviceConfig.TICKET_FRAME_BOTTOMHEIGHT);
		bottomPanel.add(bootTime);

		setUndecorated(true);

		showDefaultContent();

		setSoftVersion("软件版本号：" + DeviceConfig.softVersion);
		setGateIP("IP地址：" + DeviceConfig.getInstance().getIpAddress());
		this.bootTime.setText("启动时间:" + DateUtils.getStringDate());
		this.bootTime.setBorder(null);
	}

	public JPanel getTicketPanel() {
		return this.ticketPanel;
	}

	public void showStatusImage(ImageIcon icon) {
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setIcon(icon);
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
	 * 默认界面
	 */
	public void showDefaultContent() {
		this.labelTitle.setText("");
		labelTitle.setForeground(Color.blue);
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

		ticketInitPanel.setVisible(false);
		ticketPanel.setVisible(false);
		waitPanel.setVisible(false);
		secondInitPanel.setVisible(true);
		secondInitPanel.setBackground(Color.black);
		contentPane.remove(waitPanel);
		contentPane.remove(ticketPanel);
		contentPane.remove(bottomPanel);
		contentPane.remove(ticketInitPanel);
		contentPane.add(secondInitPanel);
		contentPane.add(bottomPanel);

		contentPane.repaint();

		timeIntevel = -1;
		timer.start();
	}

	/**
	 * 等待输入信息界面
	 * 
	 * @param ticket
	 * @param idCard
	 */
	public void showWaitInputContent(Ticket ticket, IDCard idCard, int titleStrType, int backPanelType) {
		// DeviceManager.getInstance().repaintStop();
		this.titleStrType = titleStrType;
		this.backPanelType = backPanelType;
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

		ticketPanel.setVisible(false);
		waitPanel.setVisible(false);
		secondInitPanel.setVisible(false);
		ticketInitPanel.setVisible(true);
		ticketInitPanel.setBackground(Color.WHITE);
		contentPane.remove(waitPanel);
		contentPane.remove(ticketPanel);
		contentPane.remove(secondInitPanel);
		contentPane.remove(bottomPanel);
		contentPane.add(ticketInitPanel);
		contentPane.add(bottomPanel);

		if (idCard != null) {
			if (DeviceConfig.getInstance().getVersionFlag() == 1) {
				ticketInitPanel.showIDCardImage(new ImageIcon(DeviceConfig.readedIdImgPath));
			} else {
				ticketInitPanel.showIDCardImage(new ImageIcon(idCard.getCardImage()));
			}
		} else {
			// ticketInitPanel.showIDCardImage(new
			// ImageIcon(DeviceConfig.idReaderImgPath));
			ticketInitPanel.showIDCardImage(null);
		}
		if (ticket != null) {
			ticketInitPanel.showTicketInfo(new ImageIcon(DeviceConfig.readedQRImgPath));
		} else {
			// ticketInitPanel.showTicketInfo(new
			// ImageIcon(DeviceConfig.qrReaderImgPath));
			ticketInitPanel.showTicketInfo(null);
		}

		contentPane.repaint();

		timeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
	}

	/**
	 * 成功核验车票信息界面
	 * 
	 * @param ticket
	 */
	public void showTicketContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType, int backPanelType) {
		this.titleStrType = titleStrType;
		this.backPanelType = backPanelType;
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
			// secondInitPanel.setVisible(false);
			// waitPanel.setVisible(false);
			// ticketInitPanel.setVisible(false);
			// contentPane.remove(secondInitPanel);
			// contentPane.remove(ticketInitPanel);
			// contentPane.remove(waitPanel);
			// contentPane.remove(bottomPanel);
			// contentPane.add(ticketPanel);
			// contentPane.add(bottomPanel);
			// contentPane.repaint();

			this.showSuccWait("请取走车票和身份证！", "往前走进通道");

			timeIntevel = DeviceConfig.getInstance().getSuccTimeDelay();
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
			labelTitle.setText("");

			waitPanel.showWaitMsg(msg1, msg2);

			ticketPanel.setVisible(false);
			waitPanel.setVisible(true);
			ticketInitPanel.setVisible(false);
			secondInitPanel.setVisible(false);
			contentPane.remove(secondInitPanel);
			contentPane.remove(ticketInitPanel);
			contentPane.remove(ticketPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(waitPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 核验失败信息界面
	 */
	public void showFailedContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType, int backPanelType,
			String failedMsg) {
		this.titleStrType = titleStrType;
		this.backPanelType = backPanelType;
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

			ticketPanel.setVisible(true);
			waitPanel.setVisible(false);
			ticketInitPanel.setVisible(false);
			secondInitPanel.setVisible(false);
			contentPane.remove(secondInitPanel);
			contentPane.remove(ticketInitPanel);
			contentPane.remove(waitPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(ticketPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = DeviceConfig.getInstance().getReaderTimeDelay();
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
		this.titleStrType = titleStrType;
		try {
			// ticket.printTicket();
			labelTitle.setText("设备故障");
			labelTitle.setForeground(Color.RED);
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

			ticketPanel.setVisible(true);
			secondInitPanel.setVisible(false);
			waitPanel.setVisible(false);
			ticketInitPanel.setVisible(false);
			contentPane.remove(secondInitPanel);
			contentPane.remove(ticketInitPanel);
			contentPane.remove(waitPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(ticketPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void timeRefresh() {
		String now = DateUtils.getStringDate();
		timelabel.setText(now);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		timeRefresh();
		// log.debug("TicketCheckFrame 计时开始==" + (timeIntevel - 1));
		if (timeIntevel >= 0) {
			if (this.titleStrType == 1) {
				labelTitle.setText("还需扫火车票二维码   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.titleStrType == 2) {
				labelTitle.setText("还需刷第二代身份证   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.titleStrType == 3) {
				labelTitle.setText("票证核验成功   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.GREEN);
			} else if (this.titleStrType == 4) {
				labelTitle.setText("票证核验失败   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			}
		}

		if (timeIntevel == 0) {
			if (this.backPanelType == 0) {
				log.debug("等待结束，回到TicketFrame初始界面.. " + timeIntevel);
				if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 正式代码时必须启用
					try {
						DeviceEventListener.getInstance().resetTicketAndIDCard();
						log.debug("等待结束，clean 已刷的ticket and idCard!");
					} catch (Exception ex) {
						log.error("TicketCheckFrame showDefaultContent:", ex);
					}
				}
				showDefaultContent();
			} else {
				this.showSuccWait("后面的旅客请稍候!", "");
			}
		}
		if (timeIntevel-- < 0)
			timeIntevel = -1;
	}
}
