package com.rxtec.pitchecking.picheckingservice;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceImageLog {

	public static void saveFaceDataToDsk(FaceVerifyData fd) {
		String dirName = Config.getInstance().getImagesLogDir();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dirName += formatter.format(new Date());
		String trackedDir = dirName + "/Tracked";
		String passedDir = dirName + "/Passed";
		String failedDir = dirName + "/Failed";

		if (fd.getVerifyResult() == 0) {
			saveFrameImage(trackedDir,fd);
			saveFaceImage(trackedDir,fd);
		} else if (fd.getVerifyResult() >= Config.getInstance().getFaceCheckThreshold()) {
			saveFrameImage(passedDir,fd);
			saveFaceImage(passedDir,fd);
			saveIDCardImage(passedDir,fd);
		} else {
			saveFrameImage(failedDir,fd);
			saveFaceImage(failedDir,fd);
			saveIDCardImage(failedDir,fd);

		}
	}

	private static void saveFrameImage(String dirName, FaceVerifyData fd) {
		if (fd == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");

			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF");
			if (fd.getFaceID() != null)
				sb.append(fd.getFaceID().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append(".jpg");
			String fn = sb.toString();
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFrameImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private static void saveFaceImage(String dirName, FaceVerifyData fd) {
		if (fd == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
			df.setMaximumFractionDigits(2);
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/FI");
			if (fd.getFaceID() != null)
				sb.append(fd.getFaceID().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append("$");
			sb.append(df.format(fd.getVerifyResult()));
			sb.append(".jpg");
			String fn = sb.toString();

			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFaceImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private static void saveIDCardImage(String dirName, FaceVerifyData fd) {
		if (fd == null || fd.getIdCardImg() == null)
			return;

		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/ID");
			sb.append(fd.getFaceID().hashCode());
			sb.append(".jpg");
			String fn = sb.toString();
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getIdCardImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
