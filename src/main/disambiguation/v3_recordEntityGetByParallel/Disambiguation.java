package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import tools.WordSeg.wordseg;
import tools.javatuples.Tuple;

public class Disambiguation {
	ExecutorService executorService=Executors.newCachedThreadPool();
	Set<Document> entitySet=new HashSet<>();
	HashSet<String> nameMetioned=new HashSet<>();
//	static MongoCredential credential =null;
//	static MongoClient mongoClient =null;
//	static MongoDatabase db =null;
//	static MongoCollection<Document> collection_entity =null;
//	static MongoCollection<Document> collection_synonym =null;
//	static MongoCollection<Document> collection_polysemy =null;
//	static MongoCollection<Document> collection_index =null;
//	static{
//		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
//		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	//3006设置为Mongodb端口号
//		db = mongoClient.getDatabase("Plover");
//		collection_entity = db.getCollection("BaikeEntity");
//		collection_synonym = db.getCollection("BaikeSynonym");
//		collection_polysemy = db.getCollection("BaikePolysemy");
//		collection_index=db.getCollection("entityIndex");
//	}
	public Disambiguation(String text) {
		long f1=System.currentTimeMillis();
		nameMetioned=(HashSet<String>) wordseg.segWord_Set(text, 3);
		long f2=System.currentTimeMillis();
		System.out.println("segment: "+(f2-f1));
	}
	public Set<Document> getEntitiesInfo() {
		for(String entity:nameMetioned){
    		executorService.submit(new EntityParallel(entity,entitySet));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return entitySet;
	}
		
	
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="苹果";
		Disambiguation bb=new Disambiguation(text);
		System.out.println("entities info is "+bb.getEntitiesInfo());
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
class EntityParallel implements Runnable{
	private String entity="";
	Set<Document> entities_info=new HashSet<>();
	public EntityParallel(String entity, Set<Document> entities_info) {
		this.entity=entity;
		this.entities_info=entities_info;
	}

	@Override
	public void run() {
		process();
	}

	private void process() {
		GetBaikeInfo temp;
		try {
			temp = new GetBaikeInfo(entity,null);
//			System.out.println(xx);
			synchronized (entities_info) {
				HashSet<Document> xx=temp.writeMongo();
				entities_info.addAll(xx);
			}
//			System.out.println(entities_info);
		} catch (UnsupportedEncodingException e) {
			System.out.printf("failed to get %s info\n",entity);
		}
		
	}
}
