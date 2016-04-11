package main;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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
import javatuples.Pair;
import javatuples.Triplet;
import net.sf.json.JSONArray;

public class GetBaikeInfo extends GetBasicInfo{
    public static Set<Document> documents=new HashSet<>();
    public static HashMap<String, HashMap<String, Triplet<Integer, Double, Double>>> docs_context=new HashMap<>();
    public static List<HashMap<String, Double>> tfs=new ArrayList<>();
    public static int mode=3;
    private double feature_words_ratio=0.3;
    private int docsize=1;
    HashMap<String, List<String>> feature_words=new HashMap<>();
    static MongoCollection<Document> collection=null;
    static{
//		MongoCredential credential = MongoCredential.createCredential("mdbadmin","admin"," bjgdFristDB2016".toCharArray());
//		System.out.println("***********");
//		 
//		 // 连接到 mongodb 服务器
//	    MongoClient mongoClient = new MongoClient(new ServerAddress("218.76.52.43", 3006), Arrays.asList(credential));	//3006设置为Mongodb端口号
//	    
//	    MongoDatabase mongoDatabase = mongoClient.getDatabase("BaikeInfo");
//	    collection=mongoDatabase.getCollection("Plover");
    }
	public GetBaikeInfo(String word) throws UnsupportedEncodingException{
		super(word);
		docs_context.put(super.getURL(), wordseg.segWord_TF(super.getContext(), mode));
	}
	public Set<Document> getPolysemantParallel() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();
		if(polys.isEmpty()){
			return documents;
		}
		docsize=docsize+polys.size();
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
//	public List<Document> getPolysemantSequence() throws UnsupportedEncodingException{
//		LinkedHashMap<String, String> polys=super.getPoly();
//		if(polys.isEmpty()){
//			return null;
//		}
//		String url="",desc="";
//		for(Entry<String, String> poly:polys.entrySet()){
//    		url=poly.getValue();
//    		desc=poly.getKey();
//    		Document document = new Document();
//			GetBasicInfo temp=new GetBasicInfo(url);
//			document.put("poly_url", url);
//			document.put("poly_desc", desc);
//			if(!temp.getSummary().isEmpty()){
//				document.put("poly_summary", temp.getSummary());
//			}
//			if(!temp.getLabel().isEmpty()){
//				document.put("poly_label", temp.getLabel());
//			}
//			if(!temp.getInforBox().isEmpty()){
//				document.put("poly_infobox", temp.getInforBox());
//			}
//			if(!temp.getContext().isEmpty()){
//				document.put("poly_context", temp.getContext());
//			}
//			documents.add(document);
//		}
//
//		System.out.println(documents);
//		return null;
//	}
	
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
		Set<Document> Polysemant;
		try {
			Polysemant = getPolysemantParallel();
			try {
				getTFIDF();
				document.put("default_feature", feature_words.get(default_url));
				String url=null;
				if(!Polysemant.isEmpty()&&Polysemant!=null){
					for(Document doc:Polysemant){
						url=(String) doc.get("poly_url");
						doc.append("poly_feature", feature_words.get(url));
					}
//					System.out.println(Polysemant);
					document.put("Polysemant", Polysemant);
				}
			} catch (Exception e) {
				System.out.println("get tfidf error");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return document;
	}
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e2.getValue().compareTo(e1.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	public String getTFIDF() {
		for(Entry<String, HashMap<String, Triplet<Integer, Double, Double>>> doc:docs_context.entrySet()){
			String url=doc.getKey();
			HashMap<String, Triplet<Integer, Double, Double>> terms=doc.getValue();
			Map<String,Double> word_tfidf=new HashMap<String, Double>();
			for(Entry<String, Triplet<Integer, Double, Double>> doc_term:terms.entrySet()){
				String word=doc_term.getKey();
				Triplet<Integer, Double, Double> term_data=doc_term.getValue();
				Double tfidf=term_data.getValue1()*(1+Math.log(wordseg.alldocterms/wordseg.allterms.get(word)));
//				System.out.println("url:"+word+"\ttfidf"+tfidf);
				word_tfidf.put(word, tfidf);
			}
			SortedSet<Entry<String, Double>> result=entriesSortedByValues(word_tfidf);
			
			List<String> temp=new ArrayList<>();
			int i=0;
			int proportion=(int) (word_tfidf.size()*feature_words_ratio);
			for(Entry<String, Double> word:result){
				temp.add(word.getKey());
				i++;
				if(i>proportion){
					break;
				}
			}
			feature_words.put(url, temp);
		}
//		System.out.println(feature_words);
		return "";
	}
	
	
	public static void main(String args[]) throws Exception{
    	long start=System.currentTimeMillis();
		GetBaikeInfo extract=new GetBaikeInfo("一个不存在的词汇");
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
		System.out.println(extract.writeMongo());
    	long pp=System.currentTimeMillis();
    	System.out.println("all:"+(pp-start));
	}
}

class PolyParallel implements Runnable {
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
		Document document = new Document();
		writeDocument(url,desc,document);
		synchronized(GetBaikeInfo.documents){
			GetBaikeInfo.documents.add(document);
		}
    }

    private void writeDocument(String url, String desc, Document document) {
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
			if(!temp.getContext().isEmpty()){
				HashMap<String, Triplet<Integer, Double, Double>> terms=wordseg.segWord_TF(temp.getContext(), GetBaikeInfo.mode);
				synchronized (GetBaikeInfo.docs_context) {
					GetBaikeInfo.docs_context.put(url, terms);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
