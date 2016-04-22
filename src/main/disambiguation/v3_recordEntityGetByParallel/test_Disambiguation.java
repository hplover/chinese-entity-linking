package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.print.Doc;

import java.util.Set;

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
			System.setOut(new PrintStream(new File("F:\\javaout")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		
	//pair-wise??
	public static void remove_ambiguation(Document doc,Set<Document> documents, HashMap<String, String> reDocuments){
		String word=doc.getString("word");
		String default_url=doc.getString("default_url");
		String default_context=doc.getString("default_context");
		@SuppressWarnings("unchecked")
		ArrayList<String> bb=(ArrayList<String>) doc.get("default_label");
		System.out.println("list:"+bb);
		int status=doc.getInteger("status");
		if(status==-1){
			return ;
		}
//		int target=0;
//		String target_url=default_url;
//		for(Document adoc:documents){
//			if(word!=adoc.getString("word")){
//				target=target+StringUtils.countMatches(default_context,adoc.getString("word"));
//			}
//		}
//		if(status==5){
//			if(target!=0){
//				reDocuments.put(word, target_url);
//				return;
//			}
//			return;
//		}
		@SuppressWarnings("unchecked")
		ArrayList<Document> polys=(ArrayList<Document>) doc.get("Polysemant");//convert failed
		System.out.println(polys);
		@SuppressWarnings("unchecked")
		Document infobox=(Document)doc.get("default_infobox");
		for(Entry<String, Object> jk:infobox.entrySet()){
			System.out.println(jk.getKey()+"\t"+jk.getValue());
			@SuppressWarnings("unchecked")
			ArrayList<String> bb1=(ArrayList<String>) jk.getValue();
			for(String iii:bb1){
				System.out.println("contetn:::::"+iii);
			}
		}
//		System.out.println(infobox);
//		String poly_context=null;
//		String poly_url=null;
//		for(Document poly:polys){
//			//需要改进的地方：计算实体与实体之间的关系后在决定对应的是哪个实体
//			poly_context=poly.getString("poly_context");
//			poly_url=poly.getString("poly_url");
//			int target_temp=0;
//			for(Document adoc:documents){
//				if(word!=adoc.getString("word")){
//					target_temp=target_temp+StringUtils.countMatches(poly_context,adoc.getString("word"));
//				}
//			}
//			if(target_temp>target){
//				target=target_temp;
//				target_url=poly_url;
//			}
//		}
//		reDocuments.put(word, target_url);
		return;
	}
		
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="凤凰";
		Disambiguation bb=new Disambiguation(text);
		Set<Document> documents=bb.getEntitiesInfo();
		System.out.println("=====================");
		System.out.println("document:"+documents);
//		System.out.println(HanLP.parseDependency(text));
		for(Document doc:documents){
			remove_ambiguation(doc,documents,reDocuments);
		}
		
//		for(java.util.Map.Entry<String, String> biubiu:reDocuments.entrySet()){
//			System.out.println(biubiu.getKey()+"\t"+biubiu.getValue());
//		}
		
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
	
	
	
	
	
}
