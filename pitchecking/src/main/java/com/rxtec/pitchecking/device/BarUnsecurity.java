package com.rxtec.pitchecking.device;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.Ticket;

public class BarUnsecurity {
	private Logger log = LoggerFactory.getLogger("BarUnsecurity");
	private static BarUnsecurity _instance;
	private JNative jnativeBAR2unsecurity = null;

	public static synchronized BarUnsecurity getInstance() {
		if (_instance == null) {
			_instance = new BarUnsecurity();
		}
		return _instance;
	}

	private BarUnsecurity() {
		JNative.setLoggingEnabled(false);
		try {
			jnativeBAR2unsecurity = new JNative("BAR2unsecurity.dll", "uncompress");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("Init BAR2unsecurity.dll Failed!", e);
		}
	}

	/**
	 * 解析二维码数据为车票票面信息
	 * 
	 * @param ticketStr
	 * @return
	 * @throws NumberFormatException
	 * @throws UnsupportedEncodingException
	 */
	public Ticket buildTicket(byte[] ticketArray) throws NumberFormatException, UnsupportedEncodingException {
		Ticket ticket = new Ticket();
		if (ticketArray.length == 117) {

			String ticketStr = new String(ticketArray, "gbk");

			ticket.setTicketNo(ticketStr.substring(0, 7));
			String fromStationCode = ticketStr.substring(7, 10);
			ticket.setFromStationCode(fromStationCode);
			ticket.setFromStationName(DeviceConfig.getInstance().getStationName(fromStationCode));
			String endStationCode = ticketStr.substring(10, 13);
			ticket.setEndStationCode(endStationCode);
			ticket.setToStationName(DeviceConfig.getInstance().getStationName(endStationCode));
			ticket.setChangeStationCode(ticketStr.substring(13, 16));
			String trainCodeStr = ticketStr.substring(16, 24).trim();
			if (trainCodeStr.length() > 5) {
				ticket.setTrainCode(
						trainCodeStr.substring(0, 1) + String.valueOf(Integer.parseInt(trainCodeStr.substring(1))));
			} else {
				ticket.setTrainCode(String.valueOf(Integer.parseInt(trainCodeStr)));
			}
			ticket.setCoachNo(ticketStr.substring(24, 26));
			ticket.setSeatCode(ticketStr.substring(26, 27));
			ticket.setTicketType(ticketStr.substring(27, 29));
			ticket.setSeatNo(ticketStr.substring(29, 33));
			ticket.setTicketPrice(Integer.parseInt(ticketStr.substring(33, 38)));
			byte[] trainDateArray = new byte[8];
			for (int i = 0; i < 8; i++) {
				trainDateArray[i] = ticketArray[i + 38];
			}
			ticket.setTrainDate(new String(trainDateArray));
			ticket.setChangeFlag(ticketStr.substring(46, 47));
			ticket.setTicketSourceCenter(ticketStr.substring(47, 49));
			ticket.setBzsFlag(ticketStr.substring(49, 50));
			ticket.setSaleOfficeNo(ticketStr.substring(50, 57));
			ticket.setSaleWindowNo(ticketStr.substring(57, 60));
			ticket.setSaleDate(ticketStr.substring(60, 68));
			ticket.setCardType(ticketStr.substring(68, 70));
			ticket.setCardNo(ticketStr.substring(72, 90));

			byte[] passengerNameArray = new byte[20];
			for (int i = 0; i < 20; i++) {
				passengerNameArray[i] = ticketArray[i + 90];
			}
			String ss = new String(passengerNameArray, "gbk");
			ticket.setPassengerName(ss.trim());
			byte[] specialArray = new byte[7];
			for (int i = 0; i < 7; i++) {
				specialArray[i] = ticketArray[i + 110];
			}
			ticket.setSpecialStr(new String(specialArray, "gbk"));

			ticket.setInGateNo(DeviceConfig.getInstance().getGateNo());

			trainDateArray = null;
			passengerNameArray = null;
			specialArray = null;
			
//			printTicketInfo(ticket);
		}

		return ticket;
	}

	private void printTicketInfo(Ticket ticket) {
		log.debug("getTicketNo==" + ticket.getTicketNo());
		log.debug("getFromStationCode==" + ticket.getFromStationCode());
		log.debug("getEndStationCode==" + ticket.getEndStationCode());
		log.debug("getChangeStationCode==" + ticket.getChangeStationCode());
		log.debug("getTrainCode==" + ticket.getTrainCode());
		log.debug("getCoachNo==" + ticket.getCoachNo());
		log.debug("getSeatCode==" + ticket.getSeatCode());
		log.debug("getTicketType==" + ticket.getTicketType());
		log.debug("getSeatNo==" + ticket.getSeatNo());
		log.debug("getTicketPrice==" + ticket.getTicketPrice());
		log.debug("getTrainDate==" + ticket.getTrainDate());
		log.debug("getChangeFlag==" + ticket.getChangeFlag());
		log.debug("getTicketSourceCenter==" + ticket.getTicketSourceCenter());
		log.debug("getBzsFlag==" + ticket.getBzsFlag());
		log.debug("getSaleOfficeNo==" + ticket.getSaleOfficeNo());
		log.debug("getSaleWindowNo==" + ticket.getSaleWindowNo());
		log.debug("getSaleDate==" + ticket.getSaleDate());
		log.debug("getCardType==" + ticket.getCardType());
		log.debug("getCardNo==" + ticket.getCardNo());
		log.debug("getPassengerName==" + ticket.getPassengerName());
		log.debug("getSpecialStr==" + ticket.getSpecialStr());
	}

	/**
	 * 二维码数据还原
	 * 
	 * @param barCode
	 * @param year
	 * @return
	 */
	public byte[] uncompress(String barCode, String year) {
		byte[] outStrArray = new byte[117];
		Pointer ticketStrPointer = null;
		try {
			String retval = "-1";
			int i = 0;

			ticketStrPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(117));
			jnativeBAR2unsecurity.setParameter(i++, barCode);
			jnativeBAR2unsecurity.setParameter(i++, ticketStrPointer);
			jnativeBAR2unsecurity.setParameter(i++, Type.LONG, year);
			jnativeBAR2unsecurity.setRetVal(Type.LONG);
			jnativeBAR2unsecurity.invoke();

			retval = jnativeBAR2unsecurity.getRetVal();

			// log.debug("uncompress: retval==" + retval);// 获取返回值

			if (retval.equals("0")) {
				// outStr = ticketStrPointer.getAsString();
				// outStr = new String(outStr.getBytes("UTF-8"),"GBK");
				outStrArray = ticketStrPointer.getMemory();
				// log.debug("uncompress：outStrArray.length==" +
				// outStrArray.length);
				String ticketStr = new String(outStrArray, "gbk");
				// log.debug("uncompress: ticketArray==" + ticketStr + "##");
			}
			ticketStrPointer.dispose();

		} catch (NativeException e) {
			log.error("BarUnsecurity uncompress", e);
		} catch (IllegalAccessException e) {
			log.error("BarUnsecurity uncompress", e);
		} catch (UnsupportedEncodingException e) {
			log.error("BarUnsecurity uncompress", e);
		} finally {
			if (ticketStrPointer != null)
				try {
					ticketStrPointer.dispose();
				} catch (NativeException e) {
					log.error("BarUnsecurity uncompress", e);
				}
		}
		return outStrArray;
	}
	
	public static void main(String[] args) {
		BarUnsecurity barUnsecurity = BarUnsecurity.getInstance();
		Ticket ticket = null;
		String barCode = "030662841131798439349199142258828536855229283991646592935582752300596297631667652340997372666399185220905450514101092520636404597435822209499692";
		try {
			ticket = barUnsecurity.buildTicket(barUnsecurity.uncompress(barCode, "2016"));
			if(ticket!=null){
				System.out.println("TrainCode=="+ticket.getTrainCode());
				System.out.println("CardNo=="+ticket.getCardNo());
				System.out.println("PassengerName=="+ticket.getPassengerName());
			}
		} catch (NumberFormatException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
