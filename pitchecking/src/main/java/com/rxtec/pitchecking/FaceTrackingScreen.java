package com.rxtec.pitchecking;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;

/**
 * 人脸识别显示屏幕
 * @author lenovo
 *
 */
public class FaceTrackingScreen {

	private Logger log = LoggerFactory.getLogger("FaceTrackingScreen");
	private static FaceTrackingScreen _instance = new FaceTrackingScreen();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();

	FaceCheckFrame faceFrame = new FaceCheckFrame();

	private FaceTrackingScreen() {
		initUI();
	}

	public void initUI() {

		GraphicsDevice gd = gs[DeviceConfig.getInstance().getFaceScreen()];
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		if(gd != null){
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			faceFrame.setVisible(true);
			faceFrame.setLocation(xOff, yOff);
			
	
		}
		
//		 gs[DeviceConfig.getInstance().getFaceScreen()].setFullScreenWindow(faceFrame);
//		faceFrame.setUndecorated(true);
//		faceFrame.setVisible(true);
//		faceFrame.setAlwaysOnTop(true);
	}

	public VideoPanel getVideoPanel() {
		return faceFrame.getVideoPanel();
	}

	public static FaceTrackingScreen getInstance() {
		return _instance;
	}


	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			processEventByType(e);
		}
	}



	/**
	 * 人脸识别屏幕事件处理
	 * 
	 * @param e
	 */
	private void processEventByType(ScreenElementModifyEvent e) {
		if (e.getElementCmd() == ScreenCmdEnum.ShowBeginCheckFaceContent.getValue()) {
			faceFrame.showBeginCheckFaceContent();

		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckPass.getValue()) {
			faceFrame.showFaceCheckPassContent();

		} else if (e.getElementCmd() == ScreenCmdEnum.showFaceDefaultContent.getValue()) {
			faceFrame.showDefaultContent();

		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckFailed.getValue()) {
			faceFrame.showCheckFailedContent();
		}
	}

	public void repainFaceFrame() {
		// faceFrame.showIDCardImage(null);
		faceFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
