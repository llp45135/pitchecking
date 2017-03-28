package com.rxtec.pitchecking;

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
import com.rxtec.pitchecking.net.PIVerifySubscriber;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.task.ClosePCSendingTask;
import com.rxtec.pitchecking.task.ManualCheckingTask;
import com.rxtec.pitchecking.task.PoliceSendingTask;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 单独比对人脸进程
 * 
 * @author ZhaoLin
 *
 */
public class PIDProtectService {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		// 启动检脸进程保护的线程

		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_PidProtect_CLIENT);// 启动PITEventTopic本地监听

		if (Config.getInstance().getIsSendClosePCCmd() == 1) {  //是否需要向其他闸机发送同步关机命令
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ClosePCSendingTask closePCSendingTask = new ClosePCSendingTask(DeviceConfig.GAT_MQ_PidProtect_CLIENT);
			executorService.execute(closePCSendingTask);
		}

		FaceCheckingService.getInstance().addPidProtectQuartzJobs();


	}

}
