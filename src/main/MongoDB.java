package main;
import java.util.Date;

import javax.sound.sampled.Line;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;

import main.disambiguation.v3_recordEntityGetByParallel.GetBaikeInfo;

//import main.disambiguation.

/**
 * @author HuangDC
 * @date 2016年4月3日
 * @description 
 */
public class MongoDB {
//	static PrintStream bb=System.out;

	public static void main(String[] args) throws ParseException, UnsupportedEncodingException, IOException, InterruptedException{
		String file="e://entityOut_v2";
//		MongoCredential credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
//		MongoClient mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	//3006设置为Mongodb端口号
//		MongoDatabase db = mongoClient.getDatabase("Plover");
//		System.out.println(db);
//		for (String name : db.listCollectionNames()) {
//			System.out.println("collection:"+name);
//	  	} 
//        System.out.println();
       
//       Document find=new Document();
//       find.put("word", "锁定");
////       find.put("default_alias", "中国人民解放军");
//       FindIterable<Document> xx=collection.find(find);
//       for(Document bb:xx){
//    	   System.out.println(bb);
//       }
       
       
//     //获取指定Collection中所有的索引
//       for (final Document index : collection.listIndexes()) {
//       	    System.out.println(index.toJson());
//      	}
//     //检索出所有数据
//       collection.deleteMany(new Document());
//       long i=0;
//		for (Document cur : collection.find()) {
//			System.out.println(cur.toJson());
//			i++;
////			System.out.println(i);
////			if(i%100==10){
////				break;
////			}
//      	}
//		System.out.println(i);
		
//       try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//    	    String line;
//    	    ExecutorService executorService=Executors.newFixedThreadPool(3);
//    	    while ((line = br.readLine()) != null) {
//    	    	executorService.submit(new CrawlBaikeParallel(line.trim(),collection));
//    		}
//    		executorService.shutdown();
//    		try {
//                executorService.awaitTermination(1, TimeUnit.DAYS);
//            } catch (InterruptedException ex) {
//                System.out.println(ex.getMessage());
//            }
//    	}
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("filename.txt"), "utf-8"));
//		new GetBaikeInfo("国防科大",null).writeMongo();
		
		String line = null;
    	int i=1;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    		while ((line = br.readLine()) != null) {
    			i++;
    	    	if(i==505244){
    	    		break;
    	    	}
    	    }
    	    while ((line = br.readLine()) != null) {
    	    	System.out.println(line);
    	    	new GetBaikeInfo(line,null).writeMongo();
    	    	System.out.println(new Date()+"\tinsert\t" + line+"\t"+(i++));
//    	    	Thread.sleep(1000);
    		}
    	}
		finally {
			System.out.println(line+"\t"+i);
			writer.write(line+"\t"+i+"\n");
		}
	}
}
//class CrawlBaikeParallel implements Runnable{
//	static int i=1;
//	String line="";
//	MongoCollection<Document> collection=null;
//	public CrawlBaikeParallel(String line,MongoCollection<Document> collection) {
//		this.line=line;
//		this.collection=collection;
//	}
//	@Override
//	public void run() {
//		process();
//	}
//	void process(){
//		GetBaikeInfo temp;
//		try {
//			temp = new GetBaikeInfo(line,null);
//			synchronized (collection) {
//				temp.writeMongo(collection);
//				System.out.println(new Date()+"\tinsert " + line+"\t"+(i++));
//			}
//		} catch (UnsupportedEncodingException e) {
//			System.err.println("processing error "+ line);
//		}
//	}
//}