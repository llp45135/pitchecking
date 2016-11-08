package com.rxtec.pitchecking.task;

import java.io.File;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class ClearPITFaceFileJob implements Job {
	private Logger log = LoggerFactory.getLogger("PitRecordLoger");

	public ClearPITFaceFileJob() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			log.info("闸机定时重启任务,开始执行！ By " + jobContext.getJobDetail().getJobClass());
			this.deleteImageFile();
		} catch (Exception ex) {
			log.error("AutoLogonJob:", ex);
		}
	}

	/**
	 * 
	 */
	private void deleteImageFile() {
		File faceDir = new File(Config.getInstance().getImagesLogDir());
		if (faceDir.exists()) {
			String[] children = faceDir.list();
			for (int i = 0; i < children.length; i++) {
				log.debug("第" + i + "个==" + children[i]);
				int days = CalUtils.getCalcDateShort(children[i], CalUtils.getStringDateShort2());
				// log.debug("days==" + days);
				if (days >= 10) {
					log.debug("开始定时删除人脸文件夹:" + Config.getInstance().getImagesLogDir() + children[i]);
					CommUtil.deleteDir(new File(Config.getInstance().getImagesLogDir() + children[i]));
				}
			}
		}
	}

	public static void main(String[] args) {
		File faceDir = new File(Config.getInstance().getImagesLogDir());
		if (faceDir.exists()) {
			String[] children = faceDir.list();
			for (int i = 0; i < children.length; i++) {
				System.out.println("第" + i + "个==" + children[i]);
				int days = CalUtils.getCalcDateShort(children[i], CalUtils.getStringDateShort2());
				System.out.println("days==" + days);
				if (days >= 10) {
					CommUtil.deleteDir(new File(Config.getInstance().getImagesLogDir() + children[i]));
				}
			}
		}
	}

}
