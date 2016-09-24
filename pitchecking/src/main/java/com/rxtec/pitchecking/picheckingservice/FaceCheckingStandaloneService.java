package com.rxtec.pitchecking.picheckingservice;


import java.io.File;

import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.utils.CommUtil;

import io.aeron.driver.MediaDriver;


/**
 * 单独比对人脸进程
 * 
 * @author ZhaoLin
 *
 */
public class FaceCheckingStandaloneService {

	public static void main(String[] args) {
		//启动检脸进程保护的线程
//		ExecutorService executer = Executors.newSingleThreadExecutor();
//		PITProcessDetect pitProcessDetect = new PITProcessDetect();
//		executer.execute(pitProcessDetect);

//		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
//		FaceVerifyServiceManager mbean = new FaceVerifyServiceManager();
//		try {
//			server.registerMBean(mbean, new ObjectName("PITCheck:type=FaceCheckingService,name=FaceCheckingService"));
//		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
//				| MalformedObjectNameException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (Config.getInstance().getIsUseMongoDB() == 1) {
//			PitRecordLoger.getInstance().clearExpirationData();
//			PitRecordLoger.getInstance().startThread();
//		}
//		clearAeron();
//		final MediaDriver driver = MediaDriver.launch();
		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		CommUtil.sleep(3000);
		PIVerifySubscriber s = new PIVerifySubscriber();

	}
	
	private static void clearAeron(){
		String currentUser = System.getProperty("user.name");
		if(currentUser != null){
			String fileName = "C:/Users/" + currentUser + "/AppData/Local/Temp/aeron-"+currentUser+"/cnc.dat";
			File file = new File(fileName);
		    if (file.isFile() && file.exists()) {  
		    	file.delete();
		    }
		}
	}

}
