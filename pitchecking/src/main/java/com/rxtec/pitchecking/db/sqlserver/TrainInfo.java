package com.rxtec.pitchecking.db.sqlserver;

public class TrainInfo {
	
	private String station;
	private String trainCode;
	private String tdStartTime;
	private String sjStartTime;
	private String state;   //　開行狀態

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getTrainCode() {
		return trainCode;
	}

	public void setTrainCode(String trainCode) {
		this.trainCode = trainCode;
	}

	public String getTdStartTime() {
		return tdStartTime;
	}

	public void setTdStartTime(String tdStartTime) {
		this.tdStartTime = tdStartTime;
	}

	public String getSjStartTime() {
		return sjStartTime;
	}

	public void setSjStartTime(String sjStartTime) {
		this.sjStartTime = sjStartTime;
	}

	public TrainInfo() {
		// TODO Auto-generated constructor stub
	}

}
