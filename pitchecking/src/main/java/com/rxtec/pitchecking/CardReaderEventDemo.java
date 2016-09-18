package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.QRCodeReaderEvent;

public class CardReaderEventDemo {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static CardReaderEventDemo _instance = new CardReaderEventDemo();

	public static CardReaderEventDemo getInstance() {
		return _instance;
	}

	private CardReaderEventDemo() {

	}

	public void offerDeviceEvent() {
		log.info("offerDeviceEvent readCardEvent");
		IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
		IDCard idCard = new IDCard();
		idCard.setIdNo("520203197912141119");
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
		qrEvent.setTicket(ticket);
		DeviceEventListener.getInstance().offerDeviceEvent(qrEvent);
		log.info("offerDeviceEvent qrEvent");
	}
}
