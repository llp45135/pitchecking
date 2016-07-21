package com.rxtec.pitchecking.picheckingservice;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.rxtec.pitchecking.db.MongoDB;
import com.rxtec.pitchecking.mbean.FaceVerifyServiceManager;
import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 单独比对人脸进程
 * 
 * @author ZhaoLin
 *
 */
public class FaceCheckingStandaloneService {

	public static void main(String[] args) {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		FaceVerifyServiceManager mbean = new FaceVerifyServiceManager();
		try {
			server.registerMBean(mbean, new ObjectName("PITCheck:type=FaceCheckingService,name=FaceCheckingService"));
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
				| MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MongoDB.getInstance().clearExpirationData();
		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		CommUtil.sleep(3000);
		PIVerifySubscriber s = new PIVerifySubscriber();

	}

}
