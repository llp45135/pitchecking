package com.rxtec.pitchecking.db.sqlserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.quickhigh.TrainInfomation;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.FtpUtil;

public class GetTrainInfoFromTxt {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public GetTrainInfoFromTxt() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		GetTrainInfoFromTxt gti = new GetTrainInfoFromTxt();
		gti.getTrainInfoFromTxtFile();
	}

	/**
	 * 
	 */
	private void getTrainTxtByFtp() {
		String ftpHost = Config.getInstance().getLvFuFtpAddress();// "10.168.72.60";
		String ftpUserName = Config.getInstance().getLvFuFtpUser();// "pitcheck";
		String ftpPassword = Config.getInstance().getLvFuFtpPwd();// "pitcheck";
		int ftpPort = 21;
		String ftpPath = "/";
		String localPath = "D:/pitchecking/config/";
		String fileName = "train_list.txt";
		FtpUtil.downloadFtpFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, localPath, fileName);
	}

	/**
	 * 
	 */
	public void getTrainInfoFromTxtFile() {
		this.getTrainTxtByFtp();

		String fileName = "";

		fileName = "D:/pitchecking/config/train_list.txt";
		log.info("fileName==" + fileName);
		if (!fileName.equals("")) {
			File file = new File(fileName);
			if (file.exists()) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(file));
					String tempString = null;
					int line = 1;
					while ((tempString = reader.readLine()) != null) {
						log.debug("line " + line + ": " + tempString);
						if (!tempString.trim().equals("")) {
							int k = tempString.indexOf("@"); // 第1个@
							int j = tempString.lastIndexOf("@"); // 最后一个@
							int t = tempString.indexOf("$");

							String stationCode = "";
							if (t > -1)
								stationCode = tempString.substring(j + 1, t);
							else
								stationCode = tempString.substring(j + 1);

							if (stationCode.equals(DeviceConfig.getInstance().getBelongStationCode())) {
								String trainCode = tempString.substring(0, k).startsWith("0") ? tempString.substring(1, k) : tempString.substring(0, k);
								String planDepartTime = tempString.substring(k + 1, k + 20);
								String DepartTime = tempString.substring(k + 21, k + 40);
								String state = tempString.substring(k + 41, j);

								String waitRoom = "";
								if (t > -1)
									waitRoom = tempString.substring(t + 1);

								TrainInfomation tis = new TrainInfomation();
								tis.setArriveTrainNo(trainCode);
								tis.setDepartTrainNo(trainCode);
								tis.setPlanDepartTime(planDepartTime);
								tis.setDepartTime(DepartTime);
								tis.setTrainStatus(state);
								tis.setWaitRoom(waitRoom);

								if (Config.getInstance().getIsGGQChaoshanMode() == 1) {
									if (trainCode.startsWith("D") && tis.getPlanDepartTime().substring(0, 10).equals(CalUtils.getStringDateShort())) {
										String trainKey = trainCode + tis.getPlanDepartTime().substring(0, 10);
										log.info("" + tis.getDepartTrainNo() + "次车当前状态 = " + tis.getTrainStatus() + "," + tis.getPlanDepartTime() + ","
												+ tis.getDepartTime() + "," + tis.getWaitRoom());
										DeviceEventListener.getInstance().getTrainInfoMap().put(trainKey, tis);
									}
								} else {
									String trainKey = trainCode + tis.getPlanDepartTime().substring(0, 10);
									log.info("" + tis.getDepartTrainNo() + "次车当前状态 = " + tis.getTrainStatus() + "," + tis.getPlanDepartTime() + "," + tis.getDepartTime()
											+ "," + tis.getWaitRoom());
									DeviceEventListener.getInstance().getTrainInfoMap().put(trainKey, tis);
								}
							}
						}
						line++;
					}
					reader.close();
				} catch (IOException e) {
					log.error("getTrainInfoFromTxtFile:", e.getMessage());
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e1) {
							log.error("getTrainInfoFromTxtFile:", e1);
						}
					}
				}
			}
			log.info("TrainsMap.size==" + DeviceEventListener.getInstance().getTrainInfoMap().size());
		}
	}

}
