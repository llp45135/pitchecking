package com.rxtec.pitchecking.device.smartmonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class MonitorXMLUtil {
	private static Logger log = LoggerFactory.getLogger("MonitorXMLUtil");

	public MonitorXMLUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 符合铁科院电子所标准的监控xml文件 整机及主要部件基础信息
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public static void createBaseInfoXml(String fileName, String companyCode) throws Exception {
		Document document;
		Element root;
		root = new Element("RealNameGate");
		document = new Document(root);

		Element AllInfo = new Element("AllInfo");
		root.addContent(AllInfo);

		Element EquipmentVendors = new Element("EquipmentVendors");
		EquipmentVendors.setText(companyCode);
		AllInfo.addContent(EquipmentVendors);

		Element EquipmentModel = new Element("EquipmentModel");
		EquipmentModel.setText(DeviceConfig.gateMachineType);
		AllInfo.addContent(EquipmentModel);

		Element ProductionDate = new Element("ProductionDate");
		ProductionDate.setText(Config.getInstance().getProductionDate());
		AllInfo.addContent(ProductionDate);

		Element SerialNumber = new Element("SerialNumber");
		SerialNumber.setText(Config.getInstance().getGateSerialNo().substring(16));
		AllInfo.addContent(SerialNumber);

		/**
		 * 
		 */
		Element SoftInfo = new Element("SoftInfo");
		root.addContent(SoftInfo);

		Element SoftVersion = new Element("SoftVersion");
		SoftVersion.setText(DeviceConfig.monitorSoftVersion);
		SoftInfo.addContent(SoftVersion);

		/**
		 * 闸门
		 */
		Element DoorContinuousInfo = new Element("DoorContinuousInfo");
		root.addContent(DoorContinuousInfo);
		// ID
		Element DoorContinuousID = new Element("DoorContinuousID");
		DoorContinuousID.setText("1001");
		DoorContinuousInfo.addContent(DoorContinuousID);
		// 型号
		Element DoorContinuousModel = new Element("DoorContinuousModel");
		DoorContinuousModel.setText("MTR2000");
		DoorContinuousInfo.addContent(DoorContinuousModel);
		// 品牌
		Element DoorContinuousBrand = new Element("DoorContinuousBrand");
		DoorContinuousBrand.setText("GZES");
		DoorContinuousInfo.addContent(DoorContinuousBrand);

		/**
		 * 摄像头
		 */
		Element FaceImageAcquisitionInfo = new Element("FaceImageAcquisitionInfo");
		root.addContent(FaceImageAcquisitionInfo);
		// ID
		Element FaceImageAcquisitionID = new Element("FaceImageAcquisitionID");
		FaceImageAcquisitionID.setText(Config.getInstance().getBehindCameraNo());
		FaceImageAcquisitionInfo.addContent(FaceImageAcquisitionID);
		// 型号
		Element FaceImageAcquisitionModel = new Element("FaceImageAcquisitionModel");
		FaceImageAcquisitionModel.setText("SR300");
		FaceImageAcquisitionInfo.addContent(FaceImageAcquisitionModel);
		// 品牌
		Element FaceImageAcquisitionBrand = new Element("FaceImageAcquisitionBrand");
		FaceImageAcquisitionBrand.setText("Intel");
		FaceImageAcquisitionInfo.addContent(FaceImageAcquisitionBrand);

		/**
		 * 二代证读卡器
		 */
		Element IDCardReaderInfo = new Element("IDCardReaderInfo");
		root.addContent(IDCardReaderInfo);
		// ID
		Element IDCardReaderID = new Element("IDCardReaderID");
		IDCardReaderID.setText(DeviceConfig.getInstance().getIDCardReaderID());
		IDCardReaderInfo.addContent(IDCardReaderID);
		// 型号
		Element IDCardReaderModel = new Element("IDCardReaderModel");
		IDCardReaderModel.setText("SD2000-1");
		IDCardReaderInfo.addContent(IDCardReaderModel);
		// 品牌
		Element IDCardReaderBrand = new Element("IDCardReaderBrand");
		IDCardReaderBrand.setText("ZKXA");
		IDCardReaderInfo.addContent(IDCardReaderBrand);

		/**
		 * 二维码扫描器
		 */
		Element QRCodeInfo = new Element("QRCodeInfo");
		root.addContent(QRCodeInfo);
		// ID
		Element QRCodeID = new Element("QRCodeID");
		QRCodeID.setText("10102");
		QRCodeInfo.addContent(QRCodeID);
		// 型号
		Element QRCodeModel = new Element("QRCodeModel");
		QRCodeModel.setText("7580g");
		QRCodeInfo.addContent(QRCodeModel);
		// 品牌
		Element QRCodeBrand = new Element("QRCodeBrand");
		QRCodeBrand.setText("Honeywell");
		QRCodeInfo.addContent(QRCodeBrand);

		saveXML(document, fileName);
	}

	/**
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public static void createStatusInfoXml(String fileName, Map<String, String> statusMap) throws Exception {
		Document document;
		Element root;
		root = new Element("FaultInf");
		document = new Document(root);

		/**
		 * 整机状态
		 */
		Element StatusInfo = new Element("StatusInfo");
		root.addContent(StatusInfo);

		Element StatusInf = new Element("StatusInf");
		StatusInf.setText(statusMap.get("StatusInf"));
		StatusInfo.addContent(StatusInf);

		Element MaintainsDateTime = new Element("MaintainsDateTime");
		MaintainsDateTime.setText(statusMap.get("MaintainsDateTime"));
		StatusInfo.addContent(MaintainsDateTime);

		/**
		 * 
		 */
		// String doorNo = "001";
		// String doorErrcode = companyCode + "001" + machineCode + doorNo +
		// "005";

		Element DoorContinuous = new Element("DoorContinuous");
		root.addContent(DoorContinuous);

		Element FlapTotal = new Element("FlapTotal");
		FlapTotal.setText(statusMap.get("FlapTotal"));
		DoorContinuous.addContent(FlapTotal);

		Element FlapUpdTime = new Element("FlapUpdTime");
		FlapUpdTime.setText(statusMap.get("FlapUpdTime"));
		DoorContinuous.addContent(FlapUpdTime);

		Element DoorFaultCode = new Element("FaultCode");
		DoorFaultCode.setText(statusMap.get("DoorFaultCode"));
		DoorContinuous.addContent(DoorFaultCode);

		Element DoorMaintainsDateTime = new Element("MaintainsDateTime");
		DoorMaintainsDateTime.setText(statusMap.get("DoorMaintainsDateTime"));
		DoorContinuous.addContent(DoorMaintainsDateTime);

		/**
		 * 
		 */
		// String faceNo = "002";
		// String faceErrcode = companyCode + "001" + machineCode + faceNo +
		// "005";

		Element FaceImageAcquisition = new Element("FaceImageAcquisition");
		root.addContent(FaceImageAcquisition);

		Element FaceFaultCode = new Element("FaultCode");
		FaceFaultCode.setText(statusMap.get("FaceFaultCode"));
		FaceImageAcquisition.addContent(FaceFaultCode);

		Element FaceMaintainsDateTime = new Element("MaintainsDateTime");
		FaceMaintainsDateTime.setText(statusMap.get("FaceMaintainsDateTime"));
		FaceImageAcquisition.addContent(FaceMaintainsDateTime);

		/**
		 * 
		 */
		// String idCardNo = "004";
		// String idCardErrcode = companyCode + "001" + machineCode + idCardNo +
		// "005";

		Element IDCardReader = new Element("IDCardReader");
		root.addContent(IDCardReader);

		Element IDCardFaultCode = new Element("FaultCode");
		IDCardFaultCode.setText(statusMap.get("IDCardFaultCode"));
		IDCardReader.addContent(IDCardFaultCode);

		Element IDCardMaintainsDateTime = new Element("MaintainsDateTime");
		IDCardMaintainsDateTime.setText(statusMap.get("IDCardMaintainsDateTime"));
		IDCardReader.addContent(IDCardMaintainsDateTime);

		/**
		 * 
		 */
		// String qrCodeNo = "003";
		// String qrCodeErrcode = companyCode + "001" + machineCode + qrCodeNo +
		// "005";

		Element QRCode = new Element("QRCode");
		root.addContent(QRCode);

		Element QRCodeFaultCode = new Element("FaultCode");
		QRCodeFaultCode.setText(statusMap.get("QRCodeFaultCode"));
		QRCode.addContent(QRCodeFaultCode);

		Element QRCodeMaintainsDateTime = new Element("MaintainsDateTime");
		QRCodeMaintainsDateTime.setText(statusMap.get("QRCodeMaintainsDateTime"));
		QRCode.addContent(QRCodeMaintainsDateTime);

		saveXML(document, fileName);
	}

	/**
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public static String getFlapCountFromStatusXML(String fileName) throws Exception {
		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(fileName);
		Element FaultInf = document.getRootElement();

		Element DoorContinuous = FaultInf.getChild("DoorContinuous");
		String flapTotal = DoorContinuous.getChild("FlapTotal").getText();
		return flapTotal;
	}

	/**
	 * 更新监控程序的BaseInfo
	 * 0:正常  1:异常
	 * @param fileName
	 * @param moduleCode
	 */
	public static void updateBaseInfoFonMonitor(String fileName, String moduleCode, int statusCode) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(fileName));

			Element FaultInf = document.getRootElement();

			if (moduleCode.equals("001")) {
				if (statusCode == 1) { // 异常
					Element DoorContinuousInfo = FaultInf.getChild("DoorContinuousInfo");
					DoorContinuousInfo.getChild("DoorContinuousID").setText("");
					DoorContinuousInfo.getChild("DoorContinuousModel").setText("");
					DoorContinuousInfo.getChild("DoorContinuousBrand").setText("");
				} else {
					Element DoorContinuousInfo = FaultInf.getChild("DoorContinuousInfo");
					DoorContinuousInfo.getChild("DoorContinuousID").setText("1001");
					DoorContinuousInfo.getChild("DoorContinuousModel").setText("MTR2000");
					DoorContinuousInfo.getChild("DoorContinuousBrand").setText("GZES");
				}
			}

			if (moduleCode.equals("002")) {
				if (statusCode == 1) { // 异常
					Element DoorContinuousInfo = FaultInf.getChild("FaceImageAcquisitionInfo");
					DoorContinuousInfo.getChild("FaceImageAcquisitionID").setText("");
					DoorContinuousInfo.getChild("FaceImageAcquisitionModel").setText("");
					DoorContinuousInfo.getChild("FaceImageAcquisitionBrand").setText("");
				} else {
					Element DoorContinuousInfo = FaultInf.getChild("FaceImageAcquisitionInfo");
					DoorContinuousInfo.getChild("FaceImageAcquisitionID").setText(Config.getInstance().getBehindCameraNo());
					DoorContinuousInfo.getChild("FaceImageAcquisitionModel").setText("SR300");
					DoorContinuousInfo.getChild("FaceImageAcquisitionBrand").setText("Intel");
				}
			}

			if (moduleCode.equals("003")) {
				if (statusCode == 1) { // 异常
					Element DoorContinuousInfo = FaultInf.getChild("QRCodeInfo");
					DoorContinuousInfo.getChild("QRCodeID").setText("");
					DoorContinuousInfo.getChild("QRCodeModel").setText("");
					DoorContinuousInfo.getChild("QRCodeBrand").setText("");
				} else {
					Element DoorContinuousInfo = FaultInf.getChild("QRCodeInfo");
					DoorContinuousInfo.getChild("QRCodeID").setText("10102");
					DoorContinuousInfo.getChild("QRCodeModel").setText("7580g");
					DoorContinuousInfo.getChild("QRCodeBrand").setText("Honeywell");
				}
			}

			if (moduleCode.equals("004")) {
				if (statusCode == 1) { // 异常
					Element DoorContinuousInfo = FaultInf.getChild("IDCardReaderInfo");
					DoorContinuousInfo.getChild("IDCardReaderID").setText("");
					DoorContinuousInfo.getChild("IDCardReaderModel").setText("");
					DoorContinuousInfo.getChild("IDCardReaderBrand").setText("");
				} else {
					Element DoorContinuousInfo = FaultInf.getChild("IDCardReaderInfo");
					DoorContinuousInfo.getChild("IDCardReaderID").setText(DeviceConfig.getInstance().getIDCardReaderID());
					DoorContinuousInfo.getChild("IDCardReaderModel").setText("SD2000-1");
					DoorContinuousInfo.getChild("IDCardReaderBrand").setText("ZKXA");
				}
			}

			saveXML(document, fileName);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	/**
	 * 更新开门次数
	 * 
	 * @param fileName
	 */
	public static void updateFlapTotalForMonitor(String fileName, int FlapTotal) {
		log.info("更新开门次数==" + FlapTotal);
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(fileName));

			Element FaultInf = document.getRootElement();

			Element DoorContinuous = FaultInf.getChild("DoorContinuous");

			DoorContinuous.getChild("FlapTotal").setText(String.valueOf(FlapTotal));
			DoorContinuous.getChild("FlapUpdTime").setText(CalUtils.getStringDate());

			saveXML(document, fileName);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	/**
	 * 更新总体状态
	 * 
	 * @param fileName
	 * @param status
	 */
	public static void updateEntirStatusForMonitor(String fileName, int status) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(fileName));

			Element FaultInf = document.getRootElement();

			Element DoorContinuous = FaultInf.getChild("StatusInfo");

			DoorContinuous.getChild("StatusInf").setText(String.valueOf(status));
			DoorContinuous.getChild("MaintainsDateTime").setText(CalUtils.getStringDate());

			saveXML(document, fileName);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	/**
	 * 更新部件状态
	 * 
	 * @param fileName
	 * @param companyCode
	 * @param machineCode
	 * @param moduleCode
	 * @param errCode
	 */
	public static void updateDoorStatusForMonitor(String fileName, String companyCode, String machineCode, String moduleCode, String errCode) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(fileName));

			Element FaultInf = document.getRootElement();

			String faultCode = "";
			if (errCode != null && !errCode.equals("")) {
				faultCode = companyCode + "001" + machineCode + moduleCode + errCode;
			}

			if (moduleCode.equals("001")) {
				Element DoorContinuous = FaultInf.getChild("DoorContinuous");
				DoorContinuous.getChild("FaultCode").setText(faultCode);
				DoorContinuous.getChild("MaintainsDateTime").setText(CalUtils.getStringDate());
			}

			if (moduleCode.equals("002")) {
				Element FaceImageAcquisition = FaultInf.getChild("FaceImageAcquisition");
				FaceImageAcquisition.getChild("FaultCode").setText(faultCode);
				FaceImageAcquisition.getChild("MaintainsDateTime").setText(CalUtils.getStringDate());
			}

			if (moduleCode.equals("003")) {
				Element QRCode = FaultInf.getChild("QRCode");
				QRCode.getChild("FaultCode").setText(faultCode);
				QRCode.getChild("MaintainsDateTime").setText(CalUtils.getStringDate());
			}

			if (moduleCode.equals("004")) {
				Element IDCardReader = FaultInf.getChild("IDCardReader");
				IDCardReader.getChild("FaultCode").setText(faultCode);
				IDCardReader.getChild("MaintainsDateTime").setText(CalUtils.getStringDate());
			}

			saveXML(document, fileName);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	/**
	 * 
	 * @param doc
	 */
	public static void saveXML(Document doc, String fileName) {
		// 将doc对象输出到文件
		try {
			// 创建xml文件输出流
			XMLOutputter xmlopt = new XMLOutputter();

			// 创建文件输出流
			FileWriter writer = new FileWriter(fileName);

			// 指定文档格式
			Format fm = Format.getPrettyFormat();
			fm.setEncoding("GB2312");
			xmlopt.setFormat(fm);

			// 将doc写入到指定的文件中
			xmlopt.output(doc, writer);
			writer.close();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String baseInfofileName = "C:/SmartMonitor/RealNameGate/BaseInfo.xml";
		try {
			MonitorXMLUtil.createBaseInfoXml(baseInfofileName, "001");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String statusInfofileName = "C:/SmartMonitor/RealNameGate/StatusInfo.xml";
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("StatusInf", "0");
			map.put("MaintainsDateTime", CalUtils.getStringDate());
			map.put("FlapTotal", "23");
			map.put("FlapUpdTime", CalUtils.getStringDate());
			map.put("DoorFaultCode", "");
			map.put("DoorMaintainsDateTime", CalUtils.getStringDate());
			map.put("FaceFaultCode", "");
			map.put("FaceMaintainsDateTime", CalUtils.getStringDate());
			map.put("IDCardFaultCode", "");
			map.put("IDCardMaintainsDateTime", CalUtils.getStringDate());
			map.put("QRCodeFaultCode", "");
			map.put("QRCodeMaintainsDateTime", CalUtils.getStringDate());
			MonitorXMLUtil.createStatusInfoXml(statusInfofileName, map);
			
			System.out.println(""+MonitorXMLUtil.getFlapCountFromStatusXML(Config.getInstance().getStatusInfoXMLPath()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

//		CommUtil.sleep(3000);
//
//		MonitorXMLUtil.updateFlapTotalForMonitor(Config.getInstance().getStatusInfoXMLPath(), 230);
//
//		MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "001", "");
//
//		MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "");
//
//		MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "003", "");
//
//		MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "004", "");
	}

}
