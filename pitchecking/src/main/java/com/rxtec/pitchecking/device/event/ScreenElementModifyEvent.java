package com.rxtec.pitchecking.device.event;

public class ScreenElementModifyEvent {
	private int screenType = 0;
	private int elementType = 0;
	private int elementCmd;

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
