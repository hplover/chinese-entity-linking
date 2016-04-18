package main.disambiguation.v2;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
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

public class Disambiguation {
	ExecutorService executorService=Executors.newCachedThreadPool();
	Set<Document> entities_info=new HashSet<>();
	HashSet<String> entities=null;
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<Document> collection =null;
	
	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	//3006设置为Mongodb端口号
		db = mongoClient.getDatabase("Plover");
		collection = db.getCollection("BaikeInfo"); 
	}
	public Disambiguation(String text) {
		long f1=System.currentTimeMillis();
		entities=(HashSet<String>) wordseg.segWord_Set(text, 3);
		long f2=System.currentTimeMillis();
		System.out.println("segment: "+(f2-f1));
	}
	public Set<Document> getEntitiesInfo() {
		for(String entity:entities){
			if(InMongoDB(entity.toLowerCase())){
				continue;
			}
    		executorService.submit(new EntityParallel(entity,entities_info,collection));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return entities_info;
	}
		
	private boolean InMongoDB(String entity) {
		Document search_word=new Document();
		search_word.put("word", entity);
		FindIterable<Document> results_word=collection.find(search_word);
		Document search_alias=new Document();
		search_alias.put("default_alias", entity);
		FindIterable<Document> results_alias=collection.find(search_word);
		boolean re=false;
		for(Document result:results_word){
//			System.out.println("word:"+result);
			entities_info.add(result);
			re=true;
		}
		for(Document result:results_alias){
//			System.out.println("alias:"+result);
			entities_info.add(result);
			re=true;
		}
		if(re)
		System.out.println(entity+" found");
		return re;
	}
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation bb=new Disambiguation(text);
		System.out.println("entities info is "+bb.getEntitiesInfo());
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
class EntityParallel implements Runnable{
	private String entity="";
	Set<Document> entities_info=new HashSet<>();
	private MongoCollection<Document> collection;
	public EntityParallel(String entity, Set<Document> entities_info, MongoCollection<Document> collection) {
		this.entity=entity;
		this.entities_info=entities_info;
		this.collection=collection;
	}

	@Override
	public void run() {
		process();
	}

	private void process() {
		GetBaikeInfo temp;
		try {
			temp = new GetBaikeInfo(entity);
//			System.out.println(xx);
			synchronized (entities_info) {
				Document xx=temp.writeMongo(collection);
				entities_info.add(xx);
			}
//			System.out.println(entities_info);
		} catch (UnsupportedEncodingException e) {
			System.out.printf("failed to get %s info\n",entity);
		}
		
	}
}
