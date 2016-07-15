package com.rxtec.pitchecking.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class MongoDB {
	private MongoClient client;
	private MongoDatabase db;
	MongoCollection<DBObject> collections ;
	SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private static MongoDB _instance = new MongoDB();

	private MongoDB(){
		client = new MongoClient(Config.getInstance().getMongoDBAddress(),
				Config.getInstance().getMongoDBPort());
		db = client.getDatabase(Config.MongoDBName);
		collections = db.getCollection(Config.MongoCollectionName, DBObject.class);
	}
	
	public static synchronized MongoDB getInstance() {
		if (_instance == null)
			_instance = new MongoDB();
		return _instance;
	}
	
	public void save(PITVerifyData data){
		DBObject rec = new BasicDBObject();
		rec.put("id_no", data.getIdNo());
		rec.put("name",data.getPersonName());
		rec.put("age", data.getAge());
		rec.put("frame_image", data.getFaceImg());
		rec.put("id_image", data.getIdCardImg());
		rec.put("face_image", data.getFaceImg());
		rec.put("verify_result", data.getVerifyResult());
		DBObject ticket = new BasicDBObject();
		rec.put("ticket",ticket);
		ticket.put("train_no", data.getTicket().getTrainCode());
		ticket.put("train_date", data.getTicket().getTrainDate());
		ticket.put("from_station", data.getTicket().getFromStationCode());
		ticket.put("to_station", data.getTicket().getEndStationCode());
		ticket.put("coach_no", data.getTicket().getCoachNo());
		ticket.put("seat_no", data.getTicket().getSeatNo());

		rec.put("in_date", sf.format(new Date()));
		rec.put("gate_no", data.getTicket().getInGateNo());
		
		collections.insertOne(rec);
		
		
	}

}
