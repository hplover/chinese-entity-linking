package main.disambiguation.v4_PKBASE.runResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import main.disambiguation.v4_PKBASE.statistic.QueryPKBase;
import main.disambiguation.v4_PKBASE.subTools.Word2Vec;
import tools.WordSeg.wordseg;

import org.bson.Document;

import com.mongodb.client.FindIterable;

public class run {
	private static double entitySimilarity=0.6;

	public static void main(String[] args){
		FindIterable<Document> pkBase=QueryPKBase.ELANNOTATION.find();
		HashMap<String, String> entityName_ID=new HashMap<>();
		Set<String> context=new HashSet<>();
		HashMap<String, String> result=new HashMap<>();
		for(Document doc:pkBase){
			entityName_ID=QueryPKBase.getEntityName_ID(doc);
			context=QueryPKBase.getContext(doc,entityName_ID);
			result=queryResult(entityName_ID, context);//准确查询，模糊查询
			
			result.clear();
			entityName_ID.clear();
			context.clear();
		}
	}
	
	
	public static HashMap<String, String> queryResult(HashMap<String, String> entityName_ID, Set<String> context){
		HashMap<String, String> result=new HashMap<>();
		String relevant;
		for(Entry<String, String> entity:entityName_ID.entrySet()){
			String name=entity.getKey();
			relevant=exactlyRelevant(name, context);
			if(relevant.equals("NIL")){
				//todo:模糊匹配出可能结果，拼音相似，简称，别名
			}
			else{
				result.put(name, relevant);
			}
		}
		return result;
	}
	
	public static String exactlyRelevant(String name,Set<String> context){
		Document entity=QueryPKBase.PKBASE.find(new Document("title_unique",name)).first();
		if(entity==null||entity.isEmpty()){
			return "NIL";
		}
		else{
			//todo:判断是否有关，是否指的就是这个
			Set<String> property_keys=entity.keySet();//获得实体的属性名字
			Set<String>	property_values=new HashSet<>();//实体的属性值
			for(String key:property_keys){
				if(key.equals("id")||key.equals("title_unique")||key.equals("_id")){
					continue;
				}
				@SuppressWarnings("unchecked")
				ArrayList<String> value=(ArrayList<String>) entity.get(key);
				for(String v:value)
					property_values.addAll(wordseg.segWord_Set(v, 3));//属性值分词
			}
			//todo:caculate the similarity of two sets
			double compareValue=Word2Vec.twoSetsDis(context, property_values);
			if(compareValue>entitySimilarity){
				return entity.getString("id");
			}
		}
		return "NIL";
	}
	
	
	
}
