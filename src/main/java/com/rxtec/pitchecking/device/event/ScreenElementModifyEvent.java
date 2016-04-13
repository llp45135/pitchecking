package com.rxtec.pitchecking.device.event;

import com.rxtec.pitchecking.picheckingservice.FaceData;
import com.rxtec.pitchecking.picheckingservice.IDCard;

public class ScreenElementModifyEvent {
	private int screenType = 0;
	private int elementType = 0;
	private int elementCmd;
	private IDCard idCard;
	
	private FaceData faceData;

	public FaceData getFaceData() {
		return faceData;
	}

	public void setFaceData(FaceData faceData) {
		this.faceData = faceData;
	}

	public IDCard getIdCard() {
		return idCard;
	}

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	public int getScreenType() {
		return screenType;
	}

	public void setScreenType(int screenType) {
		this.screenType = screenType;
	}

	public ScreenElementModifyEvent(int screenType, int elementType, int elementCmd) {
		this.screenType = screenType;
		this.elementType = elementType;
	}

	public int getElementType() {
		return elementType;
	}

	public void setElementType(int elementType) {
		this.elementType = elementType;
	}

	public int getElementCmd() {
		return elementCmd;
	}

	public void setElementCmd(int elementCmd) {
		this.elementCmd = elementCmd;
	}

}
