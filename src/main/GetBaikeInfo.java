package main;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import WordSeg.wordseg;
import net.sf.json.JSONArray;

public class GetBaikeInfo extends GetBasicInfo{
    public static Set<BasicDBObject> documents=new HashSet<>();
    public static List<HashMap<String, Double>> tfs=new ArrayList<>();
    static MongoCollection<Document> collection=null;
    static{
		MongoCredential credential = MongoCredential.createCredential("mdbadmin","admin"," bjgdFristDB2016".toCharArray());
		System.out.println("***********");
		 
		 // 连接到 mongodb 服务器
	    MongoClient mongoClient = new MongoClient(new ServerAddress("218.76.52.43", 3006), Arrays.asList(credential));	//3006设置为Mongodb端口号
	    
	    MongoDatabase mongoDatabase = mongoClient.getDatabase("BaikeInfo");
	    collection=mongoDatabase.getCollection("Plover");
    }
	public GetBaikeInfo(String word) throws UnsupportedEncodingException{
		super(word);
	}
	public Set<BasicDBObject> getPolysemantParallel() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();
		if(polys.isEmpty()){
			return documents;
		}
		int polysize=polys.size();
		ExecutorService executorService=Executors.newCachedThreadPool();
		String url="",desc="";
		for(Entry<String, String> poly:polys.entrySet()){
    		url=poly.getValue();
    		desc=poly.getKey();
    		executorService.submit(new PolyParallel(url,desc));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return documents;
	}
	public List<Document> getPolysemantSequence() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();
		if(polys.isEmpty()){
			return null;
		}
		String url="",desc="";
		for(Entry<String, String> poly:polys.entrySet()){
    		url=poly.getValue();
    		desc=poly.getKey();
    		BasicDBObject document = new BasicDBObject();
			GetBasicInfo temp=new GetBasicInfo(url);
			document.put("poly_url", url);
			document.put("poly_desc", desc);
			if(!temp.getSummary().isEmpty()){
				document.put("poly_summary", temp.getSummary());
			}
			if(!temp.getLabel().isEmpty()){
				document.put("poly_label", temp.getLabel());
			}
			if(!temp.getInforBox().isEmpty()){
				document.put("poly_infobox", temp.getInforBox());
			}
			if(!temp.getContext().isEmpty()){
				document.put("poly_context", temp.getContext());
			}
			documents.add(document);
		}

		System.out.println(documents);
		return null;
	}
	
	public Document writeMongo(){
		String word=super.getWord();
		Document document=new Document();
		Date date=super.getDate();
		document.put("Date", date);
		if(!word.isEmpty()){
			document.put("word", word);
		}
		int status=super.getStatus();
		document.put("status", status);
		String default_url=super.getURL();
		if(!default_url.isEmpty()){
			document.put("default_url", default_url);
		}
		String default_desc=super.getDesc();
		if(!default_desc.isEmpty()){
			document.put("default_desc", default_desc);
		}
		String default_alias=super.getAlias();
		if(!default_alias.isEmpty()){
			document.put("default_alias", default_alias);
		}
		String default_summary=super.getSummary();
		if(!default_summary.isEmpty()){
			document.put("default_summary", default_summary);
		}
		Set<String> default_label=super.getLabel();
		if(!default_label.isEmpty()){
			document.put("default_label", default_label);
		}
		JSONArray default_infobox=super.getInforBox();
		if(!default_infobox.isEmpty()){
			document.put("default_infobox", default_infobox);
		}
		Set<BasicDBObject> Polysemant;
		try {
			Polysemant = getPolysemantParallel();
			if(!Polysemant.isEmpty()&&Polysemant!=null){
				document.put("Polysemant", Polysemant);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		collection.insertOne(document);//write to mongodb
		return document;
	}
	
	
	public String getTFIDF() {
		HashMap<String, Double> docsTF=new HashMap<>();
		String word=null;
		Double count=0.0;
		for(HashMap<String, Double> tf:tfs){
			for(Entry<String, Double> doc:tf.entrySet()){
				word=doc.getKey();
				count=doc.getValue();
				if(docsTF.containsKey(word)){
					docsTF.put(word, docsTF.get(word)+count);
				}
				else{
					docsTF.put(word, count);
				}
			}
		}
		return "";
	}
	
	
	public static void main(String args[]) throws Exception{
    	long start=System.currentTimeMillis();
		GetBaikeInfo extract=new GetBaikeInfo("四叶草");
//    	System.out.println("time:"+extract.getDate());
//    	System.out.println("word:"+extract.getWord());
//    	System.out.println("status:"+extract.getStatus());
//    	System.out.println("url:"+extract.getURL());
//    	System.out.println("desc:"+extract.getDesc());
//    	System.out.println("alias:"+extract.getAlias());
//    	System.out.println("summary:"+extract.getSummary());
////    	System.out.println("content:"+extract.getContext());
//    	System.out.println("=============================================");
//    	System.out.println("label:"+extract.getLabel());
//    	System.out.println("infobox:"+extract.getInforBox());
//    	System.out.println("polysemy:"+extract.getPoly().size()+" "+extract.getPoly().toString());
//    	long mu=System.currentTimeMillis();
//    	extract.getPolysemantSequence();
//    	long middle=System.currentTimeMillis();
//
//    	extract.getPolysemantParallel();
//    	System.out.println("default:"+(mu-start));
//    	System.out.println("sequence:"+(middle-mu));
//    	System.out.println("parallel:"+(pp-middle));
		System.out.println("document:\n"+extract.writeMongo());
    	long pp=System.currentTimeMillis();
    	System.out.println("all:"+(pp-start));
	}
}

class PolyParallel implements Runnable {
    private final Object lock = new Object();
	private String url;
	private String desc;
	public PolyParallel(String url, String desc) {
		this.url=url;
		this.desc=desc;
	}

	@Override
    public void run() {
        process();
    }

    public void process() {
		BasicDBObject document = new BasicDBObject();
		List<String> terms=new ArrayList<>();
		writeDocument(url,desc,document,terms);
		synchronized(lock){
			GetBaikeInfo.documents.add(document);
		}
    }

    private void writeDocument(String url, String desc, BasicDBObject document, List<String> terms) {
    	try {
			GetBasicInfo temp=new GetBasicInfo(url);
			document.put("poly_url", url);
			document.put("poly_desc", desc);
			if(!temp.getSummary().isEmpty()){
				document.put("poly_summary", temp.getSummary());
			}
			if(!temp.getLabel().isEmpty()){
				document.put("poly_label", temp.getLabel());
			}
			if(!temp.getInforBox().isEmpty()){
				document.put("poly_infobox", temp.getInforBox());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
