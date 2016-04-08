package main;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.BasicDBObject;

import WordSeg.wordseg;

public class GetBaikeInfo extends GetBasicInfo{
    public static List<BasicDBObject> documents=new ArrayList<>();
    public static List<HashMap<String, Double>> tfs=new ArrayList<>();
	public GetBaikeInfo(String word) throws UnsupportedEncodingException{
		super(word);
	}
	public List<Document> getPolysemantParallel() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> polys=super.getPoly();
		if(polys.isEmpty()){
			return null;
		}
		int polysize=polys.size();
		ExecutorService executorService=Executors.newFixedThreadPool(polysize);
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
		System.out.println(documents);
		return null;
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
    	System.out.println("time:"+extract.getDate());
    	System.out.println("word:"+extract.getWord());
    	System.out.println("status:"+extract.getStatus());
    	System.out.println("url:"+extract.getURL());
    	System.out.println("desc:"+extract.getDesc());
    	System.out.println("synonym:"+extract.getSynonym());
    	System.out.println("summary:"+extract.getSummary());
//    	System.out.println("content:"+extract.getContext());
    	System.out.println("=============================================");
    	System.out.println("label:"+extract.getLabel());
    	System.out.println("infobox:"+extract.getInforBox());
    	System.out.println("polysemy:"+extract.getPoly().size()+" "+extract.getPoly().toString());
    	long mu=System.currentTimeMillis();
    	extract.getPolysemantSequence();
    	long middle=System.currentTimeMillis();

    	extract.getPolysemantParallel();
    	long pp=System.currentTimeMillis();
    	System.out.println("default:"+(mu-start));
    	System.out.println("sequence:"+(middle-mu));
    	System.out.println("parallel:"+(pp-middle));
    	System.out.println("all:"+(pp-start));
	}
}

class PolyParallel implements Runnable {
    private final Object lock = new Object();
	private String url;
	private String desc;
	private static int mode=3;

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
		HashMap<String, Double> tf=new HashMap<>();
		writeDocument(url,desc,document,terms,tf);
		synchronized(lock){
			GetBaikeInfo.documents.add(document);
			GetBaikeInfo.tfs.add(tf);
		}
    }

    private void writeDocument(String url, String desc, BasicDBObject document, List<String> terms, HashMap<String, Double> tf) {
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
				tf=wordseg.segWord_TF(temp.getContext(), mode);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
