package com.rxtec.pitchecking.net.event.wharf;

import org.json.JSONArray;

public class ShipTicket {
	private String TPID;
	private String SHIP_NAME;
	private String ORDERID;
	private String SHT_NAME;
	private JSONArray listRealName;
	private String ISOPEN;
	private String PORT_DOCKS;
	private String SHIPPINGDAY;
	private String SHIPPINGTIME;
	private String SHR_NAME;
	private String STR_NAME;

	public String getISOPEN() {
		return ISOPEN;
	}

	public void setISOPEN(String iSOPEN) {
		ISOPEN = iSOPEN;
	}

	public String getPORT_DOCKS() {
		return PORT_DOCKS;
	}

	public void setPORT_DOCKS(String pORT_DOCKS) {
		PORT_DOCKS = pORT_DOCKS;
	}

	public String getSHIPPINGDAY() {
		return SHIPPINGDAY;
	}

	public void setSHIPPINGDAY(String sHIPPINGDAY) {
		SHIPPINGDAY = sHIPPINGDAY;
	}

	public String getSHIPPINGTIME() {
		return SHIPPINGTIME;
	}

	public void setSHIPPINGTIME(String sHIPPINGTIME) {
		SHIPPINGTIME = sHIPPINGTIME;
	}

	public String getSHR_NAME() {
		return SHR_NAME;
	}

	public void setSHR_NAME(String sHR_NAME) {
		SHR_NAME = sHR_NAME;
	}

	public String getSTR_NAME() {
		return STR_NAME;
	}

	public void setSTR_NAME(String sTR_NAME) {
		STR_NAME = sTR_NAME;
	}

	public ShipTicket() {
		// TODO Auto-generated constructor stub
	}

	public String getTPID() {
		return TPID;
	}

	public void setTPID(String tPID) {
		TPID = tPID;
	}

	public String getSHIP_NAME() {
		return SHIP_NAME;
	}

	public void setSHIP_NAME(String sHIP_NAME) {
		SHIP_NAME = sHIP_NAME;
	}

	public String getORDERID() {
		return ORDERID;
	}

	public void setORDERID(String oRDERID) {
		ORDERID = oRDERID;
	}

	public String getSHT_NAME() {
		return SHT_NAME;
	}

	public void setSHT_NAME(String sHT_NAME) {
		SHT_NAME = sHT_NAME;
	}

	public JSONArray getListRealName() {
		return listRealName;
	}

	public void setListRealName(JSONArray listRealName) {
		this.listRealName = listRealName;
	}

}
