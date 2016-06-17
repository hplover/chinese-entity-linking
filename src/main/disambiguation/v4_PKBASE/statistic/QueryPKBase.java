package main.disambiguation.v4_PKBASE.statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import tools.WordSeg.wordseg;

public class QueryPKBase {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	public static MongoCollection<org.bson.Document> PKBASE = null;
	public static MongoCollection<Document> ELANNOTATION = null;

	
	static int query_inclued_aa=0;
	static int query_inclued_axa=1;
	static int query_startWith=2;
	static int query_pinyin=3;
	static int query_overlap=4;
	static int query_baike=5;

	static String regex_aa(int input, int mode){
		switch (mode) {
		case 0:
			return "^[^（]*"+input+"[^）^（]*$";
		case 1:
			return "";
		default:
			return "";
		}
		
	}
	
	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("weibo");
		PKBASE = db.getCollection("PKBase");
		ELANNOTATION=db.getCollection("Annotationquery");
	}
	
	public static List<Document> getFuzzyQueryInPKBase(String input){
		List<Document> result=new ArrayList<>();
		Document filter=new Document();
		String regex="^[^（]*"+input+"[^）^（]*$";
		filter.put("title_unique", java.util.regex.Pattern.compile(regex));
		FindIterable<Document> resultFuzzy=PKBASE.find(filter);
		for(Document re:resultFuzzy){
			result.add(re);
		}
		return result;
	}
	public static String getIDbyTitle(String input){
		Document filter=new Document();
		filter.put("title_unique", input);
		Document result=PKBASE.find(filter).first();
		if(result==null||result.isEmpty()){
			return "NIL";
		}
		else return result.getString("id");
	}

	public static HashMap<String, String> getEntityName_ID(Document weibo){
		Document entities=(Document) weibo.get("entity");
		Set<String> keyset=entities.keySet();
		HashMap<String,String> entityName_ID=new HashMap<>();
		Document entity=new Document();
		for(String key:keyset){
			entity=(Document)entities.get(key);
			entityName_ID.put(entity.getString("name"),entity.getString("kb"));
		}
		return entityName_ID;
	}
	
	public static String getTitleByID(String id){
		Document query=new Document();
		query.append("id", id);
		try{
			return PKBASE.find(query).first().getString("title_unique");
		}catch (Exception e) {
			return "";
		}
	}
	
	public static Set<String> getContext(Document weibo, HashMap<String, String> entityName_ID) {
		String content=weibo.getString("content");
		Set<String> context=wordseg.segWord_Set(content, 3);
		Iterator<String> it=context.iterator();
		while(it.hasNext()){
			String pro=it.next();
			if(entityName_ID.containsKey(pro)){
				it.remove();
			}
		}
		return context;
	}
}
