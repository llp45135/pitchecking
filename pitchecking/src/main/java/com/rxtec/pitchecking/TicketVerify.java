package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
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

		// log.debug("ticket==" + ticket + "||idcard==" + idCard);
		if (ticket == null || idCard == null) {
			if (ticket != null) {
				if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) {
					if (DeviceConfig.softIdNo.indexOf(ticket.getCardNo()) != -1) {
						return Config.TicketVerifyWaitInput;
					} else if (!ticket.getTrainDate().equals(DateUtils.getStringDateShort2())) {// 1、当日票
						log.debug("TicketVerifyTrainDateRuleFail==" + Config.TicketVerifyTrainDateRuleFail);
						return Config.TicketVerifyTrainDateRuleFail;
					} else if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 1、本站乘车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else {
						return Config.TicketVerifyWaitInput;
					}
				} else {
					return Config.TicketVerifyWaitInput;
				}
			} else if (idCard!=null && DeviceConfig.softIdNo.indexOf(idCard.getIdNo()) != -1) {
				Ticket virualTicket = new Ticket();
				virualTicket.setCardNo(idCard.getIdNo());
				virualTicket.setCardType("1");
				virualTicket.setCoachNo("01");
				virualTicket.setEndStationCode("SZQ");
				virualTicket.setFromStationCode("IZQ");
				virualTicket.setSeatCode("001F");
				virualTicket.setTicketNo("T000006");
				virualTicket.setTicketPrice(99);
				virualTicket.setTicketType("1");
				virualTicket.setTrainCode("G1001");
				virualTicket.setTrainDate(DateUtils.getStringDateShort());
				virualTicket.setSeatCode("8");
				this.setTicket(virualTicket);
				return Config.TicketVerifyWaitInput;
			} else
				return Config.TicketVerifyWaitInput;
		} else {
			// TODO 执行比对
			// 校验车站验票规则
			if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) {
				if (DeviceConfig.softIdNo.indexOf(idCard.getIdNo()) != -1) {
					return Config.TicketVerifySucc;
				} else if (!ticket.getTrainDate().equals(DateUtils.getStringDateShort2())) {// 1、当日票
					log.debug("TicketVerifyTrainDateRuleFail==" + Config.TicketVerifyTrainDateRuleFail);
					return Config.TicketVerifyTrainDateRuleFail;
				} else if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 1、本站乘车
					log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
					return Config.TicketVerifyStationRuleFail;
				} else if (!ticket.getCardNo().equals(idCard.getIdNo())) {// 1、票证比对
					log.debug("TicketVerifyIDFail==" + Config.TicketVerifyIDFail);
					return Config.TicketVerifyIDFail;
				}
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
