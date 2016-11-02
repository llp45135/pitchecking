package com.rxtec.pitchecking.net;


import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.utils.BASE64;
import com.rxtec.pitchecking.utils.ImageToolkit;
import com.rxtec.pitchecking.utils.JsonUtils;


public class PitinfoUPDBuilder {

	public static String buildPITDataJsonBytes(PITData pitData) {
		if (pitData == null)
			return null;
		String buff = null;
		PITInfoJson jsonObj = new PITInfoJson();
		if (pitData != null) {
			IDCard idCard = pitData.getIdCard();
			if (idCard != null) {
				jsonObj.setGender(idCard.getGender());
				jsonObj.setAge(idCard.getAge());
				jsonObj.setIdHashCode(idCard.getIdNo().hashCode());
				String s1;
				try {
					jsonObj.setFrameImageBase64(BASE64.encryptBASE64(ImageToolkit.getImageBytes(pitData.getFrame(),"jpeg")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					jsonObj.setIdPicImageBase64(BASE64.encryptBASE64(ImageToolkit.getImageBytes(idCard.getCardImage(),"jpeg")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jsonObj.setSimilarity(pitData.getFaceCheckResult());
			}
		    
		}
		
		//InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(buff),"UTF-8");
		
		return JsonUtils.serialize(jsonObj);
	}


}
