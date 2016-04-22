package main.disambiguation.v3_recordEntityGetByParallel;        

import java.io   .UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

import net.sf.json.JSONArray;
import tools.WordSeg.wordseg;
import tools.javatuples.Triplet;
/**
 * 这个类用来构建实体表，同义词表，异义词表；以及返回某个实体的全部信息
 * @author HuangDC
 */
public class GetBaikeInfo extends GetBasicInfo{
    Set<Document> documents=new HashSet<>();
    HashMap<String, HashMap<String, Triplet<Integer, Double, Double>>> docs_context=new HashMap<>();
    
    private double feature_words_ratio=0.5;
    HashSet<Document> SetEntity=null;
    HashMap<String, Set<String>> feature_words=new HashMap<>();
	private boolean isSetEntity;
	
	static int mode=3;
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<Document> collection_entity =null;
	static MongoCollection<Document> collection_synonym =null;
	static MongoCollection<Document> collection_polysemy =null;
	static MongoCollection<Document> collection_index =null;
	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	//3006设置为Mongodb端口号
		db = mongoClient.getDatabase("Plover");
		collection_entity = db.getCollection("BaikeEntity");
		collection_synonym = db.getCollection("BaikeSynonym");
		collection_polysemy = db.getCollection("BaikePolysemy");
		collection_index=db.getCollection("entityIndex");
	}
	
	
	public GetBaikeInfo(String word,HashSet<Document> temp) throws UnsupportedEncodingException{
		super(word,temp=new HashSet<>(),collection_index);
		SetEntity=temp;
		if(!SetEntity.isEmpty()){
			isSetEntity=true;
		}
		else{
			docs_context.put(super.getURL(), wordseg.segWord_TF(super.getContext(), mode));
		}
	}
	private Set<Document> getPolysemantParallel() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();
		if(polys.isEmpty()){
			return documents;
		}
		ExecutorService executorService=Executors.newCachedThreadPool();
		String url="",desc="";
		for(Entry<String, String> poly:polys.entrySet()){
    		url=poly.getValue();
    		desc=poly.getKey();
    		executorService.submit(new PolyParallel(url,documents,docs_context));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return documents;
	}
	
	public HashSet<Document> writeMongo(){
		if(isSetEntity){
			return SetEntity;
		}
		
		int status=super.getStatus();
		Date date=super.getDate();
		String title=super.getTitle();
		String word=super.getWord();
		if(status==-1){
			return SetEntity;
		}
		
		if(!word.equals(title)||!word.equals(title.toUpperCase())){
			if(status==1){
				collection_index.insertOne(new Document("word",word).append("type", "polysemy"));
				collection_index.insertOne(new Document("word",title).append("type", "entity"));
				collection_polysemy.insertOne(new Document("word",word).append("entity", title));//写入多义词表
				if(!title.endsWith("）")){
					collection_synonym.insertOne(new Document("word",word).append("entity", title));//写入同义词表
				}
			}
			else if(status==5){
				collection_index.insertOne(new Document("word",word).append("type", "synonym"));
				collection_index.insertOne(new Document("word",title).append("type", "entity"));
				collection_synonym.insertOne(new Document("word",word).append("entity", title));
			}
		}
		else{
			collection_index.insertOne(new Document("word",word).append("type", "entity"));
		}
		Document entitydoc=new Document();
		
		entitydoc.put("Date", date);
		if(!title.isEmpty()){//设置title
			entitydoc.put("title", title);
		}

		String url=super.getURL();
		if(!url.isEmpty()){//设置url
			entitydoc.put("url", url);
		}

		double prevalence=0.9/1.1;
		entitydoc.append("prevalence", prevalence);//设置流行度
		String summary=super.getSummary();
		if(!summary.isEmpty()){//设置summary
			entitydoc.put("summary", summary);
		}
		ArrayList<String> label=super.getLabel();
		if(!label.isEmpty()){//设置label
			entitydoc.put("label", label);
		}
		List<String> keywords=super.getKeyWords();
		if(!keywords.isEmpty()){//设置keywords
			entitydoc.put("keywords", keywords);
		}
		Document infobox=super.getInforBox();
		if(!infobox.isEmpty()){//设置infobox
			entitydoc.put("infobox", infobox);
		}
		String context=super.getContext();
		if(!context.isEmpty()){//设置context
			entitydoc.put("context", context);
		}
		Set<Document> Polysemant;
		try {
			Polysemant = getPolysemantParallel();
			try {
				getTFIDF();
				Set<String> feature=feature_words.get(url);
				if(!feature.isEmpty())
				entitydoc.put("feature", feature_words.get(url));
				try {
					collection_entity.insertOne(entitydoc);//写入默认的实体
					SetEntity.add(entitydoc);
				} catch (Exception e) {
					System.err.println("write to mongodb failed");
				}
				String polyurl=null;
				if(!Polysemant.isEmpty()&&Polysemant!=null){
					for(Document doc:Polysemant){
						polyurl=(String) doc.get("url");
						title=doc.getString("title");
						doc.append("feature", feature_words.get(polyurl));
						try {
							collection_index.insertOne(new Document("word",title).append("type", "entity"));//写入索引表
							collection_entity.insertOne(doc);//写入实体表
							collection_polysemy.insertOne(new Document("word",word).append("entity", title));//写入多义词表
							SetEntity.add(doc);
						} catch (Exception e) {
							System.err.println("write to mongodb failed");
						}
					}
				}
			} catch (Exception e) {
				System.out.println("get tfidf error");
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("get poly failed");
		}
		return SetEntity;
	}
	
	
	public static boolean InMongoDB(String word,Set<Document> entitySet) {
		boolean re=false;
		if(entitySet==null||collection_index==null){
			return re;
		}
		Document re_index=collection_index.find(new Document("word",word)).first();
		if(re_index!=null&&re_index.size()!=0){
			System.out.println(word+" found word ");
			String type=re_index.getString("type");
			switch (type) {
			case "entity":
				Document re_entity=collection_entity.find(new Document("title",word)).first();
				if(re_entity!=null&&re_entity.size()!=0){
					entitySet.add(re_entity);
					return true;
				}
				break;
			case "synonym":
				Document re_synonym=collection_synonym.find(new Document("word",word)).first();
				if(re_synonym!=null&&re_synonym.size()!=0){
					entitySet.add(collection_entity.find(new Document("title",re_synonym.get("entity"))).first());
					return true;
				}
				break;
			case "polysemy":
				FindIterable<Document> re_polysemys=collection_polysemy.find(new Document("word",word));
				if(re_polysemys!=null){
					for(Document re_polysemy:re_polysemys){
						entitySet.add(collection_entity.find(new Document("title",re_polysemy.get("entity"))).first());
					}
					return true;
				}
				break;
			default:
				break;
			}
		}
		return re;
	}
	
//	static <K,V extends Comparable<? super V>>
//	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
//	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
//	        new Comparator<Map.Entry<K,V>>() {
//	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
//	                int res = e2.getValue().compareTo(e1.getValue());
//	                return res != 0 ? res : 1;
//	            }
//	        }
//	    );
//	    sortedEntries.addAll(map.entrySet());
//	    return sortedEntries;
//	}
	
	public String getTFIDF() {
		for(Entry<String, HashMap<String, Triplet<Integer, Double, Double>>> doc:docs_context.entrySet()){
			String url=doc.getKey();
			HashMap<String, Triplet<Integer, Double, Double>> terms=doc.getValue();
			Map<String,Double> word_tfidf=new HashMap<String, Double>();
			for(Entry<String, Triplet<Integer, Double, Double>> doc_term:terms.entrySet()){
				String word=doc_term.getKey();
				Triplet<Integer, Double, Double> term_data=doc_term.getValue();
				Double tfidf=term_data.getValue1()*(1+Math.log(wordseg.alldocterms/wordseg.allterms.get(word)));
				word_tfidf.put(word, tfidf);
			}
//			SortedSet<Entry<String, Double>> result=entriesSortedByValues(word_tfidf);
			
			Set<String> temp=new HashSet<>();
			int i=0;
			int proportion=(int) (word_tfidf.size()*feature_words_ratio);
			for(Entry<String, Double> word:word_tfidf.entrySet()){
				temp.add(word.getKey()+":"+word.getValue());
				i++;
				if(i>proportion){
					break;
				}
			}
			feature_words.put(url, temp);
		}
		return "";
	}
	
	
	public static void main(String args[]) throws Exception{
    	long start=System.currentTimeMillis();
		GetBaikeInfo extract=new GetBaikeInfo("国防科大",null);
		System.out.println(extract.writeMongo());
    	long pp=System.currentTimeMillis();
    	System.out.println("all:"+(pp-start));
	}
}

class PolyParallel implements Runnable {
	private String url;
	private String desc;
	Set<Document> documents=null;
	HashMap<String, HashMap<String, Triplet<Integer, Double, Double>>> docs_context=null;
	public PolyParallel(String url, Set<Document> documents, HashMap<String, HashMap<String, Triplet<Integer, Double, Double>>> docs_context) {
		this.url=url;
		this.documents=documents;
		this.docs_context=docs_context;
	}

	@Override
    public void run() {
        process();
    }

    public void process() {
		Document document = new Document();
		writeDocument(url,desc,document);
		synchronized(documents){
			if(!document.isEmpty())
				documents.add(document);
		}
    }

    private void writeDocument(String url, String desc, Document document) {
    	try {
			GetBasicInfo temp=new GetBasicInfo(url,null,null);

			int status=temp.getStatus();
//			System.out.println(url+"\t"+status);
			if(status==-1){
				return;
			}
			String word=temp.getWord();
			Date date=temp.getDate();
			document.put("Date", date);
			String title=temp.getTitle();
			if(!title.isEmpty()){//设置title
				document.put("title", title);
			}
			
			document.put("url", url);//设置url
			String default_desc=temp.getDesc();
			
			String default_alias=temp.getAlias();
			
			double prevalence=new Random().nextDouble()/1.2;//随机产生流行度
			document.append("prevalence", prevalence);//设置流行度
			String summary=temp.getSummary();
			if(!summary.isEmpty()){//设置summary
				document.put("summary", summary);
			}
			ArrayList<String> label=temp.getLabel();
			if(!label.isEmpty()){//设置label
				document.put("label", label);
			}
			List<String> keywords=temp.getKeyWords();
			if(!keywords.isEmpty()){//设置keywords
				document.put("keywords", keywords);
			}
			Document default_infobox=temp.getInforBox();
			if(!default_infobox.isEmpty()){//设置infobox
				document.put("infobox", default_infobox);
			}
			String context=temp.getContext();
			if(!context.isEmpty()){//设置context
				document.put("context", context);
			}
			if(!temp.getContext().isEmpty()){
				HashMap<String, Triplet<Integer, Double, Double>> terms=wordseg.segWord_TF(temp.getContext(), GetBaikeInfo.mode);
				synchronized (docs_context) {
					docs_context.put(url, terms);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
