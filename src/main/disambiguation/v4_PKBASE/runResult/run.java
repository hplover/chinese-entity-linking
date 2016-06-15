package main.disambiguation.v4_PKBASE.runResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import main.disambiguation.v4_PKBASE.statistic.QueryPKBase;
import tools.WordSeg.wordseg;

import org.bson.Document;

import com.mongodb.client.FindIterable;

public class run {
	public static void main(String[] args){
		FindIterable<Document> el=QueryPKBase.ELANNOTATION.find();
		HashMap<String, String> entityName_ID=new HashMap<>();
		Set<String> propertyName=new HashSet<>();
		HashMap<String, String> result=new HashMap<>();
		for(Document doc:el){
			entityName_ID=QueryPKBase.getEntityName_ID(doc);
			propertyName=QueryPKBase.getPropertyName(doc,entityName_ID);
			result=queryResult(entityName_ID, propertyName);
			
			result.clear();
			entityName_ID.clear();
			propertyName.clear();
		}
	}
	
	
	public static HashMap<String, String> queryResult(HashMap<String, String> entityName_ID, Set<String> property){
		HashMap<String, String> result=new HashMap<>();
		FindIterable<Document> entities=QueryPKBase.PKBASE.find();
		for(Entry<String, String> entity:entityName_ID.entrySet()){
			String name=entity.getKey();
			xiaoqi(name, property);
		}
		return result;
	}
	
	public static boolean xiaoqi(String name,Set<String> context){
		Document entity=QueryPKBase.PKBASE.find(new Document("title_unique",name)).first();
		if(entity==null||entity.isEmpty()){
			return false;
		}
		else{
			Set<String> keys=entity.keySet();
			Set<String>	values=new HashSet<>();
			Set<String> vv=new HashSet<>();
			for(String key:keys){
				if(key.equals("id")||key.equals("title_unique")||key.equals("_id")){
					continue;
				}
				@SuppressWarnings("unchecked")
				ArrayList<String> value=(ArrayList<String>) entity.get(key);
				vv.addAll(value);
				for(String v:value)
					values.addAll(wordseg.segWord_Set(v, 3));
			}
			//todo:caculate the similarity of two sets
			int len1=context.size();
			boolean jiaoji=context.retainAll(values);
			int len2=context.size();
			System.out.println();
		}
		return false;
	}
	
	
	
}
