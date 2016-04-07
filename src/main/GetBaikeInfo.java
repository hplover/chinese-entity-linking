package main;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bson.Document;

public class GetBaikeInfo extends GetBasicInfo{
	public GetBaikeInfo(String word) throws UnsupportedEncodingException{
		super(word);
	}
	
	public List<Document> getPolysement() throws UnsupportedEncodingException{
		LinkedHashMap<String, String> poly=super.getPoly();
		if(poly.isEmpty()){
			return null;
		}
		Iterator<Entry<String, String>> iterator=poly.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, String> aa=iterator.next();
			GetBasicInfo temp=new GetBasicInfo(aa.getValue());
			System.out.println(aa.getKey()+" "+aa.getValue());
		}
		return null;
	}
	
	
	
	
	public static void main(String args[]) throws Exception{
    	long start=System.currentTimeMillis();
		GetBaikeInfo extract=new GetBaikeInfo("四叶草");
    	System.out.println("time:"+extract.getDate());
    	System.out.println("first:");
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
    	extract.getPolysement();
    	long middle=System.currentTimeMillis();
    	
    	System.out.println("first:"+(middle-start));
	}
}
