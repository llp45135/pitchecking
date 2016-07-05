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

import com.rxtec.pitchecking.Config;
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
	private JLabel labelMsg;
	private JLabel lableWarnmsg;
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
					Config config = Config.getInstance();
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
					frame.showTicketContent(config, ticket, 3);
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
		setMinimumSize(new Dimension(1024, 768));
		setMaximumSize(new Dimension(1024, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		topPanel = new JPanel();
		topPanel.setBackground(Color.CYAN);
		topPanel.setMinimumSize(new Dimension(1024, 100));
		topPanel.setMaximumSize(new Dimension(1024, 100));
		contentPane.add(topPanel);
		topPanel.setLayout(null);

		labelTitle = new JLabel("请刷二代证和车票二维码");
		labelTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 42));
		labelTitle.setBounds(10, 31, 734, 57);
		topPanel.add(labelTitle);

		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.RED);
		timelabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		timelabel.setBounds(790, 56, 195, 26);
		topPanel.add(timelabel);

		ticketPanel = new JPanel();
		ticketPanel.setBackground(Color.WHITE);
		ticketPanel.setMinimumSize(new Dimension(1024, 568));
		ticketPanel.setMaximumSize(new Dimension(1024, 568));
		contentPane.add(ticketPanel);
		ticketPanel.setLayout(null);

		/**
		 * 初始化启动界面
		 */
		initPanel = new TicketInitPanel();

		labelTrainCode = new JLabel("G6612");
		labelTrainCode.setHorizontalAlignment(SwingConstants.CENTER);
		labelTrainCode.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelTrainCode.setBounds(365, 67, 110, 54);
		ticketPanel.add(labelTrainCode);

		labelFz = new JLabel("广州南");
		labelFz.setHorizontalAlignment(SwingConstants.CENTER);
		labelFz.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelFz.setBounds(490, 67, 110, 54);
		ticketPanel.add(labelFz);

		labelZhi = new JLabel("至");
		labelZhi.setHorizontalAlignment(SwingConstants.CENTER);
		labelZhi.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelZhi.setBounds(617, 70, 48, 54);
		ticketPanel.add(labelZhi);

		labelDz = new JLabel("长沙南");
		labelDz.setHorizontalAlignment(SwingConstants.CENTER);
		labelDz.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelDz.setBounds(690, 70, 110, 54);
		ticketPanel.add(labelDz);

		label_rq = new JLabel("乘车日期");
		label_rq.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		label_rq.setBounds(410, 149, 134, 54);
		ticketPanel.add(label_rq);

		labelTrainDate = new JLabel("2016年5月16日");
		labelTrainDate.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelTrainDate.setBounds(553, 149, 262, 54);
		ticketPanel.add(labelTrainDate);

		labelSeatType = new JLabel("二等软座");
		labelSeatType.setHorizontalAlignment(SwingConstants.CENTER);
		labelSeatType.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelSeatType.setBounds(448, 220, 134, 54);
		ticketPanel.add(labelSeatType);

		labelTicketType = new JLabel("全票");
		labelTicketType.setHorizontalAlignment(SwingConstants.CENTER);
		labelTicketType.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		labelTicketType.setBounds(634, 223, 110, 54);
		ticketPanel.add(labelTicketType);

		labelMsg = new JLabel("请过闸!");
		labelMsg.setHorizontalAlignment(SwingConstants.CENTER);
		labelMsg.setForeground(Color.GREEN);
		labelMsg.setFont(new Font("微软雅黑", Font.PLAIN, 36));
		labelMsg.setBounds(346, 378, 575, 54);
		ticketPanel.add(labelMsg);

		lableImg = new JLabel("");
		lableImg.setHorizontalAlignment(SwingConstants.CENTER);
		lableImg.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lableImg.setBounds(151, 154, 180, 215);
		ticketPanel.add(lableImg);

		lableWarnmsg = new JLabel("票证不符，请检查后重新刷票！");
		lableWarnmsg.setForeground(Color.RED);
		lableWarnmsg.setFont(new Font("微软雅黑", Font.PLAIN, 36));
		lableWarnmsg.setHorizontalAlignment(SwingConstants.CENTER);
		lableWarnmsg.setBounds(337, 303, 588, 46);
		ticketPanel.add(lableWarnmsg);

		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.CYAN);
		bottomPanel.setMinimumSize(new Dimension(1024, 100));
		bottomPanel.setMaximumSize(new Dimension(1024, 100));
		bottomPanel.setLayout(null);

		setUndecorated(true);

		showDefaultContent();
	}

	public JPanel getTicketPanel() {
		return this.ticketPanel;
	}

	public void showStatusImage(ImageIcon icon) {
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setIcon(icon);
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
	public void showTicketContent(Config config, Ticket ticket, int titleStrType) {
		this.titleStrType = titleStrType;
		try {
			// ticket.printTicket();
			if (ticket != null) {
				labelFz.setText(config.getStationName(ticket.getFromStationCode()));
				this.labelZhi.setText("至");
				this.labelDz.setText(config.getStationName(ticket.getEndStationCode()));
				this.labelTrainCode.setText(ticket.getTrainCode());
				this.label_rq.setText("乘车日期:");
				String strTrainDate = ticket.getTrainDate().substring(0, 4) + "年"
						+ ticket.getTrainDate().substring(4, 6) + "月" + ticket.getTrainDate().substring(6, 8) + "日";
				this.labelTrainDate.setText(strTrainDate);
				this.labelTicketType
						.setText(config.getTicketTypesMap().get(Integer.parseInt(ticket.getTicketType())) + "票");
				this.labelSeatType.setText(config.getSeatTypesMap().get(ticket.getSeatCode()));
			}
			ImageIcon icon = new ImageIcon(DeviceConfig.allowImgPath);
			this.lableWarnmsg.setText("");
			this.labelMsg.setText("请通行!");
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
	public void showFailedContent(Config config, Ticket ticket, int titleStrType, String failedMsg) {
		this.titleStrType = titleStrType;
		try {
			// ticket.printTicket();

			labelFz.setText(config.getStationName(ticket.getFromStationCode()));
			this.labelZhi.setText("至");
			this.labelDz.setText(config.getStationName(ticket.getEndStationCode()));
			this.labelTrainCode.setText(ticket.getTrainCode());
			this.label_rq.setText("乘车日期:");
			String strTrainDate = ticket.getTrainDate().substring(0, 4) + "年" + ticket.getTrainDate().substring(4, 6)
					+ "月" + ticket.getTrainDate().substring(6, 8) + "日";
			this.labelTrainDate.setText(strTrainDate);
			this.labelTicketType
					.setText(config.getTicketTypesMap().get(Integer.parseInt(ticket.getTicketType())) + "票");
			this.labelSeatType.setText(config.getSeatTypesMap().get(ticket.getSeatCode()));
			ImageIcon icon = new ImageIcon(DeviceConfig.forbidenImgPath);
			this.lableWarnmsg.setText(failedMsg);
			this.labelMsg.setText("");
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
	public void showExceptionContent(Config config, int titleStrType, String exMsg) {
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
			this.lableWarnmsg.setText(exMsg);
			this.labelMsg.setText("");
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
				labelTitle.setText("请扫描车票二维码!   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.titleStrType == 2) {
				labelTitle.setText("请刷二代证!   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			} else if (this.titleStrType == 3) {
				labelTitle.setText("票证核验成功!   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.blue);
			} else if (this.titleStrType == 4) {
				labelTitle.setText("票证核验失败,请重试!   " + (timeIntevel - 1));
				labelTitle.setForeground(Color.RED);
			}
		}

		if (timeIntevel == 0) {
			log.debug("等待结束，回到初始界面.. " + timeIntevel);
			showDefaultContent();
		}
		if (timeIntevel-- < 0)
			timeIntevel = -1;
	}
}
