package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.print.Doc;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

/**
 * error:can't convert to the wanted type
 * @author HuangDC
 *
 */


public class test_Disambiguation {
	static HashMap<String, String> reDocuments=new HashMap<>();
	static{
		try {
			System.setOut(new PrintStream(new File("E:\\javaout")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		
	//简单的看是否包含在网页正文中
	public static void remove_ambiguation(Entry<String, Set<Document>> doc,HashMap<String, Set<Document>> otherName, HashMap<String, String> reDocuments){
		String word=doc.getKey();
		Set<Document> entitySet=doc.getValue();
		String target_title = "";
		int target_count=0;
		for(Document entity:entitySet){
			String context=entity.getString("context");
			int tempCount=0;
			String tempTitle=entity.getString("title");
			for(Entry<String, Set<Document>> name:otherName.entrySet()){
				if(!word.equals(name.getKey())){
					tempCount=tempCount+StringUtils.countMatches(context, name.getKey());
				}
			}
			if(tempCount>=target_count){
				target_count=tempCount;
				target_title=tempTitle;
			}
		}
		if(!target_title.isEmpty())
		reDocuments.put(word, target_title);
	}
		
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="元楼演过的电影";//{电影=电影（世界图书出版公司出版图书）, 元楼=成龙（中国香港演员、导演）}
		
		Disambiguation bb=new Disambiguation(text);
		HashMap<String, Set<Document>> documents=bb.getEntitiesInfo();
		System.out.println("=====================");
//		System.out.println("document:"+documents);
		for(Entry<String, Set<Document>> doc:documents.entrySet()){
			remove_ambiguation(doc,documents,reDocuments);
		}
		
		System.out.println(reDocuments);
		
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
	
	
	
	
	
}
