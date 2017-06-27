package com.rxtec.pitchecking.db.mysql;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.wharf.PhotoReceiptBean;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class PitRecordSqlDao {
	private Logger log = LoggerFactory.getLogger("PitRecordLoger");
	private String targetIPAddress = "localhost";
	private Connection conn = null;
	PreparedStatement statement = null;

	public String getTargetIPAddress() {
		return targetIPAddress;
	}

	public void setTargetIPAddress(String targetIPAddress) {
		this.targetIPAddress = targetIPAddress;
	}

	public PitRecordSqlDao(String targetIPAddress) {
		this.connSQLDB(targetIPAddress);
	}

	/**
	 * connect to MySQL
	 */
	private void connSQLDB(String targetIPAddress) {
		String url = "jdbc:mysql://" + targetIPAddress + ":3306/pitcheck?autoReconnect=true";
		String username = "root";
		String password = "pitcheck61336956"; // 加载驱动程序以连接
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, password);
			log.debug("connSQLDB successfully!!");
		}
		// 捕获加载驱动程序异常
		catch (ClassNotFoundException cnfex) {
			log.error("装载 JDBC/ODBC 驱动程序失败。", cnfex);
		}
		// 捕获连接数据库异常
		catch (SQLException sqlex) {
			log.error("无法连接数据库", sqlex);
		} catch (Exception ex) {
			log.error("无法连接数据库", ex);
		}
	}

	/**
	 * disconnect to MySQL
	 */
	public void deconnSQL() {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			log.error("关闭数据库问题", e);
		}
	}

	/**
	 * 执行查询语句
	 * 
	 * @param sql
	 * @return
	 */
	public ResultSet selectSQL(String sql) {
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(sql);
			rs = statement.executeQuery(sql);
		} catch (SQLException sqlex) {
			log.error("selectSQL:", sqlex);
		}
		return rs;
	}

	
	/**
	 * 按桂林码头格式入库
	 * @param rec
	 * @param idcardImgPath
	 * @param faceImgPath
	 * @param frameImgPath
	 * @return
	 */
	public String createInsertPhotoReceiptSqlstr(PhotoReceiptBean rec, String idcardImgPath, String faceImgPath, String frameImgPath) {
		String sqlstr = "";
		sqlstr = "insert into pit_phototable(msgType,idCardNo,ticketNo,ipAddress,pitDate,pitTime,verifyResult,isVerifyPassed,citizenName,gender,age,ideffice,idefficb,idissue,iddwelling,idbirth,idnation,idPicImagePath,faceImagePath,frameImagePath)";
		sqlstr += " values (";
		sqlstr += rec.getMsgType() + "";
		if (rec.getIdCardNo() != null)
			sqlstr += ",'" + rec.getIdCardNo() + "'";
		else
			sqlstr += ",''";
		if (rec.getTicketNo() != null)
			sqlstr += ",'" + rec.getTicketNo() + "'";
		else
			sqlstr += ",''";
		if (rec.getIpAddress() != null)
			sqlstr += ",'" + rec.getIpAddress() + "'";
		else
			sqlstr += ",''";
		if (rec.getPitDate() != null)
			sqlstr += ",'" + rec.getPitDate() + "'";
		else
			sqlstr += ",''";
		if (rec.getPitTime() != null)
			sqlstr += ",'" + rec.getPitTime() + "'";
		else
			sqlstr += ",''";
		sqlstr += "," + rec.getVerifyResult() + "";
		sqlstr += "," + rec.getIsVerifyPassed() + "";
		if (rec.getCitizenName() != null)
			sqlstr += ",'" + rec.getCitizenName() + "'";
		else
			sqlstr += ",''";
		sqlstr += "," + rec.getGender() + "";
		sqlstr += "," + rec.getAge() + "";
		if (rec.getIdeffice() != null)
			sqlstr += ",'" + rec.getIdeffice() + "'";
		else
			sqlstr += ",''";
		if (rec.getIdefficb() != null)
			sqlstr += ",'" + rec.getIdefficb() + "'";
		else
			sqlstr += ",''";
		if (rec.getIdissue() != null)
			sqlstr += ",'" + rec.getIdissue() + "'";
		else
			sqlstr += ",''";
		if (rec.getIddwelling() != null)
			sqlstr += ",'" + rec.getIddwelling() + "'";
		else
			sqlstr += ",''";
		if (rec.getIdbirth() != null)
			sqlstr += ",'" + rec.getIdbirth() + "'";
		else
			sqlstr += ",''";
		if (rec.getIdnation() != null)
			sqlstr += ",'" + rec.getIdnation() + "'";
		else
			sqlstr += ",''";
		if (idcardImgPath != null)
			sqlstr += ",'" + idcardImgPath + "'";
		else
			sqlstr += ",''";
		if (faceImgPath != null)
			sqlstr += ",'" + faceImgPath + "'";
		else
			sqlstr += ",''";
		if (frameImgPath != null)
			sqlstr += ",'" + frameImgPath + "'";
		else
			sqlstr += ",''";

		sqlstr += ")";
		return sqlstr;
	}

	/**
	 * 
	 * @param rec
	 * @param recordType
	 * @return
	 */
	public String createInsertSqlstr(PitFaceRecord rec, int recordType) {
		String sqlstr = "";
		if (recordType == 1)
			sqlstr = "insert into pit_face_verify(idNo,personName,gender,age,verifyResult,pitDate,pitStation,pitTime,gateNo,faceId,faceDistance,useTime,idCardImgPath,faceImgPath,frameImgPath,facePosePitch,facePoseRoll,facePoseYaw)";
		if (recordType == 2)
			sqlstr = "insert into pit_face_verify(idNo,personName,gender,age,verifyResult,pitDate,pitStation,pitTime,gateNo,faceId,faceDistance,useTime,idCardImgPath,faceImgPath,frameImgPath,facePosePitch,facePoseRoll,facePoseYaw,ticketNo,fromStationCode,endStationCode,trainCode,coachNo,seatCode,ticketType,seatNo,trainDate,cardNo,passengerName,saleOfficeNo,saleWindowNo,saleDate)";
		sqlstr += " values (";
		sqlstr += "'" + rec.getIdNo() + "'";
		if (rec.getPersonName() != null)
			sqlstr += ",'" + rec.getPersonName() + "'";
		else
			sqlstr += ",NULL";
		sqlstr += "," + rec.getGender() + "";
		sqlstr += "," + rec.getAge() + "";
		sqlstr += "," + rec.getVerifyResult() + "";
		if (rec.getPitDate() != null)
			sqlstr += ",'" + rec.getPitDate() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getPitStation() != null)
			sqlstr += ",'" + rec.getPitStation() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getPitTime() != null)
			sqlstr += ",'" + rec.getPitTime() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getGateNo() != null)
			sqlstr += ",'" + rec.getGateNo() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getFaceId() != null)
			sqlstr += ",'" + rec.getFaceId() + "'";
		else
			sqlstr += ",NULL";
		sqlstr += "," + rec.getFaceDistance() + "";
		sqlstr += "," + rec.getUseTime() + "";
		if (rec.getIdCardImgPath() != null)
			sqlstr += ",'" + rec.getIdCardImgPath() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getFaceImgPath() != null)
			sqlstr += ",'" + rec.getFaceImgPath() + "'";
		else
			sqlstr += ",NULL";
		if (rec.getFrameImgPath() != null)
			sqlstr += ",'" + rec.getFrameImgPath() + "'";
		else
			sqlstr += ",NULL";
		sqlstr += "," + rec.getFacePosePitch() + "";
		sqlstr += "," + rec.getFacePoseRoll() + "";
		sqlstr += "," + rec.getFacePoseYaw() + "";
		if (recordType == 2) {
			if (rec.getTicketNo() != null)
				sqlstr += ",'" + rec.getTicketNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getFromStationCode() != null)
				sqlstr += ",'" + rec.getFromStationCode() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getEndStationCode() != null)
				sqlstr += ",'" + rec.getEndStationCode() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getTrainCode() != null)
				sqlstr += ",'" + rec.getTrainCode() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getCoachNo() != null)
				sqlstr += ",'" + rec.getCoachNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getSeatCode() != null)
				sqlstr += ",'" + rec.getSeatCode() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getTicketType() != null)
				sqlstr += ",'" + rec.getTicketType() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getSeatNo() != null)
				sqlstr += ",'" + rec.getSeatNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getTrainDate() != null)
				sqlstr += ",'" + rec.getTrainDate() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getCardNo() != null)
				sqlstr += ",'" + rec.getCardNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getPassengerName() != null)
				sqlstr += ",'" + rec.getPassengerName() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getSaleOfficeNo() != null)
				sqlstr += ",'" + rec.getSaleOfficeNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getSaleWindowNo() != null)
				sqlstr += ",'" + rec.getSaleWindowNo() + "'";
			else
				sqlstr += ",NULL";
			if (rec.getSaleDate() != null)
				sqlstr += ",'" + rec.getSaleDate() + "'";
			else
				sqlstr += ",NULL";
		}
		sqlstr += ")";
		return sqlstr;
	}

	/**
	 * 执行插入语句
	 * 
	 * @param sql
	 * @return
	 */
	public boolean insertSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			log.debug("insert one pitFaceRecord successfully");
			return true;
		} catch (SQLException sqlex) {
			log.error("insertSQL:", sqlex);
		} catch (Exception e) {
			log.error("insertSQL:", e);
		}
		return false;
	}

	/**
	 * 删除一条记录
	 * 
	 * @param sql
	 * @return
	 */
	public boolean deleteSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException sqlex) {
			log.error("deleteSQL:", sqlex);
		} catch (Exception e) {
			log.error("deleteSQL:", e);
		}
		return false;
	}

	/**
	 * 更新一条记录
	 * 
	 * @param sql
	 * @return
	 */
	public boolean updateSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException sqlex) {
			log.error("updateSQL:", sqlex);
		} catch (Exception e) {
			log.error("updateSQL:", e);
		}
		return false;
	}

	// show data in ju_users
	public void layoutStyle2(ResultSet rs) {
		System.out.println("-----------------");
		System.out.println("执行结果如下所示:");
		System.out.println("-----------------");
		System.out.println(" ID" + "/t/t" + "身份证号" + "/t/t" + "验证结果");
		System.out.println("-----------------");
		try {
			while (rs.next()) {
				System.out.println(rs.getInt("id") + "/t/t" + rs.getString("idcard_no") + "/t/t" + rs.getString("verify_result"));
			}
		} catch (SQLException e) {
			System.out.println("显示时数据库出错。");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("显示出错。");
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		String targetIPAddress = "127.0.0.1";
		PitRecordSqlDao pitRecordSqlDao = new PitRecordSqlDao(targetIPAddress);
		// String s = "select * from pit_face_verify";

		// String insert = "insert into pit_face_verify values (3,
		// '520203197912141112', 0.66)";
		// String update = "update pit_face_verify set verify_result = 0.65
		// where id=3";
		// String delete = "delete from pit_face_verify where id=3";

		PITVerifyData pd = new PITVerifyData();
		pd.setIdNo("333333333333333333");
		pd.setPersonName("赵林");
		pd.setGender(1);
		pd.setAge(36);
		pd.setVerifyResult((float) 0.68);
		pd.setPitDate("20161017");
		pd.setPitStation("IZQ");
		pd.setPitTime("1300");
		pd.setIdCardImg("c:/pitcheck/images/aa.jpg".getBytes());
		pd.setFaceImg("c:/pitcheck/images/aa.jpg".getBytes());
		pd.setFrameImg("c:/pitcheck/images/aa.jpg".getBytes());
		pd.setFaceDistance((float) 1.3);
		pd.setUseTime(560);
		pd.setFacePosePitch((float) 0.55);
		pd.setFacePoseRoll((float) 0.66);
		pd.setFacePoseYaw((float) 0.77);

		// String sqls = pitRecordSqlDao.createInsertSqlstr(new
		// PitFaceRecord(pd));
		// System.out.println("sqls==" + sqls);
		// if (pitRecordSqlDao.insertSQL(sqls) == true) {
		// System.out.println("insert successfully");
		// }
		try {
			String sqls = "select idNO from pit_face_verify where verifyResult>=0.6 group by idNo";
			ResultSet rs = pitRecordSqlDao.selectSQL(sqls);
			int total = 0;
			while (rs.next()) {
				total++;
			}
			System.out.println("total==" + total);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pitRecordSqlDao.deconnSQL();
	}
}
