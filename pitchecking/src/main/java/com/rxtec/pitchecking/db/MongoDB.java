package com.rxtec.pitchecking.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class MongoDB {
	private Logger log = LoggerFactory.getLogger("MongoDB");
	private MongoClient client;
	private MongoDatabase db;
	MongoCollection<DBObject> passedCollections ;
	MongoCollection<DBObject> failedCollections ;
	
	MongoCollection<DBObject> recordsCollections ;


	SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private static MongoDB _instance = new MongoDB();

	private MongoDB(){
		String uri = "mongodb://pitcheck_writer:PitcheckWriter61336956@"
				+Config.getInstance().getMongoDBAddress()+":"
				+Config.getInstance().getMongoDBPort()
				+"/?authSource=pitcheck";
	    client = new MongoClient(new MongoClientURI(uri));
		db = client.getDatabase(Config.MongoDBName);
		passedCollections = db.getCollection(Config.PassedMongoCollectionName, DBObject.class);
		failedCollections = db.getCollection(Config.FailedMongoCollectionName, DBObject.class);
		recordsCollections = db.getCollection("pit_records", DBObject.class);
	}
	
	public static synchronized MongoDB getInstance() {
		if (_instance == null)
			_instance = new MongoDB();
		return _instance;
	}
	
	public void save(PITVerifyData data, boolean isPassed){
		DBObject rec = new BasicDBObject();
//		log.debug("getIdNo=="+data.getIdNo());
//		log.debug("getPersonName=="+data.getPersonName());
//		log.debug("getAge=="+data.getAge());
		
		rec.put("id_no", data.getIdNo());
		rec.put("name",data.getPersonName());
		rec.put("age", data.getAge());
		rec.put("frame_image", data.getFaceImg());
		rec.put("id_image", data.getIdCardImg());
		rec.put("face_image", data.getFaceImg());
		rec.put("verify_result", data.getVerifyResult());
		rec.put("use_time", data.getUseTime());
		rec.put("distance", data.getFaceDistance());
		
		DBObject ticket = new BasicDBObject();
		rec.put("ticket",ticket);
		ticket.put("train_no", data.getTicket().getTrainCode());
		ticket.put("train_date", data.getTicket().getTrainDate());
		ticket.put("from_station", data.getTicket().getFromStationName());
		ticket.put("to_station", data.getTicket().getToStationName());
		ticket.put("coach_no", data.getTicket().getCoachNo());
		ticket.put("seat_no", data.getTicket().getSeatNo());

		rec.put("in_date", sf.format(new Date()));
		rec.put("gate_no", data.getTicket().getInGateNo());
		
		if(isPassed) passedCollections.insertOne(rec);
		else failedCollections.insertOne(rec);
		
		
	}
	
	
	public void clearExpirationData(){
		int days = Config.getInstance().getFaceLogRemainDays();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");

		Date today = new Date();
		String dn = sFormat.format(new Date(today.getTime() - days * 24 * 60 * 60 * 1000));
		BasicDBObject query = new BasicDBObject();  
		query.put("in_date", new BasicDBObject("$lte", dn));
		DeleteResult result = passedCollections.deleteMany(query);
		System.out.println("Delete passedCollections "+result.getDeletedCount() + " docs");
		
		result = failedCollections.deleteMany(query);
		System.out.println("Delete failedCollections "+result.getDeletedCount() + " docs");

		
	}


	public FindIterable<DBObject> getRecords(String date){
		BasicDBObject condition= new BasicDBObject();
		condition.append("in_date",new BasicDBObject("$gt",date+" 00:00:00"));
		condition.append("in_date",new BasicDBObject("$lte",date + "23:59:59"));
		return passedCollections.find(condition);
	}
	
	
	public List<PitRecord> queryAllRecords(){
		List<PitRecord> recs = new ArrayList<PitRecord>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		MongoCursor cursor  = passedCollections.find().iterator();
		while(cursor.hasNext()){
			DBObject d = (DBObject) cursor.next();
			PitRecord p = new PitRecord();
			p.setIdNo((String) d.get("id_no"));
			p.setAge((int) d.get("age"));
			p.setPersonName((String) d.get("name"));
			p.setIdCardImg((byte[]) d.get("id_image"));
			
			p.setVerifyResult((double) d.get("verify_result"));
			String pds = (String) d.get("in_date");
			p.setPitDate(pds.substring(0, 8));
			p.setPitTime(pds);
			Ticket t = new Ticket();
			t.setFromStationName((String) ((DBObject)d.get("ticket")).get("from_station"));
			t.setToStationName((String) ((DBObject)d.get("ticket")).get("to_station"));
			t.setTrainCode((String) ((DBObject)d.get("ticket")).get("train_no"));
			t.setTrainDate((String) ((DBObject)d.get("ticket")).get("train_date"));
			t.setCoachNo((String) ((DBObject)d.get("ticket")).get("coach_no"));
			t.setSeatNo((String) ((DBObject)d.get("ticket")).get("seat_no"));
			p.setTicket(t);
			FaceVerifyRecord fr = new FaceVerifyRecord();
			fr.setVerifyResult( (double) d.get("verify_result"));
			if(d.get("distance") != null)
				fr.setFaceDistance( (int) d.get("distance"));
			if(d.get("use_time") != null)
			fr.setUseTime((int) d.get("use_time"));
			fr.setFaceImg((byte[]) d.get("face_image"));
			fr.setIdCardImg((byte[]) d.get("id_image"));
			fr.setFrameImg((byte[]) d.get("frame_image"));
			fr.setFaceId(Integer.toString(p.getIdNo().hashCode()));
			p.getFaceVerifyRecords().add(fr);
			System.out.println("query record " +p);
			recs.add(p);
		}
		
		return recs;
	}

	
	private  DBObject buildRecord(PITVerifyData data){
		DBObject rec = new BasicDBObject();
		rec.put("id_no", data.getIdNo());
		rec.put("face_verify",data.getVerifyResult());
		rec.put("name",data.getPersonName());
		rec.put("age", data.getAge());
		rec.put("id_image", data.getIdCardImg());
		rec.put("pit_date", data.getPitDate());
		rec.put("check_time", data.getPitTime());
		rec.put("check_station", data.getPitStation());
		rec.put("gate_no", data.getTicket().getInGateNo());
		
		
		DBObject ticket = new BasicDBObject();
		rec.put("ticket",ticket);
		ticket.put("train_no", data.getTicket().getTrainCode());
		ticket.put("train_date", data.getTicket().getTrainDate());
		ticket.put("from_station", data.getTicket().getFromStationName());
		ticket.put("to_station", data.getTicket().getToStationName());
		ticket.put("coach_no", data.getTicket().getCoachNo());
		ticket.put("seat_no", data.getTicket().getSeatNo());

	
		DBObject record = new BasicDBObject();
		rec.put("record", record);
		record.put("verify_result", data.getVerifyResult());
		record.put("use_time", data.getUseTime());
		record.put("distance", data.getFaceDistance());
		record.put("frame_image", data.getFaceImg());
		record.put("face_image", data.getFaceImg());
		return rec;
		
	}
	

	public void save(PITVerifyData data){
		DBObject newRecord = this.buildRecord(data);
		
		DBObject dbRecord = findRecord(data.getIdNo(),data.getPitDate(),data.getTicket().getTrainCode());
		if(dbRecord == null){
			dbRecord.put("record", newRecord.get("record"));
		}else{
			recordsCollections.insertOne(newRecord);
		}
	}

	public DBObject findRecord(String idNo,String date,String trainNo){
		BasicDBObject query = new BasicDBObject();  
		BasicDBObject condition= new BasicDBObject();
		condition.append("pit_date",date);
		condition.append("id_no",idNo);
		condition.append("ticket.train_no", trainNo);
		return recordsCollections.find(condition).first();

	}


}
