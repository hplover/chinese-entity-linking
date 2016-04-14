package main.disambiguation.v2;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import tools.WordSeg.wordseg;

public class Disambiguation {
	ExecutorService executorService=Executors.newCachedThreadPool();
	Set<Document> entities_info=new HashSet<>();
	HashSet<String> entities=null;
	int entity_size=0;
	public Disambiguation(String text) {
		long f1=System.currentTimeMillis();
		entities=(HashSet<String>) wordseg.segWord_Set(text, 3);
		entity_size=entities.size();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
		long f2=System.currentTimeMillis();
		System.out.println("segment: "+(f2-f1));
	}
	public Set<Document> getEntitiesInfo() {
		for(String entity:entities){
    		executorService.submit(new EntityParallel(entity,entities_info));
		}
		executorService.shutdown();
		try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
		return entities_info;
	}
	
	@SuppressWarnings("unchecked")
	public void process(){
		if(entities_info.isEmpty()){
			return ;
		}
		int status;
		HashSet<String> feature_words=null;
		int featuresize=0;
		for(Document entity:entities_info){
			status=(int) entity.get("status");
			if(status==5){
				try {
					feature_words=(HashSet<String>) entity.get("default_features");
					featuresize=feature_words.size();
					feature_words.addAll(entities);
					if(entity_size+featuresize-feature_words.size()>0){
						
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		}
	}
	
	
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation bb=new Disambiguation(text);
		System.out.println("entities info is "+bb.getEntitiesInfo());
		long e=System.currentTimeMillis();
		System.out.println("all time: "+(e-s));
	}
}
class EntityParallel implements Runnable{
	private String entity="";
	Set<Document> entities_info=new HashSet<>();
	public EntityParallel(String entity, Set<Document> entities_info) {
		this.entity=entity;
		this.entities_info=entities_info;
	}

	@Override
	public void run() {
		process();
	}

	private void process() {
		GetBaikeInfo temp;
		try {
			temp = new GetBaikeInfo(entity);
			Document xx=temp.writeMongo();
//			System.out.println(xx);
			synchronized (entities_info) {
				entities_info.add(xx);
			}
//			System.out.println(entities_info);
		} catch (UnsupportedEncodingException e) {
			System.out.printf("failed to get %s info\n",entity);
		}
		
	}
}
