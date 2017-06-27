package com.rxtec.pitchecking.net.event.wharf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.rxtec.pitchecking.Config;

public class TicketCheckCenter {
	private static TicketCheckCenter _instance;
	private Dispatch test = null;

	public static synchronized TicketCheckCenter getInstance() {
		if (_instance == null) {
			_instance = new TicketCheckCenter();
		}
		return _instance;
	}

	private TicketCheckCenter() {
		test = new ActiveXComponent("MpsTicketCom.MpsTicket");
		Dispatch.call(test, "SetUser", Config.getInstance().getShipTicketCenterUser(), Config.getInstance().getShipTicketCenterPwd());
	}

	/**
	 * 主要函数： /// <summary> /// 根据身份证获取门票 /// </summary> ///
	 * <param name="url">接口url</param> /// <param name="idNumber">身份证号</param>
	 * /// <param name="checkOperation">默认1 查询</param> ///
	 * <param name="devID">设备ID </param> ///
	 * <param name="checkDock">码头名称</param> /// <returns>json字符串</returns>
	 * 
	 * @return
	 */
	public Variant GetTicketFromIDNumber(String url, String idNumber, String checkOperation, String devID, String checkDock) {
		Variant result = null;
		try {
			// Dispatch.call(test, "SetUser",
			// "admin","21218CCA77804D2BA1922C33E0151105");

			result = Dispatch.call(test, "GetTicketFromIDNumber", url, idNumber, checkOperation, devID, checkDock);
			// System.out.println("GetTicketFromIDNumber Result = " + result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketCheckCenter ticketCenter = TicketCheckCenter.getInstance();
		Variant resultJson = ticketCenter.GetTicketFromIDNumber(Config.getInstance().getShipTicketUrl(), "450304197703190515", "1", Config.getInstance().getWharfDevId(),
				"竹江");
		if (resultJson.toString().indexOf("\"code\":\"0000\"") != -1) {
			// System.out.println("有电子票：" + resultJson.toString());
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

				if (list.size() > 0) {
					Iterator ite = list.iterator();
					while (ite.hasNext()) {
						ShipTicket shipTicket = (ShipTicket) ite.next();
						System.out.println("TPID = " + shipTicket.getTPID());
						System.out.println(shipTicket.getListRealName());
						System.out.println("SHIP_NAME = " + shipTicket.getSHIP_NAME());
						System.out.println("SHIPPINGDAY = " + shipTicket.getSHIPPINGDAY());
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (resultJson.toString().indexOf("\"code\":\"1001\"") != -1) {
				System.out.println("单据不存在");
			} else if (resultJson.toString().indexOf("\"code\":\"9004\"") != -1) {
				System.out.println("内部服务器错误");
			}
		}

		// resultJson =
		// ticketCenter.GetTicketFromIDNumber("http://ticket.gxpft.com:8090/twebservice/realNameForJson/checkRealName",
		// "450304197703190515", "1",
		// "000111", "竹江");
	}

}
