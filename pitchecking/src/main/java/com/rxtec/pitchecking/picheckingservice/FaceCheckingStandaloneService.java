package com.rxtec.pitchecking.picheckingservice;

import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceCheckingStandaloneService {

	public static void main(String[] args) {
		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		CommUtil.sleep(3000);
		PIVerifySubscriber s = new PIVerifySubscriber();
	}

	
	
}
