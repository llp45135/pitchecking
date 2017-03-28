package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.QRCodeReaderEvent;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.IDCardUtil;

public class CardReaderEventDemo {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static CardReaderEventDemo _instance = new CardReaderEventDemo();

	public static CardReaderEventDemo getInstance() {
		return _instance;
	}

	private CardReaderEventDemo() {

	}

	public void offerDeviceEvent() {
		log.debug("offerDeviceEvent readCardEvent");
		IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
		IDCard idCard = IDCardUtil.createIDCard("zp.jpg");
		readCardEvent.setIdCard(idCard);
		DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);

		QRCodeReaderEvent qrEvent = new QRCodeReaderEvent(Config.QRReaderEvent);
		Ticket ticket = new Ticket();
		ticket.setCardNo("520203197912141119");
		ticket.setTicketNo("T129999");
		ticket.setFromStationCode("GGQ");
		ticket.setEndStationCode("SZQ");
		ticket.setTicketType("0");
		ticket.setCardType("1");
		ticket.setTrainDate(CalUtils.getStringDateShort2());
		qrEvent.setTicket(ticket);
		DeviceEventListener.getInstance().offerDeviceEvent(qrEvent);
		log.debug("offerDeviceEvent qrEvent");
	}
}
