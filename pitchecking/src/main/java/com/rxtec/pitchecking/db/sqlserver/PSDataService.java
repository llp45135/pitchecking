package com.rxtec.pitchecking.db.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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
import com.rxtec.pitchecking.task.CreateLvFuTrainTask;
import com.rxtec.pitchecking.utils.CommUtil;

public class PSDataService {
	private static Logger log = LoggerFactory.getLogger("DBHelper");
	private static PSDataService _instance = new PSDataService();

	/**
	 * 获取数据库链接
	 * 
	 * @return 数据库链接
	 */
	public static Connection getConnection() {
		Connection conn = null;
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";// 驱动
		String username = "psuser";// 用户名
		String password = "psuser";// 密码
		String url = "jdbc:sqlserver://10.168.2.114:1433;databaseName=data";// SqlServer链接地址
		try {
			Class.forName(driver);// 加载驱动类
			conn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			log.error("找不到驱动程序类 ，加载驱动失败！", e);
		} catch (SQLException e) {
			log.error("数据库连接失败！", e);
		}
		return conn;
	}

	public static synchronized PSDataService getInstance() {
		if (_instance == null) {
			_instance = new PSDataService();
		}
		return _instance;
	}

	private PSDataService() {
		// TODO Auto-generated constructor stub

	}

	/**
	 * 
	 * @return
	 */
	private ArrayList getTrainList() {
		ArrayList list = new ArrayList();

		Connection conn = getConnection();// 获取数据库链接
		if (conn == null)
			return null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select station,train_code,td_start_time,sj_start_time,state from start_train_daily";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();// 执行查询
			while (rs.next()) {// 判断是否还有下一个数据
				// System.out.println("ID:" + rs.getString("id") + "\tNAME:" +
				// rs.getString("name"));
				TrainInfo train = new TrainInfo();
				train.setStation(rs.getString("station"));
				train.setTrainCode(rs.getString("train_code"));
				train.setTdStartTime(rs.getString("td_start_time"));
				train.setSjStartTime(rs.getString("sj_start_time"));
				train.setState(rs.getString("state"));
				list.add(train);
			}
		} catch (SQLException e) {
			log.error("", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();// 关闭记录集
				} catch (SQLException e) {
					log.error("", e);
				}
			}
			if (ps != null) {
				try {
					ps.close();// 关闭声明
				} catch (SQLException e) {
					log.error("", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();// 关闭连接
				} catch (SQLException e) {
					log.error("", e);
				}
			}
		}
		return list;
	}

	/**
	 * 
	 */
	public void writeTrainListToTxt() {
		ArrayList list = this.getTrainList();
		if (list != null && list.size() > 0) {
			log.info("list.size==" + list.size());
			int total = list.size();
			Iterator ite = list.iterator();
			StringBuffer sb = new StringBuffer();
			int i = 0;
			while (ite.hasNext()) {
				TrainInfo train = (TrainInfo) ite.next();
				// System.out.println(train.getTrainCode() + "@" +
				// train.getSjStartTime() + "@" + train.getStation());
				sb.append(train.getTrainCode() + "@" + train.getTdStartTime().substring(0, 19) + "@" + train.getSjStartTime().substring(0, 19) + "@" + train.getState()
						+ "@" + train.getStation());
				i++;
				if (i < total)
					sb.append("\r\n");
			}
			CommUtil.writeFileContent("d:/ftphome/train_list.txt", sb.toString());
		}
	}

	public static void main(String[] args) {
		PSDataService.getInstance().writeTrainListToTxt();

		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器

			/**
			 * 定时更新旅服信息
			 */
			JobDetail job = JobBuilder.newJob(CreateLvFuTrainTask.class).withIdentity("CreateLvFuTrainTaskJob", "queryUPSGroup").build();
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("CreateLvFuTrainTaskTrigger", "queryUPSGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getUpdateTrainInfoCronStr())).build(); // 设置触发器

			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: " + trigger.getCronExpression());

			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			log.error("", ex);
		}
	}
}
