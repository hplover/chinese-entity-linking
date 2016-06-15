package main.disambiguation.v4_PKBASE.statistic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import main.disambiguation.v4_PKBASE.subTools.*;

public class ReadPKBASE extends QueryPKBase {

	static int fenmu=0;
	static int fenzi=0;
	
	static int error_pinyin,error_startwith,error_equal,error_included,error_overlap,error_norelation;
	
	
	public static void main(String args[]){
		FindIterable<Document> el=ELANNOTATION.find();
		HashMap<String, String> entityName_ID=new HashMap<>();
		Set<String> propertyName=new HashSet<>();
		for(Document doc:el){
			entityName_ID=getEntityName_ID(doc);
			propertyName=getPropertyName(doc,entityName_ID);
			getResult(doc,entityName_ID,propertyName);
			entityName_ID.clear();
			propertyName.clear();
		}
		System.out.println(fenzi+"\t"+fenmu+"\t"+fenzi/(double)fenmu);
		System.out.println("error_pinyin:"+error_pinyin+"\nerror_startwith:"+error_startwith+"\nerror_equal:"+error_equal+"\nerror_included:"+error_included+"\nerror_overlap:"+error_overlap+"\nerror_norelation:"+error_norelation);
	}

	public static String Errorstatistic(String key, String rightTitle, String errorTitle) {
		if(key.equals("孙扬")){
			System.out.println();
		}
		if(PinyinEqual.full_equal(rightTitle, key)){
			error_pinyin++;
			return "pinyin";
		}
		if(key.equals(errorTitle)){
			error_equal++;
			return "equal";
		}
		if(rightTitle.startsWith(key)||key.startsWith(rightTitle)){
			error_startwith++;
			return "startwith";
		}
		
		char[] keyArray=key.toCharArray();
		Set<Character> keySet=new HashSet<>();
		for(int i=0;i<keyArray.length;i++){
			keySet.add(keyArray[i]);
		}
		char[] rightTitleArray=rightTitle.toCharArray();
		Set<Character> rightTitleSet=new HashSet<>();
		for(int i=0;i<rightTitleArray.length;i++){
			rightTitleSet.add(rightTitleArray[i]);
		}
		if(rightTitleSet.containsAll(keySet)||keySet.containsAll(rightTitleSet)){
			error_included++;
			return "included";
		}
		
		if(rightTitleSet.retainAll(keySet)){
			error_overlap++;
			return "overlap";
		}
		error_norelation++;
		return "no relation";
	}
	
	public static HashMap<String, String> getResult(Document weibo, HashMap<String, String> entityName_ID,
			Set<String> propertyName) {
		boolean isContentPrinted=true;
		String rightTitle,errorTitle;
		String error_type="";
		for(Entry<String, String> entity:entityName_ID.entrySet()){
			String key=entity.getKey();
			String exactValueID=entity.getValue();
			String queryValueID=getIDbyTitle(key);
			fenmu++;
			if(exactValueID.equals(queryValueID)){
				fenzi++;
			}
			else{
				if(isContentPrinted){
//					System.out.println("\n"+weibo.getString("content"));
					isContentPrinted=false;
				}
				rightTitle=getTitleByID(exactValueID);
				errorTitle=getTitleByID(queryValueID);
				error_type=Errorstatistic(key,rightTitle,errorTitle);
//				System.out.println(error_type+"\t"+key+"\t"+exactValueID+"\t"+rightTitle+"\t"+queryValueID+"\t"+errorTitle);
			}
		}
		return null;
	}
}
