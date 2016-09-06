package com.rxtec.pitchecking;

import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public interface IVerifyFaceTask {
	public PITVerifyData beginCheckFace(IDCard idCard, Ticket ticket,int delaySeconds);
}
