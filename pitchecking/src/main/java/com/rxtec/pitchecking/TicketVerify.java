package com.rxtec.pitchecking;

public class TicketVerify {
	private Ticket ticket = null;
	private IDCard idCard = null;

	public int verify() {
		
		return Config.TicketVerifySucc; //debug
		
//		if (ticket == null || idCard == null)
//			return Config.TicketVerifyWaitInput;
//		else{
//			return Config.TicketVerifySucc;
//		}
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public IDCard getIdCard() {
		return idCard;
	}

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}
	
	public void reset(){
		this.ticket = null;
		this.idCard = null;
	}

}
