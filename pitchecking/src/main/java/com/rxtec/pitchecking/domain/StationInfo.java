package com.rxtec.pitchecking.domain;

public class StationInfo {
	private String stationTelecode;
    private String stationName;
    private String belongLineCode;
    private String belongLineName;
    private String startDate;
    private int distance;

    public StationInfo() {
    }

	public String getStationTelecode() {
		return stationTelecode;
	}

	public void setStationTelecode(String stationTelecode) {
		this.stationTelecode = stationTelecode;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getBelongLineCode() {
		return belongLineCode;
	}

	public void setBelongLineCode(String belongLineCode) {
		this.belongLineCode = belongLineCode;
	}

	public String getBelongLineName() {
		return belongLineName;
	}

	public void setBelongLineName(String belongLineName) {
		this.belongLineName = belongLineName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
