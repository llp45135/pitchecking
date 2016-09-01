package com.rxtec.pitchecking.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.AddToSetBuilder;

import com.mongodb.WriteResult;

public class PitRecordDAO {
	private Logger log = LoggerFactory.getLogger("PitRecordLoger");
	MongoTemplate mongo;

	public PitRecordDAO() {
		SpringMongoConfig smc = new SpringMongoConfig();
		try {
			mongo = smc.mongoTemplate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void save(PitRecord rec) {
		PitRecord oldRec = queryRecord(rec.getIdNo(), rec.getTicket().getTrainDate(), rec.getTicket().getTrainCode());
		log.info("PitRecord.oldRec==" + oldRec);
		if (oldRec == null) {
			mongo.save(rec);
		} else {
			Query query = new Query(Criteria.where("idNo").is(rec.getIdNo()).and("ticket.trainCode")
					.is(rec.getTicket().getTrainCode()).and("ticket.trainDate").is(rec.getTicket().getTrainDate()));

			Update update = new Update();
			update.push("faceVerifyRecords", rec.getFaceVerifyRecords().get(0));
			mongo.upsert(query, update, PitRecord.class);

			if (rec.getVerifyResult() > oldRec.getVerifyResult()) {
				update = new Update();
				update.set("verifyResult", rec.getVerifyResult());
				mongo.findAndModify(query, update, PitRecord.class);
			}
		}
		log.info("PitRecord save done by mongo");
	}

	public PitRecord queryRecord(String idNo, String trainDate, String trainCode) {
		Query query = new Query(Criteria.where("idNo").is(idNo).and("ticket.trainCode").is(trainCode)
				.and("ticket.trainDate").is(trainDate));

		return mongo.findOne(query, PitRecord.class, "pitRecord");

	}

	public int deleteRecords(String expirationDate) {
		Query query = new Query(Criteria.where("pitDate").lt(expirationDate));
		List<PitRecord> s = mongo.findAllAndRemove(query, PitRecord.class);
		return s.size();
	}

}
