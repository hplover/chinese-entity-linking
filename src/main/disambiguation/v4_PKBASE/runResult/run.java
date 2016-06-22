package main.disambiguation.v4_PKBASE.runResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import main.disambiguation.v4_PKBASE.statistic.QueryPKBase;
import main.disambiguation.v4_PKBASE.subTools.PinyinSim;
import main.disambiguation.v4_PKBASE.subTools.Word2Vec;
import tools.WordSeg.wordseg;

import org.bson.Document;
import org.xml.sax.SAXException;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.mongodb.client.FindIterable;

public class run {
	private static double entitySimilarity=0.0;
	private static double threshold1=0.0;
	static public double step=0.05;
	static public List<String> loc_suffix=new ArrayList<>();
	static{
		loc_suffix.add("省");
		loc_suffix.add("市");
		loc_suffix.add("区");
		loc_suffix.add("县");
		loc_suffix.add("镇");
		loc_suffix.add("村");
		loc_suffix.add("乡");
	}

	public static void main(String[] args){
		HashMap<String, String> entityName_ID=new HashMap<>();
		Set<String> context=new HashSet<>();
		HashMap<String, String> result=new HashMap<>();
		boolean nan;
		for(;entitySimilarity<0.3;entitySimilarity+=step){
		for(threshold1=0.0;threshold1<=entitySimilarity;threshold1+=step){
		long start=System.currentTimeMillis();
		FindIterable<Document> pkBase=QueryPKBase.ELANNOTATION.find();
		for(Document doc:pkBase){
			entityName_ID=QueryPKBase.getEntityName_ID(doc);
			context=QueryPKBase.getContext(doc,entityName_ID);
			result=queryResult(entityName_ID, context);//准确模糊查询，拼音查询。待：百度
			nan=resultStatistic.resultJudge(doc,entityName_ID, result);
//			if(!nan){
//				System.out.println(doc.getString("content"));//把微博内容传入resultJudge
//				System.out.println("right:"+entityName_ID);
//				System.out.println("wrong:"+result);
//			}
			result.clear();
			entityName_ID.clear();
			context.clear();
		}
		if(resultStatistic.printresult()>=0.6534){
			System.out.println("*********************************");
		}
		resultStatistic.init();
		System.out.println(entitySimilarity+"\t"+threshold1);
		long end=System.currentTimeMillis();
		System.out.println("time:"+(end-start));
		}}
	}
	
	
	public static HashMap<String, String> queryResult(HashMap<String, String> entityName_ID, Set<String> context){
		HashMap<String, String> result=new HashMap<>();
		String relevant;
		for(Entry<String, String> entity:entityName_ID.entrySet()){
			String name=entity.getKey();
//			if(name.equals("青岩")){
//				System.out.println();
//			}
			relevant=exactlyIncludedRelevant(name, context);//完全匹配或者包含的情况
			if(!relevant.equals("NIL")){
				//TODO:模糊匹配出可能结果，拼音相似，简称，别名
				result.put(name, relevant);
				continue;
			}
			relevant=pinyinRelevant(name, context);//只有拼音匹配的情况
			if(!relevant.equals("NIL")){
				result.put(name, relevant);
				continue;
			}
			result.put(name, "NIL");
		}
		return result;
	}
	
	private static String pinyinRelevant(String name, Set<String> context) {
		String pinyin=PinyinHelper.convertToPinyinString(name, " ", PinyinFormat.WITHOUT_TONE);
		Set<String> entity_name=new HashSet<>();
		try {
			PinyinSim.loadPinyin();//加载实体拼音
			entity_name=PinyinSim.queryPinyin(pinyin);//返回拼音相同的实体名字
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return remove_ambigous(entity_name,context);
	}


	private static String remove_ambigous(Set<String> entity_name, Set<String> context) {
		if(entity_name.isEmpty()){
			return "NIL";
		}
		Document entity=new Document();
		Set<String> property_value=new HashSet<>();
		double max=0,temp;
		String result = "NIL";
		for(String name:entity_name){//取最大的
			entity=QueryPKBase.PKBASE.find(new Document("title_unique",name)).first();
			property_value=getPropertyValue(entity);
			temp=Word2Vec.twoSetsDis(property_value, context, threshold1);
			if(max<temp){
				max=temp;
				result=entity.getString("id");
			}
			property_value.clear();
		}
		if(max<entitySimilarity){
			return "NIL";
		}
		return result;
	}


	public static String exactlyIncludedRelevant(String name,Set<String> context){
		Document entity=QueryPKBase.PKBASE.find(new Document("title_unique",name)).first();
		if(entity!=null&&!entity.isEmpty()){
			Set<String> property_values=getPropertyValue(entity);
			double compareValue=Word2Vec.twoSetsDis(context, property_values,threshold1);
			if(compareValue>entitySimilarity){
				return entity.getString("id");
			}
		}
				
		Document filter=new Document();
		String regex="\\w*"+name+"\\w*";
		filter.put("title_unique", java.util.regex.Pattern.compile(regex));
		FindIterable<Document> resultsFuzzy=QueryPKBase.PKBASE.find(filter);
		Set<String> property_value=new HashSet<>();
		double max=0,temp;
		String result = "NIL";
		for(Document fuzzy:resultsFuzzy){
			for (String a:loc_suffix) {
				if(fuzzy.getString("title_unique").length()-1==name.length())
				if(fuzzy.getString("title_unique").endsWith(a)){
					max=0.5;
					try {
						result=fuzzy.getString("id");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(">>>>>>>>>>>>>>>>"+fuzzy.getString("title_unique"));
					}
					
					break;
				}
			}
			property_value=getPropertyValue(fuzzy);
			temp=Word2Vec.twoSetsDis(property_value, context,threshold1);
			if(max<temp){
				max=temp;
				result=fuzzy.getString("id");
			}
			property_value.clear();
		}
//		if(!result.equals("NIL")){
//			Document bDocument=QueryPKBase.PKBASE.find(new Document("title_unique",result)).first();
//			return bDocument.getString("id");
//		}
		if(max<entitySimilarity){
			return "NIL";
		}
		
		return result;
	}
	
	public static String exactlyIncludedRelevant_v2(String name,Set<String> context){
		Document entity=QueryPKBase.PKBASE.find(new Document("title_unique",name)).first();
		if(entity!=null&&!entity.isEmpty()){
			Set<String> property_values=getPropertyValue(entity);
			//todo:caculate the similarity of two sets
			double compareValue=Word2Vec.twoSetsDis(context, property_values,threshold1);
			if(compareValue>entitySimilarity){
				return entity.getString("id");
			}
		}
		
		
		Document filter=new Document();
		String regex="\\w*"+name+"\\w*";
		filter.put("title_unique", java.util.regex.Pattern.compile(regex));
		FindIterable<Document> resultsFuzzy=QueryPKBase.PKBASE.find(filter);
		Set<String> property_value=new HashSet<>();
		double max=0,temp;
		String result = "NIL";
		for(Document fuzzy:resultsFuzzy){
			for (String a:loc_suffix) {
				if(fuzzy.getString("title_unique").length()-1==name.length())
				if(fuzzy.getString("title_unique").endsWith(a)){
					max=0.5;
					try {
						result=fuzzy.getString("id");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(">>>>>>>>>>>>>>>>"+fuzzy.getString("title_unique"));
					}
					
					break;
				}
			}
			property_value=getPropertyValue(fuzzy);
			temp=Word2Vec.twoSetsDis(property_value, context,threshold1);
			if(max<temp){
				max=temp;
				result=fuzzy.getString("id");
			}
			property_value.clear();
		}
//		if(!result.equals("NIL")){
//			Document bDocument=QueryPKBase.PKBASE.find(new Document("title_unique",result)).first();
//			return bDocument.getString("id");
//		}
		if(max<entitySimilarity){
			return "NIL";
		}
		
		return result;
	}


	private static Set<String> getPropertyValue(Document entity) {
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
		return property_values;
	}
	
	
	
}
