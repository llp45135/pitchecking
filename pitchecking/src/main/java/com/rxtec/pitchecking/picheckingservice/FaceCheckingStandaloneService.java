package com.rxtec.pitchecking.picheckingservice;

import com.rxtec.pitchecking.db.MongoDB;
import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 单独比对人脸进程
 * @author ZhaoLin
 *
 */
public class FaceCheckingStandaloneService {

	public static void main(String[] args) {
		MongoDB.getInstance().clearExpirationData();
		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		CommUtil.sleep(3000);
		PIVerifySubscriber s = new PIVerifySubscriber();
	}

	
	
}
