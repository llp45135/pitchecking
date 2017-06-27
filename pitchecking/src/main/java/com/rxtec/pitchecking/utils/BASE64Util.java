package com.rxtec.pitchecking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.police.dao.PITInfoJmsObj;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/** * BASE64编码和解码 */
public class BASE64Util {
	/** * BASE64解码 * @param key * @return * @throws Exception */
	public static byte[] decryptBASE64(String key) throws Exception {
		return (new BASE64Decoder()).decodeBuffer(key);
	}

	/** * BASE64编码 * @param key * @return * @throws Exception */
	public static String encryptBASE64(byte[] key) throws Exception {
		return (new BASE64Encoder()).encodeBuffer(key);
	}

	public static void main(String[] args) throws Exception {
		String data = BASE64Util.encryptBASE64("520203197912141118".getBytes());
		System.out.println("编码后：" + data);
		byte[] byteArray = BASE64Util.decryptBASE64(data);
		System.out.println("解码后：" + new String(byteArray));

		// File file1 = new
		// File("D:/pitchecking/images/20161121/Passed/VF452224198701260520@2016-11-21
		// 03-06-18-076.jpg");
		// FileInputStream fis = new FileInputStream(file1);
		// byte[] inBuffer = new byte[(int) file1.length()];
		// fis.read(inBuffer);
		// fis.close();
		//
		// String indata = BASE64.encryptBASE64(inBuffer);
		// System.out.println("编码后,indata==" + indata);
		//
		// byte[] outBuffer = BASE64.decryptBASE64(indata);
		// File file2 = new File("D:/pitchecking/images/base64test.jpg");
		// FileOutputStream fos = new FileOutputStream(file2);
		// fos.write(outBuffer);
		// fos.flush();
		// fos.close();

		
		String pitInfoMsg = ProcessUtil
				.getRebackTrackAppFlag("D:/pitchecking/images/json/441226199401051422_121755.093.json");
//		System.out.println(""+CommUtil.getEncoding(pitInfoMsg));
//		pitInfoMsg = pitInfoMsg.replace("\\r\\n", "");
//		 System.out.println("pitInfoMsg=="+pitInfoMsg);

		ObjectMapper mapper = new ObjectMapper();
		PITInfoJmsObj pitInfoJsonBean = mapper.readValue(pitInfoMsg, PITInfoJmsObj.class);
		System.out.println("getMsgType==" + pitInfoJsonBean.getMsgType());
		System.out.println("getIdCardNo==" + pitInfoJsonBean.getIdCardNo());
		System.out.println("getCardNo==" + pitInfoJsonBean.getCardNo());
		System.out.println("getPassengerName==" + pitInfoJsonBean.getPassengerName());
		// System.out.println("getFaceImageBase64==" +
		// pitInfoJsonBean.getFaceImageBase64());
		// System.out.println("getFrameImageBase64==" +
		// pitInfoJsonBean.getFrameImageBase64());
		System.out.println("getIdPicImageBase64==" + pitInfoJsonBean.getIdPicImageBase64());

		PITVerifyData fd = new PITVerifyData();
		fd.setIdNo(new String(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdCardNo())));
		fd.setVerifyResult(pitInfoJsonBean.getSimilarity());
		fd.setFaceDistance((float) 1.3);
		fd.setFaceImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getFaceImageBase64()));
		fd.setFrameImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getFrameImageBase64()));
		fd.setIdCardImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdPicImageBase64()));
		fd.setPitDate(CalUtils.getStringDateShort2());
		fd.setPitStation("IZQ");
		fd.setAge(pitInfoJsonBean.getAge());
		fd.setGender(pitInfoJsonBean.getGender());
		fd.setGateIp(pitInfoJsonBean.getIpAddress());

		FaceImageLog.saveFaceDataToDsk(fd);
	}
}