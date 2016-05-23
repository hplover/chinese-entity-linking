package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.javatuples.Tuple;

import tools.WordSeg.wordseg;

public class Disambiguation_PrevalenceLesk {
	//存储结果的数据结构
	private static HashMap<String, String> reDocuments=new HashMap<>();
	
	//线程池相关
	ExecutorService executorService=Executors.newCachedThreadPool();
	
	//存储句子中所有实体的信息
	HashMap<String,Set<Document>> entitySet=new HashMap<>();
	
	//存储识别到的句子中的命名实体
	HashSet<String> nameMetioned=new HashSet<>();
	
	//存储每个term的Scroll信息
	HashMap<String, HashMap<String, Double>> termInfo=new HashMap<>();
	
	/**
	 * 获得命名实体
	 * @param text
	 */
	public Disambiguation_PrevalenceLesk(String text) {
		long f1=System.currentTimeMillis();
		nameMetioned=(HashSet<String>) wordseg.segWord_Set(text, 3);
		long f2=System.currentTimeMillis();
		System.out.println("segment: "+(f2-f1));
	}
	
	/**
	 * 并行获得命名实体的信息
	 * @return
	 */
	private HashMap<String, Set<Document>> getEntitiesInfo() {
		for(String entity:nameMetioned){
    		executorService.submit(new EntityParallel(entity,entitySet));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return entitySet;
	}
	
	/**
	 * 获取消歧结果
	 * @return
	 */
	public HashMap<String, String> getResult(){
		getEntitiesInfo();
		caculate_prevalence();
		for(Entry<String, Set<Document>> doc:entitySet.entrySet()){
			remove_ambiguation(doc,entitySet,reDocuments);
		}
		System.out.println(reDocuments);
		return reDocuments;
	}
	
	private void caculate_prevalence() {
		if(entitySet.isEmpty()){
			return;
		}
		for(String name:nameMetioned){
			Set<Document> terms=entitySet.get(name);
			HashMap<String, Double> temp_term=new HashMap<>();
			if(!terms.isEmpty()){
				int lenMax=0;
				int len=0;
				for(Document term:terms){
					len=term.getString("context").length()-23;
					if(len>lenMax){
						lenMax=len;
					}
					temp_term.put(term.getString("title"), (double) (len));
				}
				for(Entry<String, Double> term:temp_term.entrySet()){
					temp_term.put(term.getKey(), term.getValue()/lenMax);
				}
				termInfo.put(name, temp_term);
			}
		}
		
		System.out.println("terminfo:\n"+termInfo);
		
	}

	/**
	 * 消歧：简单看待消歧实体是否包含句子中其他命名实体
	 * @param doc
	 * @param otherName
	 * @param reDocuments
	 */
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
			if(tempCount>target_count){
				target_count=tempCount;
				target_title=tempTitle;
			}
		}
		if(!target_title.isEmpty())
		reDocuments.put(word, target_title);
	}
	//调用实例
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="苹果CEO是谁？";
		Disambiguation_PrevalenceLesk bb=new Disambiguation_PrevalenceLesk(text);
		bb.getResult();
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
