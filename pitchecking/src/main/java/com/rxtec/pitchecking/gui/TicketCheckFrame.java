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

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.IDReader;
import com.rxtec.pitchecking.QRReader;
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
	private TicketInitPanel initPanel;
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
	int timeIntevel = 5;
	private JLabel timelabel;
	private int titleStrType = 0;

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
					frame.showTicketContent(deviceConfig, ticket, 3);
//					frame.showFailedContent(deviceConfig, ticket, 4, "票证未通过核验，请重试!");
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
		setBounds(new Rectangle(0, 0, 1920, 1080));
		setMinimumSize(new Dimension(1280, 768));
		setMaximumSize(new Dimension(1280, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		topPanel = new JPanel();
		topPanel.setBackground(Color.CYAN);
		topPanel.setMinimumSize(new Dimension(1280, 100));
		topPanel.setMaximumSize(new Dimension(1280, 100));
		contentPane.add(topPanel);
		topPanel.setLayout(null);

		labelTitle = new JLabel("请刷二代证和车票二维码");
		labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 48));
		labelTitle.setBounds(10, 24, 1250, 64);
		topPanel.add(labelTitle);

		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.RED);
		timelabel.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		timelabel.setBounds(958, 10, 285, 26);
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
		initPanel = new TicketInitPanel();

		labelTrainCode = new JLabel("G6612");
		labelTrainCode.setHorizontalAlignment(SwingConstants.CENTER);
		labelTrainCode.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelTrainCode.setBounds(566, 70, 110, 54);
		ticketPanel.add(labelTrainCode);

		labelFz = new JLabel("广州南");
		labelFz.setHorizontalAlignment(SwingConstants.CENTER);
		labelFz.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelFz.setBounds(717, 70, 110, 54);
		ticketPanel.add(labelFz);

		labelZhi = new JLabel("至");
		labelZhi.setHorizontalAlignment(SwingConstants.CENTER);
		labelZhi.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelZhi.setBounds(864, 70, 48, 54);
		ticketPanel.add(labelZhi);

		labelDz = new JLabel("长沙南");
		labelDz.setHorizontalAlignment(SwingConstants.CENTER);
		labelDz.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelDz.setBounds(947, 70, 110, 54);
		ticketPanel.add(labelDz);

		label_rq = new JLabel("乘车日期");
		label_rq.setHorizontalAlignment(SwingConstants.CENTER);
		label_rq.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		label_rq.setBounds(566, 149, 198, 54);
		ticketPanel.add(label_rq);

		labelTrainDate = new JLabel("2016年5月16日");
		labelTrainDate.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelTrainDate.setBounds(795, 149, 262, 54);
		ticketPanel.add(labelTrainDate);

		labelSeatType = new JLabel("二等软座");
		labelSeatType.setHorizontalAlignment(SwingConstants.CENTER);
		labelSeatType.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelSeatType.setBounds(672, 223, 134, 54);
		ticketPanel.add(labelSeatType);

		labelTicketType = new JLabel("全票");
		labelTicketType.setHorizontalAlignment(SwingConstants.CENTER);
		labelTicketType.setFont(new Font("微软雅黑", Font.PLAIN, 32));
		labelTicketType.setBounds(854, 223, 110, 54);
		ticketPanel.add(labelTicketType);

		lableImg = new JLabel("");
		lableImg.setHorizontalAlignment(SwingConstants.CENTER);
		lableImg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableImg.setBounds(270, 149, 180, 215);
		ticketPanel.add(lableImg);

		lableWarnmsg = new JLabel("请通行！");
		lableWarnmsg.setForeground(Color.RED);
		lableWarnmsg.setFont(new Font("微软雅黑", Font.PLAIN, 40));
		lableWarnmsg.setHorizontalAlignment(SwingConstants.CENTER);
		lableWarnmsg.setBounds(508, 322, 615, 65);
		ticketPanel.add(lableWarnmsg);

		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.CYAN);
		bottomPanel.setMinimumSize(new Dimension(1280, 60));
		bottomPanel.setMaximumSize(new Dimension(1280, 60));
		bottomPanel.setLayout(null);
		
		labelVersion = new JLabel("版本号：pitcheck160709.02");
		labelVersion.setForeground(Color.BLACK);
		labelVersion.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setBounds(96, 10, 302, 42);
		bottomPanel.add(labelVersion);
		
		lblIp = new JLabel("IP地址：192.168.1.5");
		lblIp.setForeground(Color.BLACK);
		lblIp.setHorizontalAlignment(SwingConstants.CENTER);
		lblIp.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		lblIp.setBounds(928, 10, 276, 42);
		bottomPanel.add(lblIp);

		setUndecorated(true);

		showDefaultContent();
		
		setSoftVersion("软件版本号：" + DeviceConfig.softVersion);
		setGateIP("IP地址：" + DeviceConfig.getInstance().getIpAddress());
	}

	public JPanel getTicketPanel() {
		return this.ticketPanel;
	}

	public void showStatusImage(ImageIcon icon) {
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setIcon(icon);
	}
	
	public void setSoftVersion(String verText){
		labelVersion.setText(verText);
	}
	
	public void setGateIP(String ipText){
		lblIp.setText(ipText);
	}

	public void showDefaultContent() {

		this.labelTitle.setText("请刷二代证和车票二维码");
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

		ticketPanel.setVisible(false);
		initPanel.setVisible(true);
		initPanel.setBackground(Color.WHITE);
		initPanel.showIDCardImage(new ImageIcon(DeviceConfig.idReaderImgPath));
		initPanel.showTicketInfo(new ImageIcon(DeviceConfig.qrReaderImgPath));
		contentPane.remove(ticketPanel);
		contentPane.remove(bottomPanel);
		contentPane.add(initPanel);
		contentPane.add(bottomPanel);

		contentPane.repaint();

		timeIntevel = -1;
		timer.start();
	}

	/**
	 * 重画等待输入信息界面
	 * 
	 * @param ticket
	 * @param idCard
	 */
	public void showWaitInputContent(Ticket ticket, IDCard idCard, int titleStrType) {
		// DeviceManager.getInstance().repaintStop();
		this.titleStrType = titleStrType;
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
		initPanel.setVisible(true);
		initPanel.setBackground(Color.WHITE);
		contentPane.remove(ticketPanel);
		contentPane.remove(bottomPanel);
		contentPane.add(initPanel);
		contentPane.add(bottomPanel);

		if (idCard != null) {
//			initPanel.showIDCardImage(new ImageIcon(idCard.getCardImage()));
			initPanel.showIDCardImage(new ImageIcon(DeviceConfig.readedIdImgPath));
		}
		if (ticket != null) {
			initPanel.showTicketInfo(new ImageIcon(DeviceConfig.readerQRImgPath));
		}

		contentPane.repaint();

		timeIntevel = 4;
	}

	/**
	 * 重画成功核验车票信息界面
	 * 
	 * @param ticket
	 */
	public void showTicketContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType) {
		this.titleStrType = titleStrType;
		try {
			// ticket.printTicket();
			if (ticket != null) {
				labelFz.setText(deviceConfig.getStationName(ticket.getFromStationCode()));
				this.labelZhi.setText("至");
				this.labelDz.setText(deviceConfig.getStationName(ticket.getEndStationCode()));
				this.labelTrainCode.setText(ticket.getTrainCode());
				this.label_rq.setText("乘车日期:");
				String strTrainDate = ticket.getTrainDate().substring(0, 4) + "年"
						+ ticket.getTrainDate().substring(4, 6) + "月" + ticket.getTrainDate().substring(6, 8) + "日";
				this.labelTrainDate.setText(strTrainDate);
				this.labelTicketType
						.setText(deviceConfig.getTicketTypesMap().get(Integer.parseInt(ticket.getTicketType())) + "票");
				this.labelSeatType.setText(deviceConfig.getSeatTypesMap().get(ticket.getSeatCode()));
			}
			ImageIcon icon = new ImageIcon(DeviceConfig.allowImgPath);
			lableWarnmsg.setForeground(Color.GREEN);
			this.lableWarnmsg.setText("请通行!");
			showStatusImage(icon);

			ticketPanel.setVisible(true);
			initPanel.setVisible(false);
			contentPane.remove(initPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(ticketPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = 4;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 重画核验失败信息界面
	 */
	public void showFailedContent(DeviceConfig deviceConfig, Ticket ticket, int titleStrType, String failedMsg) {
		this.titleStrType = titleStrType;
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
			initPanel.setVisible(false);
			contentPane.remove(initPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(ticketPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = 4;
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
			labelTitle.setText("设备异常!!");
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
			initPanel.setVisible(false);
			contentPane.remove(initPanel);
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
				labelTitle.setText("请扫描车票二维码   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.titleStrType == 2) {
				labelTitle.setText("请刷第二代身份证   " + (timeIntevel - 1));
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
			log.debug("等待结束，回到TicketFrame初始界面.. " + timeIntevel);
			showDefaultContent();
		}
		if (timeIntevel-- < 0)
			timeIntevel = -1;
	}
}
