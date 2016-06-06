package main;

import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import tools.WordSeg.wordseg;

public class mongodb_test {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<Document> test_collection =null;

	static{
//		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
//		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
//		db = mongoClient.getDatabase("test_database");
//		test_collection = db.getCollection("test_collection");
	}
	public static void main(String args[]){
//		Document final_doc=new Document();
//		final_doc.append("question", "这是一个问题，关于生物的问题");
//		Document entity=new Document();
//		Document entity1_terms=new Document();
//		entity1_terms.append("问题1", "这是问题1的summary");
//		entity1_terms.append("问题2", "这是问题2summary");
//		entity.append("问题", entity1_terms);
//		Document entity2_terms=new Document();
//		entity2_terms.append("生物1", "这是生物1的summary");
//		entity2_terms.append("生物2", "这是生物2的summary");
//		entity.append("生物", entity2_terms);
//		final_doc.append("entity", entity);
//		
//
//		Document final_doc2=new Document();
//		final_doc2.append("question", "这是一个测试，关于人性的测试");
//		Document anotherentity = new Document();
//		Document anotherentity1_terms=new Document();
//		anotherentity1_terms.append("测试1", "这是测试1的summary");
//		anotherentity1_terms.append("测试2", "这是测试2summary");
//		anotherentity.append("测试", anotherentity1_terms);
//		Document anotherentity2_terms=new Document();
//		anotherentity2_terms.append("人性1", "这是人性1的summary");
//		anotherentity2_terms.append("人性2", "这是人性2的summary");
//		anotherentity.append("人性", anotherentity2_terms);
//		final_doc2.append("entity",anotherentity);
//		
//		System.out.println(final_doc+"\n"+final_doc2);
//		System.out.println("inserting into mongodb...");
//		test_collection.insertOne(final_doc);
//		test_collection.insertOne(final_doc2);
//		System.out.println("insert done");
//		System.out.println("now read from mongodb");
		
//		MongoCursor<Document> bb=test_collection.find().iterator();
//		Document qq=new Document();
//		while(bb.hasNext()){
//			qq=bb.next();
//			for(Entry<String, Object> pp:qq.entrySet()){
//				if(!pp.getKey().equals("_id"))
//				System.out.println(pp.getKey()+":"+pp.getValue());
//			}
//		}
		String text="select * from bb;";
		System.out.println(StringEscapeUtils.unescapeJava(text));
		
	}
}
