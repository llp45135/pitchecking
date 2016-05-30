package com.rxtec.pitchecking.picheckingservice;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class FaceCheckTestWindow {

	private JFrame frame;
	static JPanel panel;
	static IFaceTrackService faceTrackService = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceCheckTestWindow window = new FaceCheckTestWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		faceTrackService = FaceDetectionService.getInstance();

		FaceCheckingService.getInstance().beginFaceCheckerTask();
		faceTrackService.setVideoPanel(panel);
		faceTrackService.beginVideoCaptureAndTracking();

	}

	/**
	 * Create the application.
	 */
	public FaceCheckTestWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 664, 525);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
	}

}
