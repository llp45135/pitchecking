package com.rxtec.pitchecking;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.CalUtils;

public class TicketVerify {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private Logger trainLog = LoggerFactory.getLogger("TrainInfo");
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
			if (ticket != null) { // 有票
				if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) {// 需要核验票证
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getFromStationCode().equals("SZQ") && ticket.getTrainCode().startsWith("C")) { // 广深线动车
						return Config.TicketVerifyStationRuleFail;
					} else {
						if (DeviceConfig.getInstance().getSZQTrainsMap() != null
								&& DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) != null) {   //时刻表里有该车次
							if(Config.getInstance().getIsMatchTodayByFirst()==1){    //优先匹配是否当天票
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
									trainLog.debug(
											"trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(),
											ticket.getTrainDate());
									if (days > 0) {
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										return Config.TicketVerifyStopCheckFail;
									}
								}
							}							
							
							String trainDate = ticket.getTrainDate().substring(0, 4) + "-"
									+ ticket.getTrainDate().substring(4, 6) + "-"
									+ ticket.getTrainDate().substring(6, 8);
							String startTime = trainDate + " "
									+ DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) + ":00"
									+ ".000";

							log.debug("TrainCode==" + ticket.getTrainCode() + ",开车时间==" + startTime);

							if (CalUtils.isDateBefore(startTime)) { // 开车时间>当前时间
								log.debug("开车时间>当前时间");
								String nowTime = CalUtils.getStringDateHaomiao();
								try {
									long tt = CalUtils.howLong("m", nowTime, startTime);
									log.debug("tt====" + tt);
									if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
										return Config.TicketVerifyStopCheckFail;
									} else if (tt >= DeviceConfig.getInstance().getNotStartCheckMinutes()) {
										return Config.TicketVerifyNotStartCheckFail;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							} else {// 开车时间<当前时间
								log.debug("开车时间<当前时间");
								return Config.TicketVerifyStopCheckFail;
							}
						} else {      //时刻表里无该车次，则只验证是否当日票
							if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
								trainLog.debug(
										"trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
								int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(),
										ticket.getTrainDate());
								if (days > 0) {
									return Config.TicketVerifyNotStartCheckFail;
								}
								if (days < 0) {
									return Config.TicketVerifyStopCheckFail;
								}
							}
						}
					}
					return Config.TicketVerifyWaitInput;
				} else { // 不需要核验
					return Config.TicketVerifyWaitInput;
				}
			} else if (idCard != null) { // 有证
				log.debug("getSoftIdNo==" + DeviceConfig.getInstance().getSoftIdNo());
				if (DeviceConfig.getInstance().getSoftIdNo().indexOf(idCard.getIdNo()) != -1) { // 白名单
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
					virualTicket.setTrainDate(CalUtils.getStringDateShort());
					virualTicket.setSeatCode("8");
					this.setTicket(virualTicket);
					return Config.TicketVerifySucc;
				} else { // 非白名单
					return Config.TicketVerifyWaitInput;
				}
			} else { // 无票无证
				return -99;
			}
		} else { // 有票有证
			// TODO 执行比对
			// 校验车站验票规则
			log.debug("fromStationCode==" + ticket.getFromStationCode() + ",trainCode==" + ticket.getTrainCode()
					+ ",trainDate==" + ticket.getTrainDate());
			log.debug("belongStationCode==" + DeviceConfig.getInstance().getBelongStationCode() + "#");
			if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) { // 需要核验
				if (DeviceConfig.getInstance().getSoftIdNo().indexOf(idCard.getIdNo()) != -1
						&& ticket.getCardNo().equals(idCard.getIdNo())) { // 白名单
					return Config.TicketVerifySucc;
				} else if (!ticket.getCardNo().equals(idCard.getIdNo())) {// 1、票证比对不一致
					log.debug("TicketVerifyIDFail==" + Config.TicketVerifyIDFail);
					return Config.TicketVerifyIDFail;
				} else {
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getFromStationCode().equals("SZQ") && ticket.getTrainCode().startsWith("C")) { // 广深线动车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else {
						if (DeviceConfig.getInstance().getSZQTrainsMap() != null
								&& DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) != null) {   //时刻表里有该车次
							if(Config.getInstance().getIsMatchTodayByFirst()==1){    //优先匹配是否当天票
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
									trainLog.debug(
											"trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(),
											ticket.getTrainDate());
									if (days > 0) {
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										return Config.TicketVerifyStopCheckFail;
									}
								}
							}
							
							String trainDate = ticket.getTrainDate().substring(0, 4) + "-"
									+ ticket.getTrainDate().substring(4, 6) + "-"
									+ ticket.getTrainDate().substring(6, 8);
							String startTime = trainDate + " "
									+ DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) + ":00"
									+ ".000";
							log.debug("TrainCode==" + ticket.getTrainCode() + ",开车时间==" + startTime);

							if (CalUtils.isDateBefore(startTime)) { // 开车时间>当前时间
								log.debug("开车时间>当前时间");
								String nowTime = CalUtils.getStringDateHaomiao();
								try {
									long tt = CalUtils.howLong("m", nowTime, startTime);
									log.debug("tt====" + tt);
									if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
										return Config.TicketVerifyStopCheckFail;
									} else if (tt >= DeviceConfig.getInstance().getNotStartCheckMinutes()) {
										return Config.TicketVerifyNotStartCheckFail;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							} else {// 开车时间<当前时间
								log.debug("开车时间<当前时间");
								return Config.TicketVerifyStopCheckFail;
							}
						} else {   //时刻表里无该车次
							if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
								trainLog.debug(
										"trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
								int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(),
										ticket.getTrainDate());
								if (days > 0) {
									return Config.TicketVerifyNotStartCheckFail;
								}
								if (days < 0) {
									return Config.TicketVerifyStopCheckFail;
								}
							}
						}
					}
				}
			}
			log.debug("return TicketVerifySucc");
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
		log.debug("已经清除本次的票证对象!!");
	}

	public void clearTicket() {
		this.ticket = null;
	}

	public void clearIdCard() {
		this.idCard = null;
	}

}
