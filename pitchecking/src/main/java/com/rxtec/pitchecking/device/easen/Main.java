/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rxtec.pitchecking.device.easen;

import com.rxtec.pitchecking.Config;
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author demid
 */
public class Main {

	public enum SDK_ERROR {
		SDK_NO_ERROR, SDK_ACTIVATION_SERIAL_UNKNOWN, SDK_ACTIVATION_EXPIRED,

		SDK_NOT_INITIALIZED, SDK_ALREADY_INITIALIZED, SDK_NO_RES_FILE, SDK_BAD_THRESHOLD_XML, SDK_NOT_FACE_DETECTED,

		SDK_NONE_IDCARD_SET, SDK_BAD_PARAMETER, SDK_NOT_ID_PHOTO, SDK_DONGLE_ERROR
	}

	public interface SDKMethod extends Library {

		SDKMethod INSTANCE = (SDKMethod) Native.loadLibrary("idVerificationSDK.dll", SDKMethod.class);

		int getCurrentHWID(byte[] hwidBytes, int size);

		int setActivation(byte[] licenseBytes);

		int initializeSDK(byte[] pathBytes);

		int finalizeSDK();

		int setIDCardPhoto(byte[] idImgPtr, int width, int height);

		int match(byte[] idImgPtr, int width, int height, float[] matchinfgScore, int[] yesNo, float[] matchingTime,
				int[] faceX, int[] faceY, int[] faceWidth, int[] faceHeight);

		int detectFace(byte[] imgData, int width, int height, int[] faceX, int[] faceY, int[] faceWidth,
				int[] faceHeight);

		public final int SDK_NO_ERROR = 0;
		public final int SDK_ACTIVATION_SERIAL_UNKNOW = 1;
		public final int SDK_ACTIVATION_EXPIRED = 2;

		public final int SDK_NOT_INITIALIZED = 3;
		public final int SDK_ALREADY_INITIALIZED = 4;
		public final int SDK_NO_RES_FILE = 5;
		public final int SDK_BAD_THRESHOLD_XML = 6;
		public final int SDK_NOT_FACE_DETECTED = 7;

		public final int SDK_NONE_IDCARD_SET = 8;
		public final int SDK_BAD_PARAMETER = 9;
		public final int SDK_NOT_ID_PHOTO = 10;
		public final int SDK_DONGLE_ERROR = 11;

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting
		// code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
		 * html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(ActivationDlg.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(ActivationDlg.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(ActivationDlg.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ActivationDlg.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		// </editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				System.out.println("config.path=="+Config.getInstance().getEasenConfigPath());
				int ret;
				byte[] licenseBytes = null;
				try {
					File file = new File("D:/pitchecking/config/license.txt");
					FileInputStream fis = new FileInputStream(file);
					licenseBytes = new byte[(int) file.length() + 1];
					Arrays.fill(licenseBytes, (byte) 0);
					fis.read(licenseBytes);
					fis.close();
				} catch (Exception e) {

				}

				ret = SDKMethod.INSTANCE.setActivation(licenseBytes);
				System.out.println("init ret = " + ret);
				if (ret != SDKMethod.SDK_NO_ERROR) {
					ActivationDlg dialog = new ActivationDlg(new javax.swing.JFrame(), true);
					dialog.setVisible(true);
					int result = dialog.getResult();
					if (result == 0) {
						System.exit(0);
						return;
					}
				}

				try {
					// String strPath = "D:\\_copyToBin";
					String strPath = Config.getInstance().getEasenConfigPath();
					byte[] path = strPath.getBytes("UTF-16LE");
					byte[] pathBytes = new byte[path.length + 2];
					Arrays.fill(pathBytes, (byte) 0);
					System.arraycopy(path, 0, pathBytes, 0, path.length);

					ret = SDKMethod.INSTANCE.initializeSDK(pathBytes);
					System.err.println("init ret = " + ret);
					if (ret != SDKMethod.SDK_NO_ERROR) {
						JOptionPane.showMessageDialog(null, "Init SDK Failed!", "Warning", JOptionPane.PLAIN_MESSAGE);
						return;
					}

					MainForm mainForm = new MainForm();
					mainForm.setVisible(true);

				} catch (Exception e) {

				}
			}
		});
	}
}
