package com.rxtec.pitchecking.picheckingservice.realsense;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.gui.VideoPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSlider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class RSFaceFrameForTest extends JFrame {

	private JPanel contentPane;
	private VideoPanel videoPanel;
	private RSFaceDetectionService rsft;
	private JButton btnHdrOff;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RSFaceFrameForTest frame = new RSFaceFrameForTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	RealsenseDeviceProperties props = new RealsenseDeviceProperties();
	private JPanel panel;
	private JSlider slider_Exposure;
	private JLabel label;
	private JLabel label_1;
	private JSlider slider_Brightness;
	private JLabel label_2;
	private JSlider slider_gamma;
	private JLabel label_3;
	private JSlider slider_contrast;
	private JSlider slider_Gain;
	private JLabel label_4;
	private JSlider slider_Saturation;
	private JSlider slider_HUE;
	/**
	 * Create the frame.
	 */
	public RSFaceFrameForTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1342, 870);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		videoPanel = new VideoPanel(Config.FrameWidth, Config.FrameHeigh);
		videoPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		videoPanel.setBounds(384, 112, 940, 566);
		contentPane.setLayout(null);
		panel = new JPanel();
		panel.setBounds(15, 15, 354, 823);
		contentPane.add(panel);
		contentPane.add(videoPanel);
		panel.setLayout(null);
		
		btnHdrOff = new JButton("HDR OFF");
		btnHdrOff.setBounds(26, 5, 95, 29);
		panel.add(btnHdrOff);
		
		JButton btnNewButton = new JButton("HDR ON");
		btnNewButton.setBounds(136, 5, 87, 29);
		panel.add(btnNewButton);
		
		slider_Exposure = new JSlider();
		slider_Exposure.setPaintTicks(true);
		slider_Exposure.setMajorTickSpacing(8);
		slider_Exposure.setMinorTickSpacing(1);
		slider_Exposure.setSnapToTicks(true);
		slider_Exposure.setPaintLabels(true);
		
		slider_Exposure.setBounds(136, 116, 208, 41);
		slider_Exposure.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setColorExposure(slider_Exposure.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});

		slider_Exposure.setMaximum(0);
		slider_Exposure.setMinimum(-8);
		slider_Exposure.setValue(-4);

		
		label = new JLabel("曝光值");
		label.setFont(new Font("宋体", Font.PLAIN, 14));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setBounds(26, 119, 101, 21);
		panel.add(label);
		panel.add(slider_Exposure);
		
		label_1 = new JLabel("亮度值");
		label_1.setFont(new Font("宋体", Font.PLAIN, 14));
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setBounds(26, 182, 101, 21);
		panel.add(label_1);
		
		slider_Brightness = new JSlider();
		slider_Brightness.setPaintTicks(true);
		slider_Brightness.setMajorTickSpacing(128);
		slider_Brightness.setMinorTickSpacing(8);
		slider_Brightness.setSnapToTicks(true);
		slider_Brightness.setPaintLabels(true);
		slider_Brightness.setBounds(136, 188, 208, 41);
		slider_Brightness.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setColorBrightness(slider_Brightness.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});

		slider_Brightness.setValue(0);
		slider_Brightness.setMinimum(-64);
		slider_Brightness.setMaximum(64);
		panel.add(slider_Brightness);
		
		label_2 = new JLabel("伽马");
		label_2.setFont(new Font("宋体", Font.PLAIN, 14));
		label_2.setHorizontalAlignment(SwingConstants.LEFT);
		label_2.setBounds(26, 245, 95, 21);
		panel.add(label_2);
		
		slider_gamma = new JSlider();
		slider_gamma.setPaintTicks(true);
		slider_gamma.setMajorTickSpacing(499);
		slider_gamma.setMinorTickSpacing(50);
		slider_gamma.setSnapToTicks(true);
		slider_gamma.setPaintLabels(true);
		slider_gamma.setBounds(136, 239, 208, 41);
		slider_gamma.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setGamma(slider_gamma.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});

		slider_gamma.setValue(60);
		slider_gamma.setMinimum(1);
		slider_gamma.setMaximum(500);
		panel.add(slider_gamma);
		
		label_3 = new JLabel("对比度");
		label_3.setFont(new Font("宋体", Font.PLAIN, 14));
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setBounds(26, 307, 95, 21);
		panel.add(label_3);
		
		slider_contrast = new JSlider();
		slider_contrast.setPaintTicks(true);
		slider_contrast.setMajorTickSpacing(128);
		slider_contrast.setMinorTickSpacing(8);
		slider_contrast.setSnapToTicks(true);
		slider_contrast.setPaintLabels(true);
		slider_contrast.setBounds(136, 301, 208, 41);
		slider_contrast.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setContrast(slider_contrast.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_contrast.setValue(64);
		slider_contrast.setMinimum(0);
		slider_contrast.setMaximum(128);
		panel.add(slider_contrast);
		
		label_4 = new JLabel("图像增益");
		label_4.setFont(new Font("宋体", Font.PLAIN, 14));
		label_4.setHorizontalAlignment(SwingConstants.LEFT);
		label_4.setBounds(26, 363, 95, 21);
		panel.add(label_4);
		
		slider_Gain = new JSlider();
		slider_Gain.setPaintTicks(true);
		slider_Gain.setMajorTickSpacing(128);
		slider_Gain.setMinorTickSpacing(8);
		slider_Gain.setSnapToTicks(true);
		slider_Gain.setPaintLabels(true);
		slider_Gain.setBounds(136, 363, 208, 46);
		slider_Gain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setGain(slider_Gain.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);

			
			}
		});
		slider_Gain.setValue(64);
		slider_Gain.setMinimum(0);
		slider_Gain.setMaximum(128);
		panel.add(slider_Gain);
		
		slider_HUE = new JSlider();
		slider_HUE.setPaintTicks(true);
		slider_HUE.setMajorTickSpacing(360);
		slider_HUE.setMinorTickSpacing(36);
		slider_HUE.setSnapToTicks(true);
		slider_HUE.setPaintLabels(true);
		slider_HUE.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setHue(slider_HUE.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);

			}
		});
		slider_HUE.setBounds(137, 419, 207, 41);
		slider_HUE.setValue(0);
		slider_HUE.setMinimum(-180);
		slider_HUE.setMaximum(180);
		panel.add(slider_HUE);
		
		slider_Saturation = new JSlider();
		slider_Saturation.setPaintTicks(true);
		slider_Saturation.setMajorTickSpacing(100);
		slider_Saturation.setMinorTickSpacing(10);
		slider_Saturation.setSnapToTicks(true);
		slider_Saturation.setPaintLabels(true);
		slider_Saturation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setSaturation(slider_Saturation.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_Saturation.setBounds(137, 482, 207, 41);
		slider_Saturation.setValue(64);
		slider_Saturation.setMinimum(0);
		slider_Saturation.setMaximum(100);
		panel.add(slider_Saturation);
		
		JLabel lblHue = new JLabel("色相");
		lblHue.setFont(new Font("宋体", Font.PLAIN, 14));
		lblHue.setHorizontalAlignment(SwingConstants.LEFT);
		lblHue.setBounds(26, 428, 95, 21);
		panel.add(lblHue);
		
		JLabel label_5 = new JLabel("色彩饱和度");
		label_5.setFont(new Font("宋体", Font.PLAIN, 14));
		label_5.setHorizontalAlignment(SwingConstants.LEFT);
		label_5.setBounds(26, 490, 95, 21);
		panel.add(label_5);
		
		JLabel label_6 = new JLabel("清晰度");
		label_6.setFont(new Font("宋体", Font.PLAIN, 14));
		label_6.setHorizontalAlignment(SwingConstants.LEFT);
		label_6.setBounds(26, 540, 95, 21);
		panel.add(label_6);
		
		JSlider slider_Sharpness = new JSlider();
		slider_Sharpness.setPaintTicks(true);
		slider_Sharpness.setMajorTickSpacing(100);
		slider_Sharpness.setMinorTickSpacing(10);
		slider_Sharpness.setSnapToTicks(true);
		slider_Sharpness.setPaintLabels(true);
		slider_Sharpness.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setSharpness(slider_Sharpness.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_Sharpness.setValue(50);
		slider_Sharpness.setMinimum(0);
		slider_Sharpness.setMaximum(100);
		slider_Sharpness.setBounds(137, 540, 207, 44);
		panel.add(slider_Sharpness);
		
		JLabel label_7 = new JLabel("白平衡");
		label_7.setFont(new Font("宋体", Font.PLAIN, 14));
		label_7.setHorizontalAlignment(SwingConstants.LEFT);
		label_7.setBounds(26, 594, 95, 21);
		panel.add(label_7);
		
		JSlider slider_WhitenBalance = new JSlider();
		slider_WhitenBalance.setPaintTicks(true);
		slider_WhitenBalance.setMajorTickSpacing(2800);
		slider_WhitenBalance.setMinorTickSpacing(400);
		slider_WhitenBalance.setSnapToTicks(true);
		slider_WhitenBalance.setPaintLabels(true);
		slider_WhitenBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setWhitebalance(slider_WhitenBalance.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_WhitenBalance.setValue(4600);
		slider_WhitenBalance.setMinimum(2800);
		slider_WhitenBalance.setMaximum(5600);
		slider_WhitenBalance.setBounds(137, 594, 207, 41);
		panel.add(slider_WhitenBalance);
		
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				props.setSR300_HDR(1.0f);
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		btnHdrOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				props.setSR300_HDR(0f);
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
	}
	
	public JPanel getVideoPanel() {
		return videoPanel;
	}

	public RSFaceDetectionService getRsft() {
		return rsft;
	}

	public void setRsft(RSFaceDetectionService rsft) {
		this.rsft = rsft;
	}
}
