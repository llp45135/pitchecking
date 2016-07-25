package com.rxtec.pitchecking.db;

import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class DataMigration {

	public static void main(String[] args) {
		PitRecordDAO dao = new PitRecordDAO();
		List<PitRecord> recs = MongoDB.getInstance().queryAllRecords();
		for(PitRecord rec : recs){
			System.out.println(rec);
			dao.save(rec);
//			break;
		}
		
		
		
	}
	
	

	

}
