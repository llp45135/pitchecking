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
import com.rxtec.pitchecking.device.Ticket;
import com.rxtec.pitchecking.utils.GetDate;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.SystemColor;

@SuppressWarnings("serial")
public class TicketCheckFrame extends JFrame implements ActionListener {


	private Logger log = LoggerFactory.getLogger("TicketCheckFrame");
	private Timer timer = new Timer(1000, this);
	private JPanel contentPane;
	private JPanel topPanel;
	private JPanel ticketPanel;
	private JPanel initPanel;
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
	int timeIntevel = 5;
	private JLabel timelabel;

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
					frame.showTicketContent(config, ticket);
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
		
		labelTitle = new JLabel("请扫描车票正面右下角的二维码");
		labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setFont(new Font("微软雅黑", Font.PLAIN, 36));
		labelTitle.setBounds(235, 0, 535, 93);
		topPanel.add(labelTitle);
		
		timelabel = new JLabel("yyyyMMdd hh:mm:ss");
		timelabel.setForeground(Color.RED);
		timelabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		timelabel.setBounds(794, 42, 195, 26);
		topPanel.add(timelabel);
		
		ticketPanel = new JPanel();
		ticketPanel.setBackground(Color.WHITE);
		ticketPanel.setMinimumSize(new Dimension(1024, 568));
		ticketPanel.setMaximumSize(new Dimension(1024, 568));
		contentPane.add(ticketPanel);
		ticketPanel.setLayout(null);
		
		initPanel = new TicketInitPanel();
		initPanel.setBackground(Color.WHITE);
		
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
		labelMsg.setBounds(337, 373, 478, 54);
		ticketPanel.add(labelMsg);
		
		lableImg = new JLabel("");
		lableImg.setHorizontalAlignment(SwingConstants.CENTER);
		lableImg.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		lableImg.setBounds(185, 139, 140, 180);
		ticketPanel.add(lableImg);
		
		
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

		 this.labelTitle.setText("请扫描车票正面右下角的二维码");
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

		contentPane.repaint();

		timeIntevel = -1;
		timer.start();
		//
		Config.getInstance().setDealQR(true);
	}
	
	/**
	 * 
	 * @param ticket
	 */
	public void showTicketContent(Config config, Ticket ticket) {

		try {
//			ticket.printTicket();

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
			ImageIcon icon = new ImageIcon("./img/tky_allow.gif");
			this.labelMsg.setText("请过闸!!");
			showStatusImage(icon);

			ticketPanel.setVisible(true);
			initPanel.setVisible(false);
			contentPane.remove(initPanel);
			contentPane.remove(bottomPanel);
			contentPane.add(ticketPanel);
			contentPane.add(bottomPanel);
			contentPane.repaint();

			timeIntevel = 5;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void timeRefresh() {
		String now = GetDate.getStringDate();
		timelabel.setText(now);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		timeRefresh();
		if (timeIntevel-- < 0)
			timeIntevel = -1;
		if (timeIntevel >= 0) {
//			log.debug("请抓紧时间小心通过闸门     " + timeIntevel);
			labelTitle.setText("请抓紧时间小心通过闸门     " + timeIntevel);
		}
		if (timeIntevel == 0) {
			// log.debug("showDefaultContent " + timeIntevel);
			showDefaultContent();
		}
	}

}
