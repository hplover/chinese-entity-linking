package main.disambiguation.v4_PKBASE;        

import java.io   .UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.javatuples.Triplet;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import tools.WordSeg.wordseg;
/**
 * @since 2016年4月14日
 * @author HuangDC
 * @desc 并行爬取多义词信息，整合后计算tfidf值，然后把爬取和计算的信息写入数据库；更新数据库中的实体表、同义词表、多义词表、索引表
 */
public class GetBaikeInfo extends GetBasicInfo{
    Set<Document> documents=new HashSet<>();//存储多义词信息
    
    //计算tfidf需要的数据结构，存储每个word对应的实体的网页正文中的名词的tfidf值
    HashMap<String, HashMap<String, Triplet<Integer, Double, Double>>> docs_context=new HashMap<>();
    
    HashSet<Document> SetEntity=null;//存储word对应的每个实体的信息
    HashMap<String, Set<String>> feature_words=new HashMap<>();//每个实体对应的特征值（网页正文中名词的tfidf值）
    private double feature_words_ratio=1;//获取百分之一百的网页名词
	private boolean isSetEntity;//判断SetEntity是否有内容
	
	static int mode=3;//设置分词模式，3位CRF分词
	
	//以下static为数据库连接信息
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<Document> collection_entity =null;
	static MongoCollection<Document> collection_synonym =null;
	static MongoCollection<Document> collection_polysemy =null;
	static MongoCollection<Document> collection_index =null;
	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("Plover");
		collection_entity = db.getCollection("BaikeEntity");
		collection_synonym = db.getCollection("BaikeSynonym");
		collection_polysemy = db.getCollection("BaikePolysemy");
		collection_index=db.getCollection("entityIndex");
	}
	
	//判断输入的词是否存在在数据库中，若存在则把实体信息存在SetEntity中，若不存在则爬取百科
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
	
	//并行爬取多义词信息，并把爬取的信息存到documents中
	private Set<Document> getPolysemantParallel() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();//获取多义词的url
		if(polys.isEmpty()){
			return documents;
		}
		ExecutorService executorService=Executors.newCachedThreadPool();
		String url="";
		for(Entry<String, String> poly:polys.entrySet()){
    		url=poly.getValue();
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
	
	
	//整合多个多义词信息并存入数据库
	public Set<Document> writeMongo(){
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
		//根据输入的word的信息更新索引表、同义词表、多义词表
		if(!word.equals(title)||!word.equals(title.toUpperCase())){
			if(status==1){
				collection_index.insertOne(new Document("word",word).append("type", "polysemy"));
				collection_index.insertOne(new Document("word",title).append("type", "entity"));
				collection_polysemy.insertOne(new Document("word",word).append("entity", title));
				if(!title.endsWith("）")){
					collection_synonym.insertOne(new Document("word",word).append("entity", title));
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
		
		//以下操作为更新实体表：
		
		Document entitydoc=new Document();//构建实体表中的每条记录
		
		entitydoc.put("Date", date);
		if(!title.isEmpty()){
			entitydoc.put("title", title);
		}

		String url=super.getURL();
		if(!url.isEmpty()){
			entitydoc.put("url", url);
		}

		double prevalence=0.9/1.1;
		entitydoc.append("prevalence", prevalence);
		String summary=super.getSummary();
		if(!summary.isEmpty()){
			entitydoc.put("summary", summary);
		}
		ArrayList<String> label=super.getLabel();
		if(!label.isEmpty()){
			entitydoc.put("label", label);
		}
		List<String> keywords=super.getKeyWords();
		if(!keywords.isEmpty()){
			entitydoc.put("keywords", keywords);
		}
		Document infobox=super.getInforBox();
		if(!infobox.isEmpty()){
			entitydoc.put("infobox", infobox);
		}
		String context=super.getContext();
		if(!context.isEmpty()){
			entitydoc.put("context", context);
		}
		Set<Document> Polysemant;
		try {
			Polysemant = getPolysemantParallel();//并行爬取多义词信息，并存入documents
			try {
				getTFIDF();//计算TFIDF值
				Set<String> feature=feature_words.get(url);
				if(!feature.isEmpty())
				entitydoc.put("feature", feature_words.get(url));
				try {
					collection_entity.insertOne(entitydoc);//写实体表
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
							collection_index.insertOne(new Document("word",title).append("type", "entity"));//更新索引表
							collection_entity.insertOne(doc);
							collection_polysemy.insertOne(new Document("word",word).append("entity", title));//更新多义词表
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
	
	//查看某个词的多义词
	public static List<String> getSynonym(String word) {
		Document synonym=collection_synonym.find(new Document("word",word)).first();
		if(!synonym.isEmpty()){
			List<String> results=new ArrayList<>();
			FindIterable<Document> re=collection_synonym.find(new Document("entity",synonym.get("entity")));
			for(Document doc:re){
				results.add(doc.getString("word"));
			}
			return results;
		}		
		return null;
	}
	
	//判断输入的word是否存在于数据库中，若返回true，并同时更新entitySet，该集合存了word对应的所有实体的信息（包含对应的同义词）
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
					Document partRe=collection_entity.find(new Document("title",re_synonym.get("entity"))).first();
					partRe.append("synonym", getSynonym(word));
					entitySet.add(partRe);
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
	
	
	//计算TFIDF
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
		GetBaikeInfo extract=new GetBaikeInfo("四叶草",null);
		System.out.println(extract.writeMongo());
    	long pp=System.currentTimeMillis();
    	System.out.println("all:"+(pp-start));
	}
}



/**
 * 爬取每个多义词实体的信息并写入数据库
 */
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
			if(status==-1){
				return;
			}
			String word=temp.getWord();
			Date date=temp.getDate();
			document.put("Date", date);
			String title=temp.getTitle();
			if(!title.isEmpty()){
				document.put("title", title);
			}
			
			document.put("url", url);
			double prevalence=new Random().nextDouble()/1.2;
			document.append("prevalence", prevalence);
			String summary=temp.getSummary();
			if(!summary.isEmpty()){
				document.put("summary", summary);
			}
			ArrayList<String> label=temp.getLabel();
			if(!label.isEmpty()){
				document.put("label", label);
			}
			List<String> keywords=temp.getKeyWords();
			if(!keywords.isEmpty()){
				document.put("keywords", keywords);
			}
			Document default_infobox=temp.getInforBox();
			if(!default_infobox.isEmpty()){
				document.put("infobox", default_infobox);
			}
			String context=temp.getContext();
			if(!context.isEmpty()){
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
