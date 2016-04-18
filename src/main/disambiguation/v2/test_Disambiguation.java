package main.disambiguation.v2;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class test_Disambiguation {
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
	
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation bb=new Disambiguation(text);
		System.out.println("entities info is "+bb.getEntitiesInfo());
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
