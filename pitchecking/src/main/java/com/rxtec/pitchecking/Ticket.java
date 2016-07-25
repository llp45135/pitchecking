package com.rxtec.pitchecking;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Ticket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6822588667513026022L;
	private String ticketNo = "";
	private String fromStationCode = "";
	private String endStationCode = "";
	private String changeStationCode = "";
	private String trainCode = "";
	private String coachNo = "";
	private String seatCode = "";
	private String ticketType = "";
	private String seatNo = "";
	private int ticketPrice ;
	private String trainDate = "";
	private String changeFlag = "";// 中转票标记
	private String ticketSourceCenter = "";
	private String bzsFlag = "";// 本站售标记
	private String saleOfficeNo = "";// 售票处
	private String saleWindowNo = "";
	private String saleDate = "";
	private String cardType = "";
	private String cardNo = "";
	private String passengerName = "";
	private String specialStr = "";
	private String inGateNo = "";			//进闸机编号
	private String fromStationName = "";
	private String toStationName = "";

	public String getFromStationName() {
		return fromStationName;
	}

	public void setFromStationName(String fromStationName) {
		this.fromStationName = fromStationName;
	}

	public String getToStationName() {
		return toStationName;
	}

	public void setToStationName(String toStationName) {
		this.toStationName = toStationName;
	}

	public String getInGateNo() {
		return inGateNo;
	}

	public void setInGateNo(String inGateNo) {
		this.inGateNo = inGateNo;
	}

	public String getTicketNo() {
		return ticketNo;
	}

	public void setTicketNo(String ticketNo) {
		this.ticketNo = ticketNo;
	}

	public String getFromStationCode() {
		return fromStationCode;
	}

	public void setFromStationCode(String fromStationCode) {
		this.fromStationCode = fromStationCode;
	}

	public String getEndStationCode() {
		return endStationCode;
	}

	public void setEndStationCode(String endStationCode) {
		this.endStationCode = endStationCode;
	}

	public String getChangeStationCode() {
		return changeStationCode;
	}

	public void setChangeStationCode(String changeStationCode) {
		this.changeStationCode = changeStationCode;
	}

	public String getTrainCode() {
		return trainCode;
	}

	public void setTrainCode(String trainCode) {
		this.trainCode = trainCode;
	}

	public String getCoachNo() {
		return coachNo;
	}

	public void setCoachNo(String coachNo) {
		this.coachNo = coachNo;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getTicketType() {
		return ticketType;
	}

	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}

	public String getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(String seatNo) {
		this.seatNo = seatNo;
	}

	public int getTicketPrice() {
		return ticketPrice;
	}

	public void setTicketPrice(int ticketPrice) {
		this.ticketPrice = ticketPrice;
	}

	public String getTrainDate() {
		return trainDate;
	}

	public void setTrainDate(String trainDate) {
		this.trainDate = trainDate;
	}

	public String getChangeFlag() {
		return changeFlag;
	}

	public void setChangeFlag(String changeFlag) {
		this.changeFlag = changeFlag;
	}

	public String getTicketSourceCenter() {
		return ticketSourceCenter;
	}

	public void setTicketSourceCenter(String ticketSourceCenter) {
		this.ticketSourceCenter = ticketSourceCenter;
	}

	public String getBzsFlag() {
		return bzsFlag;
	}

	public void setBzsFlag(String bzsFlag) {
		this.bzsFlag = bzsFlag;
	}

	public String getSaleOfficeNo() {
		return saleOfficeNo;
	}

	public void setSaleOfficeNo(String saleOfficeNo) {
		this.saleOfficeNo = saleOfficeNo;
	}

	public String getSaleWindowNo() {
		return saleWindowNo;
	}

	public void setSaleWindowNo(String saleWindowNo) {
		this.saleWindowNo = saleWindowNo;
	}

	public String getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getSpecialStr() {
		return specialStr;
	}

	public void setSpecialStr(String specialStr) {
		this.specialStr = specialStr;
	}
	
	public void printTicket(){
//		log.debug("getTicketNo=="+this.getTicketNo());
//		log.debug("getFromStationCode=="+this.getFromStationCode());
//		log.debug("getEndStationCode=="+this.getEndStationCode());
//		log.debug("getChangeStationCode=="+this.getChangeStationCode());
//		log.debug("getTrainCode=="+this.getTrainCode());
//		log.debug("getCoachNo=="+this.getCoachNo());
//		log.debug("getSeatCode=="+this.getSeatCode());
//		log.debug("getTicketType=="+this.getTicketType());
//		log.debug("getSeatNo=="+this.getSeatNo());
//		log.debug("getTicketPrice=="+this.getTicketPrice());
//		log.debug("getTrainDate=="+this.getTrainDate());
//		log.debug("getChangeFlag=="+this.getChangeFlag());
//		log.debug("getTicketSourceCenter=="+this.getTicketSourceCenter());
//		log.debug("getBzsFlag=="+this.getBzsFlag());
//		log.debug("getSaleOfficeNo=="+this.getSaleOfficeNo());
//		log.debug("getSaleWindowNo=="+this.getSaleWindowNo());
//		log.debug("getSaleDate=="+this.getSaleDate());
//		log.debug("getCardType=="+this.getCardType());
//		log.debug("getCardNo=="+this.getCardNo());
//		log.debug("getPassengerName=="+this.getPassengerName());
//		log.debug("getSpecialStr=="+this.getSpecialStr());
//		if(this.getPassengerName().equals("韩翔宇")){
//			log.debug("请通过!");
//		}
	}
}
