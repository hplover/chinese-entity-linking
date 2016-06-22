package main.disambiguation.v4_PKBASE.runResult;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.management.Query;

import org.bson.Document;

import main.disambiguation.v4_PKBASE.statistic.QueryPKBase;
import main.disambiguation.v4_PKBASE.statistic.ReadPKBASE;

public class resultStatistic {
	static int fenmu=0;
	static int fenzi=0;
	
	 
	public static boolean resultJudge(Document doc, HashMap<String, String> r,HashMap<String, String> w){
		boolean jj=true;
		if(r.size()!=w.size()){
			System.out.println("error in static ");
			return false;
		}
		String name,rid,wid,right,wrong,error_type;
		for(Entry<String , String> bb:r.entrySet()){
			name=bb.getKey();
			rid=bb.getValue();
			wid=w.get(name);
			if(!rid.equals(wid)){
				if(jj){
					System.out.println(doc.getString("content"));
					jj=false;
				}
				right=QueryPKBase.getTitleByID(rid);
				wrong=QueryPKBase.getTitleByID(wid);
				error_type=ReadPKBASE.Errorstatistic(name, right, wrong);
				System.out.println(error_type+"\t"+name+"\t"+rid+"\t"+right+"\t"+wid+"\t"+wrong);
			}
			else{
				fenzi++;
			}
			fenmu++;
		}
		return jj;
	}
	public static double printresult(){
		System.out.println("fenzi:"+fenzi);
		System.out.println("fenmu:"+fenmu);
		ReadPKBASE.printError();
		double result=(double)(fenzi)/fenmu;
		System.out.println(result);
		return result;
	}
	
	public static void init() {
//		System.out.println("fenmu:"+fenmu+"\tfenzi:"+fenzi);
		fenmu=0;
		fenzi=0;
	}
}
