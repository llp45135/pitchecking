package com.rxtec.pitchecking;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.HighFaceCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;

/**
 * 人脸识别显示屏幕
 * 
 * @author lenovo
 *
 */
public class HighFaceTrackingScreen {

	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static HighFaceTrackingScreen _instance = new HighFaceTrackingScreen();

	HighFaceCheckFrame faceFrame;

	public HighFaceCheckFrame getFaceFrame() {
		return faceFrame;
	}

	public void setFaceFrame(HighFaceCheckFrame faceFrame) {
		this.faceFrame = faceFrame;
	}

	private HighFaceTrackingScreen() {

	}

	public void initUI(int screenNo) throws Exception {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice gd = gs[screenNo];
		// log.debug("GraphicsDevice=="+gd);
		if (gd != null) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			faceFrame.setVisible(true);
			faceFrame.setLocation(xOff, yOff);
		}
		// log.debug("当前的人脸检测屏位置：face.x=="+faceFrame.getBounds().x+",face.y=="+faceFrame.getBounds().y);
	}

	public VideoPanel getVideoPanel() {
		return faceFrame.getVideoPanel();
	}

	public static HighFaceTrackingScreen getInstance() {
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
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceDisplayFromTK.getValue()) {
			faceFrame.showFaceDisplayFromTK();
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
