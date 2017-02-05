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
		videoPanel.setBounds(384, 15, 940, 823);
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
		slider_Exposure.setBounds(137, 114, 200, 26);
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
		label.setBounds(26, 119, 54, 21);
		panel.add(label);
		panel.add(slider_Exposure);
		
		label_1 = new JLabel("亮度值");
		label_1.setBounds(26, 182, 54, 21);
		panel.add(label_1);
		
		slider_Brightness = new JSlider();
		slider_Brightness.setBounds(137, 177, 200, 26);
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
		
		label_2 = new JLabel("灰度值");
		label_2.setBounds(26, 245, 54, 21);
		panel.add(label_2);
		
		slider_gamma = new JSlider();
		slider_gamma.setBounds(136, 245, 200, 21);
		slider_gamma.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setGamma(slider_gamma.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});

		slider_gamma.setValue(0);
		slider_gamma.setMinimum(-64);
		slider_gamma.setMaximum(64);
		panel.add(slider_gamma);
		
		label_3 = new JLabel("对比度");
		label_3.setBounds(26, 307, 54, 21);
		panel.add(label_3);
		
		slider_contrast = new JSlider();
		slider_contrast.setBounds(137, 302, 200, 26);
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
		label_4.setBounds(15, 363, 72, 21);
		panel.add(label_4);
		
		slider_Gain = new JSlider();
		slider_Gain.setBounds(136, 358, 200, 26);
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
		slider_HUE.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setHue(slider_HUE.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);

			}
		});
		slider_HUE.setBounds(137, 419, 200, 26);
		slider_HUE.setValue(0);
		slider_HUE.setMinimum(-180);
		slider_HUE.setMaximum(180);
		panel.add(slider_HUE);
		
		slider_Saturation = new JSlider();
		slider_Saturation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setSaturation(slider_Saturation.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_Saturation.setBounds(137, 482, 200, 26);
		slider_Saturation.setValue(64);
		slider_Saturation.setMinimum(0);
		slider_Saturation.setMaximum(100);
		panel.add(slider_Saturation);
		
		JLabel lblHue = new JLabel("色相");
		lblHue.setHorizontalAlignment(SwingConstants.CENTER);
		lblHue.setBounds(15, 419, 72, 21);
		panel.add(lblHue);
		
		JLabel label_5 = new JLabel("色彩饱和度");
		label_5.setHorizontalAlignment(SwingConstants.CENTER);
		label_5.setBounds(15, 482, 106, 21);
		panel.add(label_5);
		
		JLabel label_6 = new JLabel("清晰度");
		label_6.setHorizontalAlignment(SwingConstants.CENTER);
		label_6.setBounds(15, 540, 106, 21);
		panel.add(label_6);
		
		JSlider slider_Sharpness = new JSlider();
		slider_Sharpness.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setSharpness(slider_Sharpness.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_Sharpness.setValue(50);
		slider_Sharpness.setMinimum(0);
		slider_Sharpness.setMaximum(100);
		slider_Sharpness.setBounds(137, 540, 200, 26);
		panel.add(slider_Sharpness);
		
		JLabel label_7 = new JLabel("白平衡");
		label_7.setHorizontalAlignment(SwingConstants.CENTER);
		label_7.setBounds(15, 594, 106, 21);
		panel.add(label_7);
		
		JSlider slider_WhitenBalance = new JSlider();
		slider_WhitenBalance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				props.setWhitebalance(slider_WhitenBalance.getValue());
				if(rsft != null) rsft.setDeviceProperties(props);
			}
		});
		slider_WhitenBalance.setValue(4600);
		slider_WhitenBalance.setMinimum(2800);
		slider_WhitenBalance.setMaximum(5600);
		slider_WhitenBalance.setBounds(137, 594, 200, 26);
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
