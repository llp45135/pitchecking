package com.rxtec.pitchecking.net.event;

import com.rxtec.pitchecking.Ticket;

public class PIVerifyEventBean {
	private String eventName = "CAM_Notify";
	private int eventDirection = 1;
	private String uuid;
	private byte[] idPhoto;

	private String personName;
	private int age;
	private int gender;
	private Ticket ticket;
	private int delaySeconds;

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public int getEventDirection() {
		return eventDirection;
	}

	public void setEventDirection(int eventDirection) {
		this.eventDirection = eventDirection;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public byte[] getIdPhoto() {
		return idPhoto;
	}

	public void setIdPhoto(byte[] idPhoto) {
		this.idPhoto = idPhoto;
	}


	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}


	 
	
}
