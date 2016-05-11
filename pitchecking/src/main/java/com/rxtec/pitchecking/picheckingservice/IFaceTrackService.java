package com.rxtec.pitchecking.picheckingservice;

import javax.swing.JPanel;

import com.rxtec.pitchecking.gui.VideoPanel;

public interface IFaceTrackService {
	public void beginCheckingFace(IDCard idCard);
	/**
	 * 结束人证对比
	 */
	public void stopCheckingFace();
	
	public void beginVideoCaptureAndTracking();
	
	public void setVideoPanel(JPanel vp);

}
