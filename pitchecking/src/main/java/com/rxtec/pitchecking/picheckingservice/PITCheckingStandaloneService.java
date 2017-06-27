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
import com.rxtec.pitchecking.mqtt.ClosePCEventSenderBroker;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttCamResponseReceiver;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyReceiver;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyResultSender;
import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.socket.pitevent.FaceReceiver;
import com.rxtec.pitchecking.socket.pitevent.FaceResultReceiver;
import com.rxtec.pitchecking.socket.pitevent.OcxFaceReceiver;
import com.rxtec.pitchecking.task.ClosePCSendingTask;
import com.rxtec.pitchecking.task.ManualCheckingTask;
import com.rxtec.pitchecking.task.PoliceSendingTask;
import com.rxtec.pitchecking.task.ThirdSendingTask;
import com.rxtec.pitchecking.task.WharfSendingTask;

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
		// ExecutorService executer = Executors.newSingleThreadExecutor();
		// PITProcessDetect pitProcessDetect = new PITProcessDetect();
		// executer.execute(pitProcessDetect);

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

		// FaceCheckingService.getInstance().deleteHeartLog();
		// FaceCheckingService.getInstance().startupApp();

		// 是否启用monodb保存人脸数据
		if (Config.getInstance().getIsUseMongoDB() == 1) {
			PitRecordLoger.getInstance().clearExpirationData();
			PitRecordLoger.getInstance().startThread();
		}

		if (Config.getInstance().getIsUseMySQLDB() == 1) {
			PitRecordSqlLoger.getInstance().startThread();
		}

		if (Config.getInstance().getIsLightFaceLED() == 1) {
			int lightLedRet = -1;
			log.debug("准备点亮补光灯");
			try {
				lightLedRet = LightControlBoard.getInstance().startLED(); // 点亮补光灯
			} catch (Exception ex) {
				log.error("PITCheckingStandaloneService:", ex);
			}
			log.debug("点亮补光灯,lightLedRet==" + lightLedRet);
		}

		// 语音调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

		AudioPlayTask.getInstance().start(DeviceConfig.AudioUseHelpFlag); // 循环播放引导使用语音

		if (Config.getInstance().getIsUseManualMQ() == 1) {// 是否连人工窗控制台
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ManualCheckingTask manualCheckingTask = new ManualCheckingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			executorService.execute(manualCheckingTask);
		}

		if (Config.getInstance().getIsUsePoliceMQ() == 1) {// 是否连公安处
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			PoliceSendingTask policeSendingTask = new PoliceSendingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			executorService.execute(policeSendingTask);
		}

		if (Config.getInstance().getIsUseThirdMQ() == 1) {// 是否连第三方
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ThirdSendingTask thirdSendingTask = new ThirdSendingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			executorService.execute(thirdSendingTask);
		}
		
		if (Config.getInstance().getIsUseWharfMQ() == 1) {// 是否连桂林码头
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			WharfSendingTask wharfSendingTask = new WharfSendingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			executorService.execute(wharfSendingTask);
		}

		if (Config.getInstance().getFaceControlMode() == 2) { // 有人脸比对进程监听
			MqttReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT); // 同CAM_RXTa.dll通信			
		}

		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);// 启动PITEventTopic本地监听

		// if (Config.getInstance().getIsSendClosePCCmd() == 1) {
		// //是否需要向其他闸机发送同步关机命令
		// ExecutorService executorService =
		// Executors.newSingleThreadExecutor();
		// ClosePCSendingTask closePCSendingTask = new
		// ClosePCSendingTask(DeviceConfig.GAT_MQ_Standalone_CLIENT);
		// executorService.execute(closePCSendingTask);
		// }

		if (Config.getInstance().getIsCheckStandaloneHeart() == 1) {
			FaceCheckingService.getInstance().addStandaloneQuartzJobs();
		}

		GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd(DeviceConfig.Event_StandaloneCheckStartupSucc);

		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) { // Mqtt
			PTVerifyReceiver.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT); // 开启订阅由人脸检测进程发过来的人脸比对请求-By-Mqtt
		}
		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) { // Socket
			/**
			 * 人脸来源是realsense
			 */
			if (Config.getInstance().getSendFaceSourceBySocket() == 1)
				FaceReceiver.getInstance().startSubscribing(); // 开启订阅由人脸检测进程发过来的人脸比对请求-BySocket
			/**
			 * 人脸来源是ocx方式的usb摄像头
			 * 单门摄像头或者双门的后置摄像头
			 */
			if (Config.getInstance().getSendFaceSourceBySocket() == 2)
				OcxFaceReceiver.getInstance().startSubscribing();
		}
		FaceCheckingService.getInstance().beginFaceCheckerStandaloneTask();
		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByAeron) { // aeron
			PIVerifySubscriber piVerifySubscriber = new PIVerifySubscriber(); // 开启订阅由人脸检测进程发过来的人脸比对请求-By-aeron
		}
	}

}
