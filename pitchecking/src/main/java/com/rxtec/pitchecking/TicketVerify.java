package com.rxtec.pitchecking;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.com.Variant;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.quickhigh.TrainInfomation;
import com.rxtec.pitchecking.net.event.wharf.ShipTicket;
import com.rxtec.pitchecking.net.event.wharf.TicketCheckCenter;
import com.rxtec.pitchecking.service.ticketsystem.TIDCardInfo;
import com.rxtec.pitchecking.service.ticketsystem.TicketReservationService;
import com.rxtec.pitchecking.utils.CalUtils;

public class TicketVerify {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private Logger trainLog = LoggerFactory.getLogger("TrainInfo");
	private Ticket ticket = null;
	private IDCard idCard = null;

	/**
	 * 
	 * @return
	 */
	public String verifyShipTicket() {
		log.info("准备开始联机进行电子船票核验...");

		if (idCard != null) {
			log.info("二代证不为空");
			if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
				log.info("重复刷卡");
				return Config.TicketVerifyRepeatCheck;
			} else {
				TicketCheckCenter ticketCenter = TicketCheckCenter.getInstance();
				log.info("ShipTicketUrl==" + Config.getInstance().getShipTicketUrl());
				log.info("getIdNo==" + idCard.getIdNo());
				log.info("WharfDevId==" + Config.getInstance().getWharfDevId());
				Variant resultJson = ticketCenter.GetTicketFromIDNumber(Config.getInstance().getShipTicketUrl(), idCard.getIdNo(), "1",
						Config.getInstance().getWharfDevId(), "竹江");
				if (resultJson.toString().indexOf("\"code\":\"0000\"") != -1) {
					log.info("有电子船票");
					boolean passFlag = false;
					try {
						JSONObject json = new JSONObject(resultJson.toString());
						JSONArray jsonArray = json.getJSONArray("ListTicket");

						List list = new ArrayList();
						for (int i = 0; i < jsonArray.length(); i++) {
							ShipTicket ticket = new ShipTicket();
							JSONObject ship = (JSONObject) jsonArray.get(i);
							String TPID = (String) ship.get("TPID");
							ticket.setTPID(TPID);
							String SHIP_NAME = (String) ship.get("SHIP_NAME");
							ticket.setSHIP_NAME(SHIP_NAME);
							String ORDERID = (String) ship.get("ORDERID");
							ticket.setORDERID(ORDERID);
							String SHT_NAME = (String) ship.get("SHT_NAME");
							ticket.setSHT_NAME(SHT_NAME);
							JSONArray listRealName = (JSONArray) ship.getJSONArray("listRealName");
							ticket.setListRealName(listRealName);
							String ISOPEN = (String) ship.get("ISOPEN");
							ticket.setISOPEN(ISOPEN);
							String PORT_DOCKS = (String) ship.get("PORT_DOCKS");
							ticket.setPORT_DOCKS(PORT_DOCKS);
							String SHIPPINGDAY = (String) ship.get("SHIPPINGDAY");
							ticket.setSHIPPINGDAY(SHIPPINGDAY);
							String SHIPPINGTIME = (String) ship.get("SHIPPINGTIME");
							ticket.setSHIPPINGTIME(SHIPPINGTIME);
							String SHR_NAME = (String) ship.get("SHR_NAME");
							ticket.setSHR_NAME(SHR_NAME);
							String STR_NAME = (String) ship.get("STR_NAME");
							ticket.setSTR_NAME(STR_NAME);
							list.add(ticket);
						}

						if (Config.getInstance().getIsCheckShipTicketRule() == 1) { // 需要核验规则
							if (list.size() > 0) {
								Iterator ite = list.iterator();
								while (ite.hasNext()) {
									ShipTicket shipTicket = (ShipTicket) ite.next();
									log.info("SHIPPINGDAY==" + shipTicket.getSHIPPINGDAY() + ",ShipName==" + shipTicket.getSHIP_NAME());
									if (shipTicket.getSHIPPINGDAY().equals(CalUtils.getStringDateShort())) {
										passFlag = true;
									}
								}
							}
						} else {
							log.info("测试状态，不需要核验规则");
							passFlag = true;
						}

						if (passFlag) {
							return "000000";
						} else {
							log.info("船票不符");
							return "-89002"; // 船票不符
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						log.error("", e);
					}
				} else {
					if (resultJson.toString().indexOf("\"code\":\"1001\"") != -1) {
						log.info("单据不存在");
						return "-89001";
					} else if (resultJson.toString().indexOf("\"code\":\"9004\"") != -1) {
						log.info("内部服务器错误");
						return "-89004";
					} else {
						log.info("其他错误：" + resultJson);
						return "-89009";
					}
				}
			}
		}
		return "000000";
	}

	/**
	 * 票证核验 连接客票
	 * 
	 * @return
	 */
	public String verifyFromTK() {
		if (ticket == null || idCard == null) {
			if (ticket != null) { // 有票
				log.info("有票无证");
				return Config.TicketVerifyWaitInput;
			} else if (idCard != null) { // 有证无票
				log.info("有证无票");

				if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
					log.info("重复刷卡");
					return Config.TicketVerifyRepeatCheck;
				}

				TIDCardInfo tidCardInfo = new TIDCardInfo();
				tidCardInfo.setIDName(idCard.getIDNameArray());
				tidCardInfo.setIDSex(idCard.getIDSexArray());
				tidCardInfo.setIDNation(idCard.getIDNationArray());
				tidCardInfo.setIDBirth(idCard.getIDBirthArray());
				tidCardInfo.setIDDwelling(idCard.getIDDwellingArray());
				tidCardInfo.setIDCode(idCard.getIDCodeArray());
				tidCardInfo.setIDIssue(idCard.getIDIssueArray());
				tidCardInfo.setIDEfficb(idCard.getIDEfficbArray());
				tidCardInfo.setIDEffice(idCard.getIDEfficeArray());
				tidCardInfo.setIDNewAddr(idCard.getIDNewAddrArray());
				tidCardInfo.setIDPhoto(idCard.getIDPhotoArray());

				int retVal = TicketReservationService.getInstance().TicketVerify("", tidCardInfo, "1");
				if (retVal == 0) {
					return "000000";
				} else {
					return TicketReservationService.getInstance().getTicketVerifyOutStr().substring(0, 6);
				}
			} else {// 无票无证
				return "-00999";
			}
		} else { // 有票有证
			log.info("有票有证");

			if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
				log.info("重复刷卡");
				return Config.TicketVerifyRepeatCheck;
			}

			log.info("barCode = " + ticket.getBarCode());
			TIDCardInfo tidCardInfo = new TIDCardInfo();
			tidCardInfo.setIDName(idCard.getIDNameArray());
			tidCardInfo.setIDSex(idCard.getIDSexArray());
			tidCardInfo.setIDNation(idCard.getIDNationArray());
			tidCardInfo.setIDBirth(idCard.getIDBirthArray());
			tidCardInfo.setIDDwelling(idCard.getIDDwellingArray());
			tidCardInfo.setIDCode(idCard.getIDCodeArray());
			tidCardInfo.setIDIssue(idCard.getIDIssueArray());
			tidCardInfo.setIDEfficb(idCard.getIDEfficbArray());
			tidCardInfo.setIDEffice(idCard.getIDEfficeArray());
			tidCardInfo.setIDNewAddr(idCard.getIDNewAddrArray());
			tidCardInfo.setIDPhoto(idCard.getIDPhotoArray());

			// log.info("getIDName.length = " + tidCardInfo.getIDName().length);
			// log.info("getIDSex.length = " + tidCardInfo.getIDSex().length);
			// log.info("getIDNation.length = " +
			// tidCardInfo.getIDNation().length);
			// log.info("getIDBirth.length = " +
			// tidCardInfo.getIDBirth().length);
			// log.info("getIDDwelling.length = " +
			// tidCardInfo.getIDDwelling().length);
			// log.info("getIDCode.length = " + tidCardInfo.getIDCode().length);
			// log.info("getIDIssue.length = " +
			// tidCardInfo.getIDIssue().length);
			// log.info("getIDEfficb.length = " +
			// tidCardInfo.getIDEfficb().length);
			// log.info("getIDEffice.length = " +
			// tidCardInfo.getIDEffice().length);
			// log.info("getIDNewAddr.length = " +
			// tidCardInfo.getIDNewAddr().length);
			// log.info("getIDPhoto.length = " +
			// tidCardInfo.getIDPhoto().length);

			int retVal = TicketReservationService.getInstance().TicketVerify(ticket.getBarCode(), tidCardInfo, "0");
			if (retVal == 0) {
				return "000000";
			} else {
				return TicketReservationService.getInstance().getTicketVerifyOutStr().substring(0, 6);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public String verifyForCSQOutgate() {
		if (ticket != null) { // 有票
			if (!ticket.getEndStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
				log.info("TicketVerifyStationRuleFail==非本站出闸");
				return Config.TicketVerifyStationRuleFail;
			} else if (CalUtils.getCalcDateShort(ticket.getTrainDate(), CalUtils.getStringDateShort2()) > 3) {
				log.info("TicketVerifyDateRuleFail==非当日出闸");
				return Config.TicketVerifyTrainDateRuleFail;
			} else {
				return Config.TicketVerifySucc;
			}
		}
		return Config.TicketVerifySucc;
	}

	private void verifyChaoShanTrain() {

	}

	/**
	 * 单机版票证核验具体实现
	 * 
	 * @return
	 */
	public String verify() {

		// log.debug("ticket==" + ticket + "||idcard==" + idCard);
		if (ticket == null || idCard == null) {
			if (ticket != null) { // 有票
				if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) {// 需要核验票证
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.info("TicketVerifyStationRuleFail==非本站乘车");
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("C") && Config.getInstance().getIsSupportTicketWithC() == 0) { // 城际列车C字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("D") && Config.getInstance().getIsSupportTicketWithD() == 0) { // 城际动车D字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("G") && Config.getInstance().getIsSupportTicketWithG() == 0) { // 高铁动车G字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (!ticket.getTrainCode().startsWith("G") && !ticket.getTrainCode().startsWith("D") && !ticket.getTrainCode().startsWith("C")
							&& Config.getInstance().getIsSupportTicketWithK() == 0) { // 其他普通车票
						return Config.TicketVerifyStationRuleFail;
					} else {
						if (Config.getInstance().getIsGGQChaoshanMode() == 1) {
							String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
									+ ticket.getTrainDate().substring(6, 8);
							// String startTime = trainDate + " " +
							// DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode())
							// + ":00" + ".000";
							// log.info("TrainCode==" + ticket.getTrainCode() +
							// ",开车时间==" + startTime);

							TrainInfomation tis = DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate);
							
							if (tis == null) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							}

							String trainStatus = tis.getTrainStatus();
							if (trainStatus.equals("1")) {
								return Config.TicketVerifyTrainStopped; // 列車已經停運
							} else if (trainStatus.equals("2")) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							} else if(trainStatus.indexOf("停运")!=-1){
								return Config.TicketVerifyTrainStopped; // 列車已經停运
							}
							
							String startTime = tis.getDepartTime() + ".000";

							log.info("TrainCode==" + ticket.getTrainCode() + ",图定开车时间" + tis.getPlanDepartTime() + ",实际开车时间==" + startTime);

							if (ticket.getTrainCode().equals("D7509")) {
								if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
									log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");
									String nowTime = CalUtils.getStringDateHaomiao();
									try {
										long tt = CalUtils.howLong("m", nowTime, startTime);
										log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
										if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
											return Config.TicketVerifyStopCheckFail;
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										log.error("", e);
									}
								} else {// 当前时间>开车时间
									log.info("当前时间>开车时间,停止检票");
									return Config.TicketVerifyStopCheckFail;
								}
							}
							if (ticket.getTrainCode().equals("D7501")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7509")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7509" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7521")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7501")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7501" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7529")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7521")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7521" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7525")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7529")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7529" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7513")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7525")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7525" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7517")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7513")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7513" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7505")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7517")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7517" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}

						} else {
							if (DeviceConfig.getInstance().getSZQTrainsMap() != null && DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) != null) { // 时刻表里有该车次
								if (Config.getInstance().getIsMatchTodayByFirst() == 1) { // 优先判断是否当天票
									if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) { // 1、非当日票
										int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
										// trainLog.info("trainCode==" +
										// ticket.getTrainCode() +
										// ",trainDate==" +
										// ticket.getTrainDate() + ",相差天数 = " +
										// days);
										if (days > 0) {
											trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
											log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
											return Config.TicketVerifyNotStartCheckFail;
										}
										if (days < 0) {
											trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
											log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} else {
									String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
											+ ticket.getTrainDate().substring(6, 8);
									String startTime = trainDate + " " + DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) + ":00" + ".000";

									log.info("TrainCode==" + ticket.getTrainCode() + ",开车时间==" + startTime);

									if (CalUtils.isDateBefore(startTime)) { // 开车时间>当前时间
										log.info("开车时间>当前时间");
										String nowTime = CalUtils.getStringDateHaomiao();
										try {
											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info("tt====" + tt);
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
										log.info("开车时间<当前时间");
										return Config.TicketVerifyStopCheckFail;
									}
								}
							} else { // 时刻表里无该车次，则只验证是否当日票
								trainLog.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
								log.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());

									if (days > 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										return Config.TicketVerifyStopCheckFail;
									}
								}
							}
						}
					}
					return Config.TicketVerifyWaitInput;

				} else { // 不需要核验
					return Config.TicketVerifyWaitInput;
				}
			} else if (idCard != null) { // 有证无票
				log.debug("getSoftIdNo==" + DeviceConfig.getInstance().getSoftIdNo());

				if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
					log.info("重复刷卡");
					return Config.TicketVerifyRepeatCheck;
				}

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
				return "-00999";
			}
		} else { // 有票有证
			// TODO 执行比对
			// 校验车站验票规则
			log.info("fromStationCode==" + ticket.getFromStationCode() + ",trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
			log.info("belongStationCode==" + DeviceConfig.getInstance().getBelongStationCode() + "#");
			if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) { // 需要核验

				if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
					log.info("重复刷卡");
					return Config.TicketVerifyRepeatCheck;
				}

				if (DeviceConfig.getInstance().getSoftIdNo().indexOf(idCard.getIdNo()) != -1 && ticket.getCardNo().equals(idCard.getIdNo())) { // 白名单
					return Config.TicketVerifySucc;
				} else if (!ticket.getCardNo().equals(idCard.getIdNo())) {// 1、票证比对不一致
					log.debug("TicketVerifyIDFail==" + Config.TicketVerifyIDFail);
					return Config.TicketVerifyIDFail;
				} else {
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("C") && Config.getInstance().getIsSupportTicketWithC() == 0) { // 城际列车C字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("D") && Config.getInstance().getIsSupportTicketWithD() == 0) { // 城际动车D字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("G") && Config.getInstance().getIsSupportTicketWithG() == 0) { // 高铁动车G字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (!ticket.getTrainCode().startsWith("G") && !ticket.getTrainCode().startsWith("D") && !ticket.getTrainCode().startsWith("C")
							&& Config.getInstance().getIsSupportTicketWithK() == 0) { // 其他普通车票
						return Config.TicketVerifyStationRuleFail;
					} else {
						if (Config.getInstance().getIsGGQChaoshanMode() == 1) {
							String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
									+ ticket.getTrainDate().substring(6, 8);
							// String startTime = trainDate + " " +
							// DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode())
							// + ":00" + ".000";
							// log.info("TrainCode==" + ticket.getTrainCode() +
							// ",开车时间==" + startTime);

							TrainInfomation tis = DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate);
							
							if (tis == null) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							}

							String trainStatus = tis.getTrainStatus();
							if (trainStatus.equals("1")) {
								return Config.TicketVerifyTrainStopped; // 列車已經停運
							} else if (trainStatus.equals("2")) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							} else if(trainStatus.indexOf("停运")!=-1){
								return Config.TicketVerifyTrainStopped; // 列車已經停运
							}
							
							String startTime = tis.getDepartTime() + ".000";

							log.info("TrainCode==" + ticket.getTrainCode() + ",图定开车时间" + tis.getPlanDepartTime() + ",实际开车时间==" + startTime);

							if (ticket.getTrainCode().equals("D7509")) {
								if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
									log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");
									String nowTime = CalUtils.getStringDateHaomiao();
									try {
										long tt = CalUtils.howLong("m", nowTime, startTime);
										log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
										if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
											return Config.TicketVerifyStopCheckFail;
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										log.error("", e);
									}
								} else {// 当前时间>开车时间
									log.info("当前时间>开车时间,停止检票");
									return Config.TicketVerifyStopCheckFail;
								}
							}
							if (ticket.getTrainCode().equals("D7501")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7509")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7509" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7521")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7501")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7501" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7529")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7521")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7521" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7525")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7529")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7529" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7513")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7525")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7525" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7517")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7513")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7513" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}
							if (ticket.getTrainCode().equals("D7505")) {
								try {
									// String lastTrainStartTime = trainDate + "
									// " +
									// DeviceConfig.getInstance().getSZQTrainsMap().get("D7517")
									// + ":00" + ".000";
									String lastTrainStartTime = DeviceEventListener.getInstance().getTrainInfoMap().get("D7517" + trainDate).getDepartTime() + ".000";
									String nowTime = CalUtils.getStringDateHaomiao();
									long aa = CalUtils.howLong("m", nowTime, lastTrainStartTime);

									if (CalUtils.isDateBefore(lastTrainStartTime) && aa > DeviceConfig.getInstance().getStopCheckMinutes()) { // 当前时间<上一班车的开车时间,未开检
										log.info(ticket.getTrainCode() + " 当前时间<上一班车的开检时间,未开检");
										return Config.TicketVerifyNotStartCheckFail;
									} else {
										if (CalUtils.isDateBefore(startTime)) { // 当前时间<开车时间
											log.info(ticket.getTrainCode() + " 当前时间<开车时间,允许进入候车室");

											long tt = CalUtils.howLong("m", nowTime, startTime);
											log.info(ticket.getTrainCode() + " 距离开车时间还有 " + tt + "分钟");
											if (tt <= DeviceConfig.getInstance().getStopCheckMinutes()) {
												return Config.TicketVerifyStopCheckFail;
											}
										} else {// 当前时间>开车时间
											log.info("当前时间>开车时间,停止检票");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("", e);
								}
							}

						} else {
							if (DeviceConfig.getInstance().getSZQTrainsMap() != null && DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) != null) { // 时刻表里有该车次
								if (Config.getInstance().getIsMatchTodayByFirst() == 1) { // 优先匹配是否当天票
									if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
										int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
										if (days > 0) {
											trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
											log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
											return Config.TicketVerifyNotStartCheckFail;
										}
										if (days < 0) {
											trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
											log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
											return Config.TicketVerifyStopCheckFail;
										}
									}
								}

								String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
										+ ticket.getTrainDate().substring(6, 8);
								String startTime = trainDate + " " + DeviceConfig.getInstance().getSZQTrainsMap().get(ticket.getTrainCode()) + ":00" + ".000";
								log.info("TrainCode==" + ticket.getTrainCode() + ",开车时间==" + startTime);

								if (CalUtils.isDateBefore(startTime)) { // 开车时间>当前时间
									log.info("开车时间>当前时间");
									String nowTime = CalUtils.getStringDateHaomiao();
									try {
										long tt = CalUtils.howLong("m", nowTime, startTime);
										log.info("tt====" + tt);
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
									log.info("开车时间<当前时间");
									return Config.TicketVerifyStopCheckFail;
								}
							} else { // 时刻表里无该车次
								trainLog.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
								log.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
									if (days > 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										return Config.TicketVerifyStopCheckFail;
									}
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

	/**
	 * 根据旅服数据核验
	 * 
	 * @return
	 */
	public String verifyFromLvFuService() {

		// log.debug("ticket==" + ticket + "||idcard==" + idCard);
		if (ticket == null || idCard == null) {
			if (ticket != null) { // 有票
				if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) {// 需要核验票证
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.info("TicketVerifyStationRuleFail==非本站乘车");
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("C") && Config.getInstance().getIsSupportTicketWithC() == 0) { // 城际列车C字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("D") && Config.getInstance().getIsSupportTicketWithD() == 0) { // 城际动车D字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("G") && Config.getInstance().getIsSupportTicketWithG() == 0) { // 高铁动车G字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (!ticket.getTrainCode().startsWith("G") && !ticket.getTrainCode().startsWith("D") && !ticket.getTrainCode().startsWith("C")
							&& Config.getInstance().getIsSupportTicketWithK() == 0) { // 其他普通车票
						return Config.TicketVerifyStationRuleFail;
					} else {
						String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
								+ ticket.getTrainDate().substring(6, 8);
						log.info("trainDate==" + trainDate);

						if (DeviceEventListener.getInstance().getTrainInfoMap() != null
								&& DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate) != null) { // 时刻表里有该车次
							if (Config.getInstance().getIsMatchTodayByFirst() == 1) { // 优先判断是否当天票
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) { // 1、非当日票
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
									// trainLog.info("trainCode==" +
									// ticket.getTrainCode() +
									// ",trainDate==" +
									// ticket.getTrainDate() + ",相差天数 = " +
									// days);
									if (days > 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										return Config.TicketVerifyStopCheckFail;
									}
								}
							} else {
								TrainInfomation tis = DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate);
								if (tis == null) {
									return Config.TicketVerifyInvalidTrainCode; // 无效车次
								}

								String trainStatus = tis.getTrainStatus();
								if (trainStatus.equals("1")) {
									return Config.TicketVerifyTrainStopped; // 列車已經停运
								} else if (trainStatus.equals("2")) {
									return Config.TicketVerifyInvalidTrainCode; // 无效车次
								} else if(trainStatus.indexOf("停运")!=-1){
									return Config.TicketVerifyTrainStopped; // 列車已經停运
								}

								String departStartTime = tis.getDepartTime() + ".000";
								log.info("TrainCode==" + ticket.getTrainCode() + ",图定开车时间" + tis.getPlanDepartTime() + ",实际开车时间==" + departStartTime);

								if (CalUtils.isDateBefore(departStartTime)) { // 开车时间>当前时间
									log.info("开车时间>当前时间");
									String nowTime = CalUtils.getStringDateHaomiao();
									try {
										long tt = CalUtils.howLong("m", nowTime, departStartTime);
										log.info("tt====" + tt);
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
									log.info("开车时间<当前时间");
									return Config.TicketVerifyStopCheckFail;
								}
							}
						} else { // 时刻表里无该车次，则只验证是否当日票
							trainLog.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
							log.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
							if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
								int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());

								if (days > 0) {
									trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
									log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
									return Config.TicketVerifyNotStartCheckFail;
								}
								if (days < 0) {
									trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
									log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
									return Config.TicketVerifyStopCheckFail;
								}
							}
						}
					}
					return Config.TicketVerifyWaitInput;

				} else { // 不需要核验
					return Config.TicketVerifyWaitInput;
				}
			} else if (idCard != null) { // 有证无票
				log.debug("getSoftIdNo==" + DeviceConfig.getInstance().getSoftIdNo());

				if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
					log.info("重复刷卡");
					return Config.TicketVerifyRepeatCheck;
				}

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
				return "-00999";
			}
		} else { // 有票有证
			// TODO 执行比对
			// 校验车站验票规则
			log.info("fromStationCode==" + ticket.getFromStationCode() + ",trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
			log.info("belongStationCode==" + DeviceConfig.getInstance().getBelongStationCode() + "#");
			if (DeviceConfig.getInstance().getCheckTicketFlag() == 1) { // 需要核验

				if (Config.getInstance().getIsAdmitRepeatCheck() == 0 && DeviceEventListener.getInstance().getPersonPassMap().get(idCard.getIdNo()) != null) {
					log.info("重复刷卡");
					return Config.TicketVerifyRepeatCheck;
				}

				if (DeviceConfig.getInstance().getSoftIdNo().indexOf(idCard.getIdNo()) != -1 && ticket.getCardNo().equals(idCard.getIdNo())) { // 白名单
					return Config.TicketVerifySucc;
				} else if (!ticket.getCardNo().equals(idCard.getIdNo())) {// 1、票证比对不一致
					log.debug("TicketVerifyIDFail==" + Config.TicketVerifyIDFail);
					return Config.TicketVerifyIDFail;
				} else {
					if (!ticket.getFromStationCode().equals(DeviceConfig.getInstance().getBelongStationCode())) {// 非本站乘车
						log.debug("TicketVerifyStationRuleFail==" + Config.TicketVerifyStationRuleFail);
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("C") && Config.getInstance().getIsSupportTicketWithC() == 0) { // 城际列车C字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("D") && Config.getInstance().getIsSupportTicketWithD() == 0) { // 城际动车D字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (ticket.getTrainCode().startsWith("G") && Config.getInstance().getIsSupportTicketWithG() == 0) { // 高铁动车G字头车票
						return Config.TicketVerifyStationRuleFail;
					} else if (!ticket.getTrainCode().startsWith("G") && !ticket.getTrainCode().startsWith("D") && !ticket.getTrainCode().startsWith("C")
							&& Config.getInstance().getIsSupportTicketWithK() == 0) { // 其他普通车票
						return Config.TicketVerifyStationRuleFail;
					} else {
						String trainDate = ticket.getTrainDate().substring(0, 4) + "-" + ticket.getTrainDate().substring(4, 6) + "-"
								+ ticket.getTrainDate().substring(6, 8);
						log.info("trainDate==" + trainDate);
						if (DeviceEventListener.getInstance().getTrainInfoMap() != null
								&& DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate) != null) { // 时刻表里有该车次
							if (Config.getInstance().getIsMatchTodayByFirst() == 1) { // 优先匹配是否当天票
								if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
									trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate());
									int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
									if (days > 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
										return Config.TicketVerifyNotStartCheckFail;
									}
									if (days < 0) {
										trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
										return Config.TicketVerifyStopCheckFail;
									}
								}
							}
							
							TrainInfomation tis = DeviceEventListener.getInstance().getTrainInfoMap().get(ticket.getTrainCode() + trainDate);
							
							if (tis == null) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							}

							String trainStatus = tis.getTrainStatus();
							if (trainStatus.equals("1")) {
								return Config.TicketVerifyTrainStopped; // 列車已經停運
							} else if (trainStatus.equals("2")) {
								return Config.TicketVerifyInvalidTrainCode; // 無效車次
							} else if(trainStatus.indexOf("停运")!=-1){
								return Config.TicketVerifyTrainStopped; // 列車已經停运
							}
							
							String departStartTime = tis.getDepartTime() + ".000";
							log.info("TrainCode==" + ticket.getTrainCode() + ",图定开车时间" + tis.getPlanDepartTime() + ",实际开车时间==" + departStartTime);

							if (CalUtils.isDateBefore(departStartTime)) { // 开车时间>当前时间
								log.info("开车时间>当前时间");
								String nowTime = CalUtils.getStringDateHaomiao();
								try {
									long tt = CalUtils.howLong("m", nowTime, departStartTime);
									log.info("tt====" + tt);
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
								log.info("开车时间<当前时间");
								return Config.TicketVerifyStopCheckFail;
							}
						} else { // 时刻表里无该车次
							trainLog.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
							log.info("时刻表里无该车次，则只验证是否当日票!" + "trainCode = " + ticket.getTrainCode() + ",trainDate = " + ticket.getTrainDate());
							if (!ticket.getTrainDate().equals(CalUtils.getStringDateShort2())) {// 1、非当日票
								int days = CalUtils.getCalcDateShort(CalUtils.getStringDateShort2(), ticket.getTrainDate());
								if (days > 0) {
									trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
									log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--未开检");
									return Config.TicketVerifyNotStartCheckFail;
								}
								if (days < 0) {
									trainLog.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
									log.info("trainCode==" + ticket.getTrainCode() + ",trainDate==" + ticket.getTrainDate() + ",非当日票--已停检");
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
