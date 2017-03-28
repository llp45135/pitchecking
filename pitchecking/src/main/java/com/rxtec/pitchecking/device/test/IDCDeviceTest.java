package com.rxtec.pitchecking.device.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.device.TKIDCDevice;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IDCDeviceTest extends JFrame {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private JPanel contentPane;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel sexLabel;
	private JTextField sexTextField;
	private JLabel nationLabel;
	private JTextField nationTextField;
	private JLabel birthLabel;
	private JTextField birthTextField;
	private JLabel addrLabel;
	private JTextField addrTextField;
	private JLabel idNoLabel;
	private JTextField idNoTextField;
	private JLabel signLabel;
	private JTextField signTextField;
	private JLabel beginLabel;
	private JTextField beginTextField;
	private JLabel endLabel;
	private JTextField endTextField;
	private JLabel idimgLabel;
	private JButton button;
	private JButton btnNewButton;

	private TKIDCDevice tkIDCDevice = TKIDCDevice.getInstance();
	private JLabel label;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IDCDeviceTest frame = new IDCDeviceTest();
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
	public IDCDeviceTest() {
		setResizable(false);
		setMinimumSize(new Dimension(640, 480));
		setMaximumSize(new Dimension(640, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 530);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel titleLabel = new JLabel("二代身份证读卡器测试程序");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		titleLabel.setBounds(10, 5, 774, 34);
		contentPane.add(titleLabel);

		nameLabel = new JLabel("姓名：");
		nameLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		nameLabel.setBounds(30, 49, 88, 27);
		contentPane.add(nameLabel);

		nameTextField = new JTextField();
		nameTextField.setBounds(140, 49, 428, 27);
		contentPane.add(nameTextField);
		nameTextField.setColumns(10);

		sexLabel = new JLabel("性别：");
		sexLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		sexLabel.setBounds(30, 86, 88, 27);
		contentPane.add(sexLabel);

		sexTextField = new JTextField();
		sexTextField.setColumns(10);
		sexTextField.setBounds(140, 86, 428, 27);
		contentPane.add(sexTextField);

		nationLabel = new JLabel("民族：");
		nationLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		nationLabel.setBounds(30, 123, 88, 27);
		contentPane.add(nationLabel);

		nationTextField = new JTextField();
		nationTextField.setColumns(10);
		nationTextField.setBounds(140, 123, 428, 27);
		contentPane.add(nationTextField);

		birthLabel = new JLabel("出生年月：");
		birthLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		birthLabel.setBounds(30, 160, 88, 27);
		contentPane.add(birthLabel);

		birthTextField = new JTextField();
		birthTextField.setColumns(10);
		birthTextField.setBounds(140, 160, 428, 27);
		contentPane.add(birthTextField);

		addrLabel = new JLabel("地址：");
		addrLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		addrLabel.setBounds(30, 197, 88, 27);
		contentPane.add(addrLabel);

		addrTextField = new JTextField();
		addrTextField.setColumns(10);
		addrTextField.setBounds(140, 197, 428, 27);
		contentPane.add(addrTextField);

		idNoLabel = new JLabel("身份证号：");
		idNoLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		idNoLabel.setBounds(30, 234, 88, 27);
		contentPane.add(idNoLabel);

		idNoTextField = new JTextField();
		idNoTextField.setColumns(10);
		idNoTextField.setBounds(140, 234, 428, 27);
		contentPane.add(idNoTextField);

		signLabel = new JLabel("签发机构：");
		signLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		signLabel.setBounds(30, 271, 88, 27);
		contentPane.add(signLabel);

		signTextField = new JTextField();
		signTextField.setColumns(10);
		signTextField.setBounds(140, 271, 428, 27);
		contentPane.add(signTextField);

		beginLabel = new JLabel("有效期始：");
		beginLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		beginLabel.setBounds(30, 308, 88, 27);
		contentPane.add(beginLabel);

		beginTextField = new JTextField();
		beginTextField.setColumns(10);
		beginTextField.setBounds(140, 308, 428, 27);
		contentPane.add(beginTextField);

		endLabel = new JLabel("有效期止：");
		endLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		endLabel.setBounds(30, 345, 88, 27);
		contentPane.add(endLabel);

		endTextField = new JTextField();
		endTextField.setColumns(10);
		endTextField.setBounds(140, 345, 428, 27);
		contentPane.add(endTextField);

		idimgLabel = new JLabel("身份证照片");
		idimgLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		idimgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		idimgLabel.setBounds(616, 49, 102, 126);
		contentPane.add(idimgLabel);

		btnNewButton = new JButton("读身份证");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label.setText("");
				nameTextField.setText("");
				sexTextField.setText("");
				nationTextField.setText("");
				birthTextField.setText("");
				addrTextField.setText("");
				idNoTextField.setText("");
				signTextField.setText("");
				beginTextField.setText("");
				endTextField.setText("");
				idimgLabel.setIcon(null);

				String findval = tkIDCDevice.IDC_FetchCard(1000);
				if (findval.equals("0")) {
					IDCard idCard = tkIDCDevice.IDC_ReadIDCardInfo(2);
					if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null
							&& idCard.getCardImageBytes() != null) {
						label.setText("读身份证成功!");
						nameTextField.setText(idCard.getPersonName());
						sexTextField.setText(idCard.getGenderCH());
						nationTextField.setText(idCard.getIDNationCH());
						birthTextField.setText(idCard.getIDBirth());
						addrTextField.setText(idCard.getIDDwelling());
						idNoTextField.setText(idCard.getIdNo());
						signTextField.setText(idCard.getIDIssue());
						beginTextField.setText(idCard.getIDEfficb());
						endTextField.setText(idCard.getIDEffice());
						idimgLabel.setIcon(new ImageIcon(idCard.getCardImage()));
					}
				} else {
					log.debug("findval==" + findval);
					label.setText("没有寻到二代证!FetchCard code=" + findval);
				}
			}
		});
		btnNewButton.setFont(new Font("宋体", Font.PLAIN, 14));
		btnNewButton.setBounds(213, 408, 135, 34);
		contentPane.add(btnNewButton);

		button = new JButton("退  出");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});
		button.setFont(new Font("宋体", Font.PLAIN, 14));
		button.setBounds(433, 408, 135, 34);
		contentPane.add(button);

		label = new JLabel("二代证读卡器初始化成功!");
		label.setBorder(new LineBorder(Color.BLUE, 2));
		label.setForeground(Color.RED);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		label.setBounds(10, 457, 774, 34);
		contentPane.add(label);

		log.debug("getIdcInitRet==" + tkIDCDevice.getIdcInitRet());
		if (tkIDCDevice.getIdcInitRet() == 0) {
			label.setText("二代证读卡器初始化成功!");
		} else {
			label.setText("二代证读卡器初始化失败!");
			btnNewButton.setEnabled(false);
		}
	}
}
