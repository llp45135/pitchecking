package com.rxtec.pitchecking.gui.sum;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.mysql.PitRecordSqlDao;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.CalUtils;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent;

public class CheckSumFrame extends JFrame {

	private Logger log = LoggerFactory.getLogger("CheckSumFrame");
	private JPanel contentPane;
	private JTextField rqTextField;

	PitRecordSqlDao pitRecordSqlDao;
	private JTextField yzTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CheckSumFrame frame = new CheckSumFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CheckSumFrame() {
		setBounds(new Rectangle(100, 0, 1024, 768));
		setMinimumSize(new Dimension(1024, 768));
		setMaximumSize(new Dimension(1024, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setMinimumSize(new Dimension(1024, 768));
		contentPane.setMaximumSize(new Dimension(1024, 768));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("票证人自助核验闸机查询程序");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 40));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 10, 978, 70);
		panel.add(lblNewLabel);

		JSeparator separator = new JSeparator();
		separator.setBounds(14, 90, 974, 10);
		panel.add(separator);

		JLabel rqLabel = new JLabel("统计日期：");
		rqLabel.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		rqLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		rqLabel.setBounds(184, 114, 174, 34);
		panel.add(rqLabel);

		rqTextField = new JTextField();
		rqTextField.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		rqTextField.setBounds(368, 114, 229, 34);
		panel.add(rqTextField);
		rqTextField.setColumns(10);
		
		rqTextField.setText(CalUtils.getStringDateShort2());
		
		JLabel label_yz = new JLabel("统计阈值：");
		label_yz.setHorizontalTextPosition(SwingConstants.LEFT);
		label_yz.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		label_yz.setBounds(184, 625, 174, 34);
		panel.add(label_yz);
		
		yzTextField = new JTextField();
		yzTextField.setEditable(false);
		yzTextField.setFont(new Font("微软雅黑", Font.PLAIN, 28));
		yzTextField.setColumns(10);
		yzTextField.setBounds(368, 625, 117, 34);
		panel.add(yzTextField);
		
		yzTextField.setText(String.valueOf(Config.getInstance().getFaceCheckThreshold()));

		JLabel gateLabel1 = new JLabel("1号闸机：");
		gateLabel1.setHorizontalTextPosition(SwingConstants.LEFT);
		gateLabel1.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateLabel1.setBounds(184, 241, 174, 34);
		panel.add(gateLabel1);

		JLabel gateLabel2 = new JLabel("2号闸机：");
		gateLabel2.setHorizontalTextPosition(SwingConstants.LEFT);
		gateLabel2.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateLabel2.setBounds(184, 318, 174, 34);
		panel.add(gateLabel2);

		JLabel gateLabel3 = new JLabel("3号闸机：");
		gateLabel3.setHorizontalTextPosition(SwingConstants.LEFT);
		gateLabel3.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateLabel3.setBounds(184, 400, 174, 34);
		panel.add(gateLabel3);

		JLabel gateLabel4 = new JLabel("4号闸机：");
		gateLabel4.setHorizontalTextPosition(SwingConstants.LEFT);
		gateLabel4.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateLabel4.setBounds(184, 476, 174, 34);
		panel.add(gateLabel4);

		JLabel ipaddr1 = new JLabel("");
		ipaddr1.setHorizontalTextPosition(SwingConstants.LEFT);
		ipaddr1.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		ipaddr1.setBounds(368, 241, 307, 34);
		panel.add(ipaddr1);

		JLabel ipaddr2 = new JLabel("");
		ipaddr2.setHorizontalTextPosition(SwingConstants.LEFT);
		ipaddr2.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		ipaddr2.setBounds(368, 318, 307, 34);
		panel.add(ipaddr2);

		JLabel ipaddr3 = new JLabel("");
		ipaddr3.setHorizontalTextPosition(SwingConstants.LEFT);
		ipaddr3.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		ipaddr3.setBounds(368, 400, 307, 34);
		panel.add(ipaddr3);

		JLabel ipaddr4 = new JLabel("");
		ipaddr4.setHorizontalTextPosition(SwingConstants.LEFT);
		ipaddr4.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		ipaddr4.setBounds(368, 476, 307, 34);
		panel.add(ipaddr4);

		JLabel gateCount1 = new JLabel("0");
		gateCount1.setHorizontalAlignment(SwingConstants.RIGHT);
		gateCount1.setHorizontalTextPosition(SwingConstants.LEFT);
		gateCount1.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateCount1.setBounds(711, 241, 129, 34);
		panel.add(gateCount1);

		JLabel gateCount2 = new JLabel("0");
		gateCount2.setHorizontalTextPosition(SwingConstants.LEFT);
		gateCount2.setHorizontalAlignment(SwingConstants.RIGHT);
		gateCount2.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateCount2.setBounds(711, 318, 129, 34);
		panel.add(gateCount2);

		JLabel gateCount3 = new JLabel("0");
		gateCount3.setHorizontalTextPosition(SwingConstants.LEFT);
		gateCount3.setHorizontalAlignment(SwingConstants.RIGHT);
		gateCount3.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateCount3.setBounds(711, 400, 129, 34);
		panel.add(gateCount3);

		JLabel gateCount4 = new JLabel("0");
		gateCount4.setHorizontalTextPosition(SwingConstants.LEFT);
		gateCount4.setHorizontalAlignment(SwingConstants.RIGHT);
		gateCount4.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gateCount4.setBounds(711, 476, 129, 34);
		panel.add(gateCount4);
		
		JLabel label_1 = new JLabel("合  计");
		label_1.setHorizontalTextPosition(SwingConstants.LEFT);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		label_1.setBounds(199, 540, 108, 34);
		panel.add(label_1);
		
		JLabel gatecount_sum = new JLabel("0");
		gatecount_sum.setHorizontalTextPosition(SwingConstants.LEFT);
		gatecount_sum.setHorizontalAlignment(SwingConstants.RIGHT);
		gatecount_sum.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		gatecount_sum.setBounds(711, 540, 129, 34);
		panel.add(gatecount_sum);

		JButton tjButton = new JButton("统计查询");
		tjButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tjButton.setEnabled(false);
				try {
					if (rqTextField.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(null, "请输入统计日期!");
						rqTextField.grabFocus();
					} else {
						String gateIPList = DeviceConfig.getInstance().getGateIPList();
						StringTokenizer st = new StringTokenizer(gateIPList, ",");
						int i = 0;
						int totalSum = 0;
						while (st.hasMoreTokens()) {
							i++;
							String targetIP = st.nextToken();
							pitRecordSqlDao = new PitRecordSqlDao(targetIP);
							String sqls = "select idNO from pit_face_verify where pitDate='"
									+ rqTextField.getText().trim() + "' and verifyResult>= "+Config.getInstance().getFaceCheckThreshold()+" group by idNo";
							log.info("sqls=="+sqls);
							ResultSet rs = pitRecordSqlDao.selectSQL(sqls);							
							
							int total = 0;
							while (rs.next()) {
								total++;
								totalSum++;
							}
							rs.close();
							pitRecordSqlDao.deconnSQL();
							
//							if (i == 1) {
//								ipaddr1.setText(targetIP);
//								gateCount1.setText(String.valueOf(total));
//							}
							
							switch (i) {
							case 1:
								ipaddr1.setText(targetIP);
								gateCount1.setText(String.valueOf(total));
								break;
							case 2:
								ipaddr2.setText(targetIP);
								gateCount2.setText(String.valueOf(total));
								break;
							case 3:
								ipaddr3.setText(targetIP);
								gateCount3.setText(String.valueOf(total));
								break;
							case 4:
								ipaddr4.setText(targetIP);
								gateCount4.setText(String.valueOf(total));
								break;
							default:
								ipaddr1.setText("");
								gateCount1.setText(String.valueOf(0));
								ipaddr2.setText("");
								gateCount2.setText(String.valueOf(0));
								ipaddr3.setText("");
								gateCount3.setText(String.valueOf(0));
								ipaddr4.setText("");
								gateCount4.setText(String.valueOf(0));
								break;
							}				
							
						}
						
						gatecount_sum.setText(String.valueOf(totalSum));
						
						JOptionPane.showMessageDialog(null, "统计成功!");
						tjButton.setEnabled(true);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
				}
			}
		});
		tjButton.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		tjButton.setBounds(674, 114, 162, 36);
		panel.add(tjButton);

		JLabel label = new JLabel("闸机编号");
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		label.setBounds(184, 182, 174, 34);
		panel.add(label);

		JLabel lblip = new JLabel("闸机IP地址");
		lblip.setHorizontalAlignment(SwingConstants.CENTER);
		lblip.setHorizontalTextPosition(SwingConstants.LEFT);
		lblip.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		lblip.setBounds(421, 182, 229, 34);
		panel.add(lblip);

		JLabel label_2 = new JLabel("通过人数");
		label_2.setHorizontalTextPosition(SwingConstants.CENTER);
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 30));
		label_2.setBounds(711, 182, 129, 34);
		panel.add(label_2);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(14, 221, 974, 10);
		panel.add(separator_1);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(14, 162, 974, 10);
		panel.add(separator_2);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 520, 974, 10);
		panel.add(separator_3);
		
		
		
		
	}
}
