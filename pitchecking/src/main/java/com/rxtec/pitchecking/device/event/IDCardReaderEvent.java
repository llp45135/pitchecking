package com.rxtec.pitchecking.device.event;

import java.awt.image.BufferedImage;

import com.rxtec.pitchecking.picheckingservice.IDCard;

public class IDCardReaderEvent implements IDeviceEvent{

	
	/**
	 * 1 寻卡成功
	 * 2读卡成功
	 */
	private int eventType = -1;
	
	private IDCard card;
	
	
	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return card;
	}

	@Override
	public void setEventType(int arg) {
		this.eventType = arg;
		
	}

	
	public IDCardReaderEvent (int eventType,String idNo,BufferedImage img){
		card = new IDCard();
		card.setCardImage(img);
		card.setIdNo(idNo);
	}

	public IDCardReaderEvent(int eventType){
		this.eventType = eventType;
	}

}
