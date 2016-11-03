package com.rxtec.pitchecking.picheckingservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.PitRecordLoger;
import com.rxtec.pitchecking.db.mysql.PitRecordSqlLoger;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.mbean.PITProcessDetect;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.task.ManualCheckingTask;

/**
 * 单独比对人脸进程
 * 
 * @author ZhaoLin
 *
 */
public class PITCheckingStandaloneService {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		// 启动检脸进程保护的线程
		ExecutorService executer = Executors.newSingleThreadExecutor();
		PITProcessDetect pitProcessDetect = new PITProcessDetect();
		executer.execute(pitProcessDetect);

		// MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		// FaceVerifyServiceManager mbean = new FaceVerifyServiceManager();
		// try {
		// server.registerMBean(mbean, new
		// ObjectName("PITCheck:type=FaceCheckingService,name=FaceCheckingService"));
		// } catch (InstanceAlreadyExistsException | MBeanRegistrationException
		// | NotCompliantMBeanException
		// | MalformedObjectNameException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// 是否启用monodb保存人脸数据
		if (Config.getInstance().getIsUseMongoDB() == 1) {
			PitRecordLoger.getInstance().clearExpirationData();
			PitRecordLoger.getInstance().startThread();
		}

		if (Config.getInstance().getIsUseMySQLDB() == 1) {
			PitRecordSqlLoger.getInstance().startThread();
		}

		int lightLedRet = -1;
		log.info("准备点亮补光灯");
		try {
			lightLedRet = LightControlBoard.getInstance().startLED(); // 点亮补光灯
		} catch (Exception ex) {
			log.error("PITCheckingStandaloneService:", ex);
		}
		log.info("点亮补光灯,lightLedRet==" + lightLedRet);

		// 语音调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

		AudioPlayTask.getInstance().start(DeviceConfig.AudioUseHelpFlag); // 循环播放引导使用语音

		if (Config.getInstance().getIsUseManualMQ() == 1) {// 是否连人工窗控制台
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ManualCheckingTask manualCheckingTask = new ManualCheckingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			executorService.execute(manualCheckingTask);
		}

		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);// 启动PITEventTopic本地监听

		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		PIVerifySubscriber piVerifySubscriber = new PIVerifySubscriber();

	}

}
