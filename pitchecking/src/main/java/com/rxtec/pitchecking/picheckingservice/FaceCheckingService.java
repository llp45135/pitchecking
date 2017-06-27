package com.rxtec.pitchecking.picheckingservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.task.FrontTrackPidDetectTask;
import com.rxtec.pitchecking.task.LuminanceListenerTask;
import com.rxtec.pitchecking.task.MainPidDetectTask;
import com.rxtec.pitchecking.task.QueryUPSStatusTask;
import com.rxtec.pitchecking.task.ReadLightLevelTask;
import com.rxtec.pitchecking.task.RunSetCameraPropJob;
import com.rxtec.pitchecking.task.StandaloneCheckDetectTask;
import com.rxtec.pitchecking.task.StandaloneCheckHeartTask;
import com.rxtec.pitchecking.task.TrackingPidDetectTask;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceCheckingService {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");

	private boolean isCheck = true;

	private static FaceCheckingService _instance = new FaceCheckingService();

	private FaceVerifyInterface faceVerify = null;

	// 已经检测人脸质量，待验证的队列
	private LinkedBlockingQueue<PITData> detectedFaceDataQueue;

	// 待验证的队列,独立进程比对
	private LinkedBlockingQueue<PITVerifyData> faceVerifyDataQueue;

	// 比对验证通过的队列
	private LinkedBlockingQueue<PITVerifyData> passFaceDataQueue;

	// 比对未通过验证的List，按照分值排序，分值高的在前
	private List<PITVerifyData> failedFaceDataList;

	private PITVerifyData failedFace = null;

	private boolean isSubscribeVerifyResult = true; // 是否订阅比对后的人脸结果
	private boolean isFrontCamera = false; // 是否前置摄像头
	private int idCardPhotoRet = -1;
	private IDCard idcard = null;
	private IDCard lastIdCard = null;
	private boolean isDealNoneTrackFace = false; // 是否允许处理非检脸状态下送来的人脸

	private boolean isSendFrontCameraFace = true; // 是否送前置摄像头人脸

	private String pid = "";

	private boolean isReceiveBehindCameraFace = false; // 是否接收到后置摄像头人脸

	private boolean isVerifyFace = true;

	public boolean isVerifyFace() {
		return isVerifyFace;
	}

	public void setVerifyFace(boolean isVerifyFace) {
		this.isVerifyFace = isVerifyFace;
	}

	public boolean isDealNoneTrackFace() {
		return isDealNoneTrackFace;
	}

	public void setDealNoneTrackFace(boolean isDealNoneTrackFace) {
		this.isDealNoneTrackFace = isDealNoneTrackFace;
	}

	public boolean isReceiveBehindCameraFace() {
		return isReceiveBehindCameraFace;
	}

	public void setReceiveBehindCameraFace(boolean isReceiveBehindCameraFace) {
		this.isReceiveBehindCameraFace = isReceiveBehindCameraFace;
	}

	public boolean isSendFrontCameraFace() {
		return isSendFrontCameraFace;
	}

	public void setSendFrontCameraFace(boolean isSendFrontCameraFace) {
		this.isSendFrontCameraFace = isSendFrontCameraFace;
	}

	public IDCard getLastIdCard() {
		return lastIdCard;
	}

	public void setLastIdCard(IDCard lastIdCard) {
		this.lastIdCard = lastIdCard;
	}

	public void clearLastIdCard() {
		this.lastIdCard = null;
	}

	public IDCard getIdcard() {
		return idcard;
	}

	/**
	 * 当读到身份证后的处理
	 * 
	 * @param idcard
	 */
	public void setIdcard(IDCard idcard) {
		if (idcard != null) {
			for (PITVerifyData pvd : faceVerifyDataQueue) {
				pvd.setIdCard(idcard);
				
				pvd.setIdCardImg(idcard.getCardImageBytes());
				
				pvd.setIdNo(idcard.getIdNo());
				pvd.setAge(idcard.getAge());
				pvd.setPersonName(idcard.getPersonName());
				pvd.setGender(idcard.getGender());
				pvd.setIdBirth(idcard.getIDBirth());
				pvd.setIdNation(idcard.getIDNation());
				pvd.setIdDwelling(idcard.getIDDwelling());
				pvd.setIdEfficb(idcard.getIDEfficb());
				pvd.setIdEffice(idcard.getIDEffice());
				pvd.setIdIssue(idcard.getIDIssue());
				log.debug("######Set IDCard######");
			}
			this.idcard = idcard;
		}
	}

	public void resetIdCard() {
		this.idcard = null;
	}

	public int getIdCardPhotoRet() {
		return idCardPhotoRet;
	}

	public void setIdCardPhotoRet(int idCardPhotoRet) {
		this.idCardPhotoRet = idCardPhotoRet;
	}

	public boolean isFrontCamera() {
		return isFrontCamera;
	}

	public void setFrontCamera(boolean isFrontCamera) {
		this.isFrontCamera = isFrontCamera;
	}

	public boolean isSubscribeVerifyResult() {
		return isSubscribeVerifyResult;
	}

	public void setSubscribeVerifyResult(boolean isSubscribeVerifyResult) {
		this.isSubscribeVerifyResult = isSubscribeVerifyResult;
	}

	public PITVerifyData getFailedFace() {
		return failedFace;
	}

	public void setFailedFace(PITVerifyData failedFace) {
		this.failedFace = failedFace;
	}

	public void putFailedFace(PITVerifyData failedFace) {
		try {
			if (this.failedFace == null) {
				this.failedFace = failedFace;
				this.failedFace.setIdCardImg(failedFace.getIdCardImg());
				this.failedFace.setFaceImg(failedFace.getFaceImg());
				this.failedFace.setFrameImg(failedFace.getFrameImg());

				log.info("failedFd:IdCardImg.length = " + failedFace.getIdCardImg().length + ",faceImg.length = " + failedFace.getFaceImg().length + ",frameImg.length = "
						+ failedFace.getFrameImg().length);

				this.failedFace.setIdCardImgByBase64(BASE64Util.encryptBASE64(failedFace.getIdCardImg()));
				this.failedFace.setFaceImgByBase64(BASE64Util.encryptBASE64(failedFace.getFaceImg()));
				this.failedFace.setFrameImgByBase64(BASE64Util.encryptBASE64(failedFace.getFrameImg()));

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LinkedBlockingQueue<PITVerifyData> getPassFaceDataQueue() {
		return passFaceDataQueue;
	}

	private FaceCheckingService() {
		detectedFaceDataQueue = new LinkedBlockingQueue<PITData>(Config.getInstance().getDetectededFaceQueueLen());
		faceVerifyDataQueue = new LinkedBlockingQueue<PITVerifyData>(Config.getInstance().getDetectededFaceQueueLen());
		passFaceDataQueue = new LinkedBlockingQueue<PITVerifyData>(1);
		failedFaceDataList = new LinkedList<PITVerifyData>();
	}

	public static synchronized FaceCheckingService getInstance() {
		if (_instance == null)
			_instance = new FaceCheckingService();
		return _instance;
	}

	public void setFaceVerify(FaceVerifyInterface faceVerify) {
		this.faceVerify = faceVerify;
	}

	public FaceVerifyInterface getFaceVerify() {
		return faceVerify;
	}

	// 从阻塞队列中返回比对成功的人脸
	public PITVerifyData pollPassFaceData(int delaySeconds) throws InterruptedException {
		PITVerifyData fd = passFaceDataQueue.poll(delaySeconds, TimeUnit.SECONDS);
		return fd;
	}

	// 返回人脸比对分值最高的,比对未通过的人脸数据
	public PITVerifyData pollFailedFaceData() {
		if (failedFaceDataList.size() > 0)
			return failedFaceDataList.get(0);
		return null;
	}

	public PITData takeDetectedFaceData() throws InterruptedException {
		PITData p = detectedFaceDataQueue.take();
		log.debug("detectedFaceDataQueue length=" + detectedFaceDataQueue.size());

		return p;
	}

	/**
	 * 从待验证人脸队列中取人脸对象
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public PITVerifyData takeFaceVerifyData() throws InterruptedException {
		PITVerifyData v = faceVerifyDataQueue.take();
		log.debug("takeFaceVerifyData faceVerifyDataQueue length=" + faceVerifyDataQueue.size());
		return v;
	}

	// Offer 比对成功的人脸到阻塞队列
	public boolean offerPassFaceData(PITVerifyData fd) {
		isCheck = passFaceDataQueue.offer(fd);
		log.info("offerPassFaceData...... " + isCheck);
		return isCheck;
	}

	// 添加比对失败的人脸到队列，按照比对比对结果分值排序
	public void offerFailedFaceData(PITVerifyData fd) {
		log.debug("offerFailedFaceData list length= " + failedFaceDataList.size());
		failedFaceDataList.add(fd);
		Collections.sort(failedFaceDataList);
	}

	/**
	 * 将人脸插入待检队列 人脸检测进程使用，检测到人脸即调用
	 * 
	 * @param faceData
	 */
	public void offerDetectedFaceData(PITData faceData) {
		if (!detectedFaceDataQueue.offer(faceData)) {
			detectedFaceDataQueue.poll();
			detectedFaceDataQueue.offer(faceData);
		}

		PITVerifyData vd = new PITVerifyData(faceData);

		if (!this.isFrontCamera) { // 后置摄像头
			if (vd.getIdCardImg() != null && vd.getFrameImg() != null && vd.getFaceImg() != null) {

				if (!faceVerifyDataQueue.offer(vd)) {
					faceVerifyDataQueue.poll();
					faceVerifyDataQueue.offer(vd);
				}
				// log.info("faceVerifyDataQueue==" + faceVerifyDataQueue.size()
				// + ",FaceCheckingService.getInstance().getIdcard()=="
				// + FaceCheckingService.getInstance().getIdcard());
			} else {
				log.error("后置摄像头：offerDetectedFaceData 输入数据不完整！ vd.getIdCardImg()=" + vd.getIdCardImg() + " vd.getFrameImg()=" + vd.getFrameImg() + " vd.getFaceImg()="
						+ vd.getFaceImg());
			}
		} else { // 前置摄像头
			if (vd.getFrameImg() != null && vd.getFaceImg() != null) {

				if (!faceVerifyDataQueue.offer(vd)) {
					faceVerifyDataQueue.poll();
					faceVerifyDataQueue.offer(vd);
				}
			} else {
				log.error("前置摄像头：offerDetectedFaceData 输入数据不完整！ vd.getIdCardImg()=" + vd.getIdCardImg() + " vd.getFrameImg()=" + vd.getFrameImg() + " vd.getFaceImg()="
						+ vd.getFaceImg());
			}
		}
	}

	/**
	 * 
	 * @param faceDatas
	 */
	public void offerDetectedFaceData(List<PITData> faceDatas) {

		ArrayList<PITVerifyData> newList = new ArrayList<PITVerifyData>();
		Iterator<PITVerifyData> iterQueue = faceVerifyDataQueue.iterator();
		Iterator<PITData> iterFaceDatas = faceDatas.iterator();

		log.debug("faceDatas size = " + faceDatas.size() + ",faceVerifyDataQueue.size==" + faceVerifyDataQueue.size());
		if (faceVerifyDataQueue.size() < 1) {
			for (PITData pid : faceDatas) {
				PITVerifyData pvdNew = new PITVerifyData(pid);
				faceVerifyDataQueue.add(pvdNew);
			}

		} else {
			while (iterFaceDatas.hasNext()) {
				PITData pid = iterFaceDatas.next();
				while (iterQueue.hasNext()) {
					PITVerifyData pvdOld = iterQueue.next();
					if (pid.getFaceQuality() > pvdOld.getFaceQuality()) {
						iterQueue.remove();
						log.debug("......faceVerifyDataQueue.size==" + faceVerifyDataQueue.size() + " new Pid Quality=" + pid.getFaceQuality() + " pvdOld quality="
								+ pvdOld.getFaceQuality());
					} else {
						iterFaceDatas.remove();
						log.debug("!!!!!!!!!!faceDatas.size==" + faceDatas.size() + " new Pid Quality=" + pid.getFaceQuality() + " pvdOld quality="
								+ pvdOld.getFaceQuality());
					}
				}

			}

			for (PITData pid : faceDatas) {
				PITVerifyData pvdNew = new PITVerifyData(pid);
				faceVerifyDataQueue.offer(pvdNew);
			}
			log.debug("faceVerifyDataQueue.size==" + faceVerifyDataQueue.size());

		}
	}

	/**
	 * 将人脸数据插入待验证队列 人脸比对进程中，由aeron订阅端使用
	 * 
	 * @param faceData
	 */
	public void offerFaceVerifyData(PITVerifyData faceData) {
		if (!faceVerifyDataQueue.offer(faceData)) {
			faceVerifyDataQueue.poll();
			faceVerifyDataQueue.offer(faceData);
		}
		log.debug("offerFaceVerifyData faceVerifyDataQueue length=" + faceVerifyDataQueue.size());
	}

	/**
	 * 
	 */
	public void resetFaceDataQueue() {
		detectedFaceDataQueue.clear();
		faceVerifyDataQueue.clear();
		passFaceDataQueue.clear();
		failedFaceDataList.clear();
	}

	/**
	 * 
	 */
	public void clearPassFaceDataQueue() {
		passFaceDataQueue.clear();
		failedFaceDataList.clear();
	}

	/**
	 * 
	 */
	public void clearFaceVerifyQueue() {
		detectedFaceDataQueue.clear();
		faceVerifyDataQueue.clear();
	}

	ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * 启动人脸比对 本函数由独立人脸检测进程执行
	 */
	public void beginFaceCheckerTask() {
		/**
		 * 启动独立人脸比对进程的结果订阅
		 */
		if (this.isSubscribeVerifyResult) {
			if (Config.getInstance().getFaceControlMode() == 1) {
				PIVerifyResultSubscriber.getInstance().startSubscribing();
			} else {
				log.info("后置摄像头进程不订阅验证结果，不处理该逻辑");
			}
		} else {
			log.info("前置摄像头模式不订阅验证结果，不处理该逻辑");
		}
		/**
		 * 启动向人脸比对进程发布待验证人脸的发布者
		 */
		PTVerifyPublisher.getInstance();
	}

	/**
	 * 单独比对人脸进程Task，通过共享内存通信 此函数由独立人脸比对进程执行
	 */
	public void beginFaceCheckerStandaloneTask() {
		ExecutorService executer = Executors.newCachedThreadPool();
		FaceCheckingStandaloneTask task1 = new FaceCheckingStandaloneTask();
		executer.execute(task1);
		if (Config.getInstance().getFaceVerifyThreads() == 2) {
			FaceCheckingStandaloneTask task2 = new FaceCheckingStandaloneTask();
			executer.execute(task2);
		}
		log.debug(".............Start " + Config.getInstance().getFaceVerifyThreads() + " FaceVerifyThreads");
		log.debug("softVersion==" + DeviceConfig.softVersion);
		executer.shutdown();
	}

	/**
	 * 
	 */
	public void startupApp() {
		try {
			Runtime.getRuntime().exec(Config.getInstance().getStartPITTrackCmd());
			CommUtil.sleep(2000);
			if (Config.getInstance().getIsUseGatDll() == 0)
				Runtime.getRuntime().exec(Config.getInstance().getStartPITVerifyCmd());
			else {
				Runtime.getRuntime().exec("D:/pitchecking/work/StartGui.bat");
			}
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			log.error("startupApp:", ex);
		}
	}

	/**
	 * 人脸比对进程使用-删除心跳日志
	 */
	public void deleteHeartLog() {
		try {
			File file = new File(Config.getInstance().getHeartBeatLogFile());
			if (file.exists()) {
				file.delete();
			}
			file = new File(Config.getInstance().getFrontHeartBeatLogFile());
			if (file.exists()) {
				file.delete();
			}
			file = new File(Config.getInstance().getTicketVerifyHeartFile());
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception ex) {
			log.error("deleteHeartLog:", ex);
		}
	}

	/**
	 * 
	 * @return
	 */
	public int addStandaloneQuartzJobs() {
		int retVal = 1;
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器

			JobDetail job1 = JobBuilder.newJob(StandaloneCheckHeartTask.class).withIdentity("StandaloneCheckHeartJob", "pitcheckGroup").build();
			CronTrigger trigger1 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("StandaloneCheckHeartTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")).build(); // 设置触发器
			Date ft1 = sched.scheduleJob(job1, trigger1); // 设置调度作业
			log.debug(job1.getKey() + " has been scheduled to run at: " + ft1 + " and repeat based on expression: " + trigger1.getCronExpression());

			sched.start(); // 开启调度任务，执行作业
		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 人脸比对进程使用
	 * 
	 * @return
	 */
	public int addPidProtectQuartzJobs() {
		int retVal = 1;
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器

			/**
			 * ups状态监控
			 */
			if (Config.getInstance().getIsUseUPSDevice() == 1) {
				JobDetail job1 = JobBuilder.newJob(QueryUPSStatusTask.class).withIdentity("QueryUPSStatusJob", "queryUPSGroup").build();
				CronTrigger trigger1 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("QueryUPSStatusTrigger", "queryUPSGroup")
						.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getQueryUPSCronStr())).build(); // 设置触发器

				Date ft1 = sched.scheduleJob(job1, trigger1); // 设置调度作业
				log.debug(job1.getKey() + " has been scheduled to run at: " + ft1 + " and repeat based on expression: " + trigger1.getCronExpression());
			}

			/**
			 * java主控进程监控
			 */
			JobDetail job2 = JobBuilder.newJob(MainPidDetectTask.class).withIdentity("MainPidDetectJob", "queryUPSGroup").build();
			CronTrigger trigger2 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("MainPidDetectTrigger", "queryUPSGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")).build(); // 设置触发器

			Date ft2 = sched.scheduleJob(job2, trigger2); // 设置调度作业
			log.debug(job2.getKey() + " has been scheduled to run at: " + ft2 + " and repeat based on expression: " + trigger2.getCronExpression());

			/**
			 * 后置摄像头进程监控
			 */
			if (Config.getInstance().getIsCheckBackCameraHeart() == 1) {
				JobDetail job3 = JobBuilder.newJob(TrackingPidDetectTask.class).withIdentity("TrackingPidDetectJob", "queryUPSGroup").build();
				CronTrigger trigger3 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("TrackingPidDetectTrigger", "queryUPSGroup")
						.withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?")).build(); // 设置触发器

				Date ft3 = sched.scheduleJob(job3, trigger3); // 设置调度作业
				log.debug(job3.getKey() + " has been scheduled to run at: " + ft3 + " and repeat based on expression: " + trigger3.getCronExpression());

			}
			/**
			 * 前置摄像头进程监控
			 */
			JobDetail job4 = JobBuilder.newJob(FrontTrackPidDetectTask.class).withIdentity("FrontTrackPidDetectJob", "queryUPSGroup").build();
			CronTrigger trigger4 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("FrontTrackPidDetectTrigger", "queryUPSGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?")).build(); // 设置触发器

			Date ft4 = sched.scheduleJob(job4, trigger4); // 设置调度作业
			log.debug(job4.getKey() + " has been scheduled to run at: " + ft4 + " and repeat based on expression: " + trigger4.getCronExpression());

			/**
			 * 独立比对进程监控
			 */
			if (Config.getInstance().getIsCheckStandaloneHeart() == 1) {
				JobDetail job5 = JobBuilder.newJob(StandaloneCheckDetectTask.class).withIdentity("StandaloneCheckDetectJob", "queryUPSGroup").build();
				CronTrigger trigger5 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("StandaloneCheckDetectTrigger", "queryUPSGroup")
						.withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?")).build(); // 设置触发器

				Date ft5 = sched.scheduleJob(job5, trigger5); // 设置调度作业
				log.debug(job5.getKey() + " has been scheduled to run at: " + ft5 + " and repeat based on expression: " + trigger5.getCronExpression());
			}

			/**
			 * 自动注销
			 */
			JobDetail job6 = JobBuilder.newJob(AutoLogonJob.class).withIdentity("AutoLogonJob", "queryUPSGroup").build();
			CronTrigger trigger6 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("AutoLogonTrigger", "queryUPSGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(DeviceConfig.getInstance().getAutoLogonCron())).build(); // 设置触发器

			Date ft6 = sched.scheduleJob(job6, trigger6); // 设置调度作业
			log.debug(job6.getKey() + " has been scheduled to run at: " + ft6 + " and repeat based on expression: " + trigger6.getCronExpression());

			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 
	 */
	public void noticeCameraStatus() {
		pid = ProcessUtil.getCurrentProcessID();
		if (!FaceCheckingService.getInstance().isFrontCamera()) { // 后置摄像头
			// ProcessUtil.writeHeartbeat(pid,
			// Config.getInstance().getHeartBeatLogFile()); // 写心跳日志
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).sendMessage(DeviceConfig.EventTopic,
					DeviceConfig.getInstance().getHeartStr(pid, "B"));

			Config.getInstance().setCameraWork(true);
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).sendDoorCmd("PITEventTopic",
					DeviceConfig.Event_CameraStartupSucc); // 通知比对线程，后置摄像头已经启动成功
		} else { // 前置摄像头
			// ProcessUtil.writeHeartbeat(pid,
			// Config.getInstance().getFrontHeartBeatLogFile()); // 写心跳日志

			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).sendMessage(DeviceConfig.EventTopic,
					DeviceConfig.getInstance().getHeartStr(pid, "F"));

			Config.getInstance().setFrontCameraWork(true);
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).sendDoorCmd("PITEventTopic",
					DeviceConfig.Event_FrontCameraStartupSucc); // 通知比对线程，前置摄像头已经启动成功
		}
	}

	/**
	 * 定时启动任务 检脸进程使用
	 */
	public int addQuartzJobs() {
		int retVal = 1;
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器

			JobDetail job1 = JobBuilder.newJob(RunSetCameraPropJob.class).withIdentity("setCameraPropJob1", "pitcheckGroup").build();
			CronTrigger trigger1 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("setCameraPropTrigger1", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getInitCronStr())).build(); // 设置触发器
			Date ft1 = sched.scheduleJob(job1, trigger1); // 设置调度作业
			log.debug(job1.getKey() + " has been scheduled to run at: " + ft1 + " and repeat based on expression: " + trigger1.getCronExpression());

			JobDetail job2 = JobBuilder.newJob(RunSetCameraPropJob.class).withIdentity("setCameraPropJob2", "pitcheckGroup").build();
			CronTrigger trigger2 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("setCameraPropTrigger2", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getNightCronStr())).build(); // 设置触发器
			Date ft2 = sched.scheduleJob(job2, trigger2); // 设置调度作业
			log.debug(job2.getKey() + " has been scheduled to run at: " + ft2 + " and repeat based on expression: " + trigger2.getCronExpression());

			if (Config.getInstance().getIsUseLightLevelDevice() == 1) { // 是否使用光照监测器
				JobDetail job3 = JobBuilder.newJob(ReadLightLevelTask.class).withIdentity("readLightLevelJob", "pitcheckGroup").build();
				CronTrigger trigger3 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("readLightLevelTrigger", "pitcheckGroup")
						.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getReadLightLevelCronStr())).build(); // 设置触发器

				Date ft3 = sched.scheduleJob(job3, trigger3); // 设置调度作业
				log.debug(job3.getKey() + " has been scheduled to run at: " + ft3 + " and repeat based on expression: " + trigger3.getCronExpression());
			}

			if (Config.getInstance().getIsUseLuminanceListener() == 1) { // 是否监测照片的luminance
				JobDetail job4 = JobBuilder.newJob(LuminanceListenerTask.class).withIdentity("readLuminanceJob", "pitcheckGroup").build();
				CronTrigger trigger4 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("readreadLuminanceTrigger", "pitcheckGroup")
						.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getQueryLuminanceCronStr())).build(); // 设置触发器

				Date ft4 = sched.scheduleJob(job4, trigger4); // 设置调度作业
				log.debug(job4.getKey() + " has been scheduled to run at: " + ft4 + " and repeat based on expression: " + trigger4.getCronExpression());
			}

			sched.start(); // 开启调度任务，执行作业
		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

}
