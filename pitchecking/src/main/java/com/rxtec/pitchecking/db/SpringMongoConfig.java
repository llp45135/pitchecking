package com.rxtec.pitchecking.db;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.rxtec.pitchecking.Config;

/**
 * Spring MongoDB configuration file
 */
@Configuration
public class SpringMongoConfig {
	private Logger log = LoggerFactory.getLogger("SpringMongoConfig");
	public @Bean MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), "pitcheck");
	}

	public Mongo mongo() throws Exception {
		String uri = "mongodb://pitcheck_writer:PitcheckWriter61336956" + "@" + Config.getInstance().getMongoDBAddress()
				+ ":" + Config.getInstance().getMongoDBPort() + "/?authSource=pitcheck";
		log.debug("连接到mongodb uri==" + uri);
		// String uri = "mongodb://root:root"+"@"+
		// Config.getInstance().getMongoDBAddress()
		// + ":" + Config.getInstance().getMongoDBPort() + "/?authSource=admin";
		return new Mongo(new MongoURI(uri));
	}

	public static void main(String[] args) {
		// PitRecordDAO dao = new PitRecordDAO();
		PitRecordLoger.getInstance().clearExpirationData();
		PitRecordLoger.getInstance().startThread();
	}

}