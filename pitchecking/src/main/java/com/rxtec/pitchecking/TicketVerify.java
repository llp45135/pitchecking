package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.utils.DateUtils;

public class TicketVerify {
	private Logger log = LoggerFactory.getLogger("TicketVerify");
	private Ticket ticket = null;
	private IDCard idCard = null;

	/**
	 * 票证核验具体实现
	 * 
	 * @return
	 */
	public int verify() {
		log.debug("ticket==" + ticket + "||idcard==" + idCard);
		if (ticket == null || idCard == null) {
			if (ticket != null) {
				if (ticket.getTrainDate().equals(DateUtils.getStringDateShort2())) {
					log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
					return Config.TicketVerifyStationRuleFail;
				}else{
					return Config.TicketVerifyWaitInput;
				}
			} else
				return Config.TicketVerifyWaitInput;
		} else {
			// TODO 执行比对
			// 校验车站验票规则

			// DeviceManager.getInstance().repaintStart();
			if (ticket.getTrainDate().equals(DateUtils.getStringDateShort2())) {
				log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);

				// timeIntevel = 0;
				return Config.TicketVerifyStationRuleFail;
			}
			// 票证比对
			if (!ticket.getCardNo().equals(idCard.getIdNo())) {
				log.debug("TicketVerifyIDFail==" + Config.TicketVerifyIDFail);

				// timeIntevel = 0;
				return Config.TicketVerifyIDFail;
			}
			return Config.TicketVerifySucc;
		}
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

	public void reset() {
		this.ticket = null;
		this.idCard = null;
	}

}
