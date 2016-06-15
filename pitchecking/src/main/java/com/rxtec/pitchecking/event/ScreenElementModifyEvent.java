package com.rxtec.pitchecking.event;

import com.rxtec.pitchecking.picheckingservice.PICData;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;

public class ScreenElementModifyEvent {
	private int screenType = 0;
	private int elementType = 0;
	private int elementCmd;
	private IDCard idCard;
	
	private PICData faceData;
	private Ticket ticket;

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public PICData getFaceData() {
		return faceData;
	}

	public void setFaceData(PICData faceData) {
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
	
	public ScreenElementModifyEvent(int screenType, int elementCmd, PICData fd) {
		this.screenType = screenType;
		this.elementCmd = elementCmd;
		this.faceData = fd;
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
