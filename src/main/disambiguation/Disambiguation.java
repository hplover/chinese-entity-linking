package main.disambiguation;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;

import WordSeg.wordseg;
import main.GetBaikeInfo;

public class Disambiguation {
	ExecutorService executorService=Executors.newCachedThreadPool();
	Set<Document> entities_info=new HashSet<>();
	public Disambiguation(String text) {
		HashSet<String> entities=(HashSet<String>) wordseg.segWord_Set(text, 3);
		for(String entity:entities){
    		executorService.submit(new EntityParallel(entity,entities_info));
		}
		executorService.shutdown();
	}
	public Set<Document> getEntitiesInfo() {
		return entities_info;
	}
	public static void main(String args[]){
		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation bb=new Disambiguation(text);
		System.out.println(bb.getEntitiesInfo());
	}
}
class EntityParallel implements Runnable{
	private String entity="";
	Set<Document> entities_info=null;
	public EntityParallel(String entity, Set<Document> entities_info) {
		this.entity=entity;
		this.entities_info=entities_info;
	}

	@Override
	public void run() {
		process();
	}

	private void process() {
		System.out.println("processing... "+entity+"\t"+entities_info);
		GetBaikeInfo temp;
		try {
			temp = new GetBaikeInfo(entity);
			Document xx=temp.writeMongo();
			System.out.println(xx);
			synchronized (entities_info) {
				entities_info.add(xx);
			}
		} catch (UnsupportedEncodingException e) {
			System.out.printf("failed to get %s info\n",entity);
		}
		
	}
}
