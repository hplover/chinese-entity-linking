package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import tools.WordSeg.wordseg;
import tools.javatuples.Tuple;

public class Disambiguation {
	ExecutorService executorService=Executors.newCachedThreadPool();
	HashMap<String,Set<Document>> entitySet=new HashMap<>();
	HashSet<String> nameMetioned=new HashSet<>();
	public Disambiguation(String text) {
		long f1=System.currentTimeMillis();
		nameMetioned=(HashSet<String>) wordseg.segWord_Set(text, 3);
		long f2=System.currentTimeMillis();
		System.out.println("segment: "+(f2-f1));
	}
	public HashMap<String, Set<Document>> getEntitiesInfo() {
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
		
	
	public static void main(String args[]){
		long s=System.currentTimeMillis();
		String text="国防科大 苹果 四叶草";
		Disambiguation bb=new Disambiguation(text);
		System.out.println("entities info is "+bb.getEntitiesInfo());
		long e=System.currentTimeMillis();

//		Disambiguation cc=new Disambiguation("四叶草");
//		System.out.println("entities info is "+cc.getEntitiesInfo());
//		long j=System.currentTimeMillis();
		System.out.println("1 time: "+(e-s));
//		System.out.println("2 time: "+(j-e));
	}
}
class EntityParallel implements Runnable{
	private String entity="";
	HashMap<String, Set<Document>> entities_info=new HashMap<>();
	public EntityParallel(String entity, HashMap<String, Set<Document>> entitySet) {
		this.entity=entity;
		this.entities_info=entitySet;
	}

	@Override
	public void run() {
		process();
	}

	private void process() {
		GetBaikeInfo temp;
		try {
			temp = new GetBaikeInfo(entity,null);
//			System.out.println(xx);
			synchronized (entities_info) {
				Set<Document> xx=temp.writeMongo();
				entities_info.put(entity, xx);
			}
//			System.out.println(entities_info);
		} catch (UnsupportedEncodingException e) {
			System.out.printf("failed to get %s info\n",entity);
		}
		
	}
}
