package main.disambiguation.v3_recordEntityGetByParallel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import tools.WordSeg.wordseg;

public class Disambiguation_PrevalenceLesk {
	private double default_weight=1.2;//如果是默认的term，则文章长度加上30%
	private double prevalence_weight=0.03;//流行度的权重为0.2
	private double lesk_weight=1-prevalence_weight;//lesk的权重为0.8
	private double similarity_weight=0.0299;//相关度的阈值为0.03
	
	
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
		caculate_lesk();
		process_disambiguation();
		System.out.println(reDocuments);
		return reDocuments;
	}
	
	private void process_disambiguation() {
		if(termInfo.isEmpty()){
			return;
		}
		HashMap<String, Double> temp=new HashMap<>();
		String maxString="";
		double maxDouble=0;
		for(Entry<String, HashMap<String, Double>> entity:termInfo.entrySet()){
			temp=entity.getValue();
			for(Entry<String, Double> middle:temp.entrySet()){
				if(maxDouble<middle.getValue()){
					maxDouble=middle.getValue();
					maxString=middle.getKey();
				}
			}
			if(maxDouble>similarity_weight)
				reDocuments.put(entity.getKey(), maxString);
			maxDouble=0;
			maxString="";
		}
	}

	private void caculate_lesk() {
		if(entitySet.isEmpty()){
			return;
		}
		for(String name:nameMetioned){
			term_included(name);
		}
		System.out.println("lesk:"+termInfo);
	}

	private void term_included(String name) {
		Set<Document> terms=entitySet.get(name);
		String context="";
		int count=0;
		int len_max=1;
		HashMap<String, Double> temp_lesk=new HashMap<>();
		for(Document term:terms){
			context=term.getString("context");
			for(String name2:nameMetioned){
				if(!name.equals(name2)){
					count=count + StringUtils.countMatches(context, name2);
				}
			}
			temp_lesk.put(term.getString("title"), (double) (count));//归一化
			if(count>len_max){
				len_max=count;
			}
			count=0;
		}
		HashMap<String, Double> temp_prevalence;
		if(termInfo.get(name) != null){
			temp_prevalence=termInfo.get(name);
			for(Entry<String, Double> temp:temp_lesk.entrySet()){
				temp_lesk.put(temp.getKey(), prevalence_weight*temp_prevalence.get(temp.getKey())+lesk_weight*(double) (Math.round((temp.getValue()/len_max)*100)/100.0));//归一化);
			}
		}
		termInfo.put(name, temp_lesk);
	}

	private void caculate_prevalence() {
		if(entitySet.isEmpty()){
			return;
		}
		for(String name:nameMetioned){
			Set<Document> terms=entitySet.get(name);
			HashMap<String, Double> temp_prevalence=new HashMap<>();
			if(!terms.isEmpty()){
				int lenMax=0;
				int len=0;
				for(Document term:terms){
					len=term.getString("context").length()-23;
					if(term.getDouble("prevalence").toString().startsWith("0.81")){
						len=(int) (len*(default_weight+1));
					}
					if(len>lenMax){
						lenMax=len;
					}
					temp_prevalence.put(term.getString("title"), (double) (len));
				}
				for(Entry<String, Double> term:temp_prevalence.entrySet()){
					temp_prevalence.put(term.getKey(), (double)(Math.round((term.getValue()/lenMax)*100)/100.0));
				}
				termInfo.put(name, temp_prevalence);
			}
		}
		System.out.println("terminfo:\n"+termInfo);
	}

	//调用实例
	public static void main(String args[]){
		long s=System.currentTimeMillis();
//		String text="苹果CEO是谁？";
//		String text="游戏里鹿鼎记ol玩的人多不多的啊？";
//		String text="剑灵多少级可以发邮件";
//		String text="“你认为TFBOYS拥有最强大的粉丝群吗？“没错”  “为什么？” “因为只要是四叶草看了这条说说都会转。”";
		String text="【你有2过吗？】1、坐车下错站；2、上楼走错层；3、上学进错班；4、回家开错门；5、短信发错人；6、招呼打错人；7、熟人叫错名；8、洗澡不拿毛巾或内衣；9、买东西忘拿找钱、忘付钱、不拿东西就走人；10、走路撞树、撞门、撞电杆；11、东西在手却找个不停！ （原来我也很2）@我们都爱海贼王OP";
		Disambiguation_PrevalenceLesk bb=new Disambiguation_PrevalenceLesk(text);
		bb.getResult();
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
