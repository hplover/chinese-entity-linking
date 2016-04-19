package main.disambiguation.v2;

import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.hankcs.hanlp.HanLP;

public class test_Disambiguation {
		static HashMap<String, String> reDocuments=new HashMap<>();
//	static MongoCredential credential =null;
//	static MongoClient mongoClient =null;
//	static MongoDatabase db =null;
//	static MongoCollection<Document> collection =null;
//	
//	static{
//		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
//		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	//3006设置为Mongodb端口号
//		db = mongoClient.getDatabase("Plover");
//		collection = db.getCollection("BaikeInfo"); 
//	}
	
		
	@SuppressWarnings("unchecked")
	//pair-wise??
	public static void remove_ambiguation(Document doc,Set<Document> documents, HashMap<String, String> reDocuments){
		String word=doc.getString("word");
		String default_url=doc.getString("default_url");
		String default_context=doc.getString("default_context");
		int status=doc.getInteger("status");
		if(status==-1){
			return ;
		}
		int target=0;
		String target_url=default_url;
		for(Document adoc:documents){
			if(word!=adoc.getString("word")){
				target=target+StringUtils.countMatches(default_context,adoc.getString("word"));
			}
		}
		if(status==5){
			if(target!=0){
				reDocuments.put(word, target_url);
				return;
			}
			return;
		}
		Set<Document> polys=(Set<Document>) doc.get("Polysemant");//convert failed
		String poly_context=null;
		String poly_url=null;
		for(Document poly:polys){
			//需要改进的地方：计算实体与实体之间的关系后在决定对应的是哪个实体
			poly_context=poly.getString("poly_context");
			poly_url=poly.getString("poly_url");
			int target_temp=0;
			for(Document adoc:documents){
				if(word!=adoc.getString("word")){
					target_temp=target_temp+StringUtils.countMatches(poly_context,adoc.getString("word"));
				}
			}
			if(target_temp>target){
				target=target_temp;
				target_url=poly_url;
			}
		}
		reDocuments.put(word, target_url);
		return;
	}
		
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation bb=new Disambiguation(text);
		Set<Document> documents=bb.getEntitiesInfo();
//		System.out.println(HanLP.parseDependency(text));
		for(Document doc:documents){
			remove_ambiguation(doc,documents,reDocuments);
		}
		
		for(java.util.Map.Entry<String, String> biubiu:reDocuments.entrySet()){
			System.out.println(biubiu.getKey()+"\t"+biubiu.getValue());
		}
		
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
	
	
	
	
	
}
