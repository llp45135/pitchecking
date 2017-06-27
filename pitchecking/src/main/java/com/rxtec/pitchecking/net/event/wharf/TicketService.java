package com.rxtec.pitchecking.net.event.wharf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import javax.xml.namespace.QName;  
import javax.xml.soap.MessageFactory;  
import javax.xml.soap.SOAPBody;  
import javax.xml.soap.SOAPBodyElement;  
import javax.xml.soap.SOAPConstants;  
import javax.xml.soap.SOAPEnvelope;  
import javax.xml.soap.SOAPMessage;  
import javax.xml.ws.Dispatch;  
import javax.xml.ws.Service;

public class TicketService {

	private Logger log = LoggerFactory.getLogger("TicketService");
	private static TicketService _instance = new TicketService();

	public static TicketService getInstance() {
		return _instance;
	}

	private TicketService() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

//		try {
//			String endpoint = "http://192.168.1.107:9100/ServiceTrainInfo.asmx?WSDL";
//			// 直接引用远程的wsdl文件
//			// 以下都是套路
//			Service service = new Service();
//			Call call = (Call) service.createCall();
//			call.setTargetEndpointAddress(endpoint);
//			call.setOperationName("GetRailwayDynamicDatas");// WSDL里面描述的接口名称
//			call.addParameter("sequenceNo", org.apache.axis.encoding.XMLType.XSD_DATE, javax.xml.rpc.ParameterMode.IN);// 接口的参数
//			call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);// 设置返回类型
//			String temp = "测试人员";
//			String result = (String) call.invoke(new Object[] { temp });
//			// 给方法传递参数，并且调用方法
//			System.out.println("result is " + result);
//		} catch (Exception e) {
//			System.err.println(e.toString());
//		}
		
		try{
		// {*} * 为图片中的数字  
        String ns = "http://Easyway.net.cn/";  // {1}  
        String wsdlUrl = "http://192.168.1.107:9100/ServiceTrainInfo.asmx";  // {2}  
        //1、创建服务(Service)    
        URL url = new URL(wsdlUrl);
        QName sname = new QName(ns, "ServiceTrainInfo"); // {3}  
        Service service = Service.create(url, sname);

        //2、创建Dispatch    
        Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName(ns, "ServiceTrainInfoSoap12"), SOAPMessage.class, Service.Mode.MESSAGE); // {4}    

        //3、创建SOAPMessage    
        SOAPMessage msg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        //4、创建QName来指定消息中传递数据    
        QName ename = new QName(ns, "SendPlmMsg", "tem");//<nn:add xmlns="xx"/>  // {5}  
        SOAPBodyElement ele = body.addBodyElement(ename);
        // 传递参数    
        // {6}  
        ele.addChildElement("strMobiles", "tem").setValue("151****3701");
        ele.addChildElement("strMSg", "tem").setValue("测试!");
        msg.writeTo(System.out);
        System.out.println("\n invoking.....");

        //5、通过Dispatch传递消息,会返回响应消息    
        SOAPMessage response = dispatch.invoke(msg);
        response.writeTo(System.out);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

}
