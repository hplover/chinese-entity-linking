package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import main.disambiguation.v3_recordEntityGetByParallel.Disambiguation_PrevalenceLesk;

public class writeQuestion2MongoDB {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<Document> test_collection =null;

	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("test_database");
		test_collection = db.getCollection("test_collection");
	}
	public static void main(String args[]) throws IOException {
		HashMap<String, Set<Document>> entityInfo;
		@SuppressWarnings("resource")
		BufferedReader bufferedReader=new BufferedReader(new FileReader("resources/test_weibo_data"));
		String line;
		Disambiguation_PrevalenceLesk bb;
		while((line=bufferedReader.readLine()) != null){
			bb=new Disambiguation_PrevalenceLesk(line);
			entityInfo=bb.getEntitiesInfo();
			write2mongodb(line,entityInfo);
		}
	}

	private static void write2mongodb(String line, HashMap<String, Set<Document>> entityInfo) {
		Document final_doc=new Document();
		final_doc.append("question", line);
		Document entity=new Document();
		String entity_name="";
		Set<Document> terms=new HashSet<>();
		for(Entry<String, Set<Document>> entityDoc:entityInfo.entrySet()){
			Document entity_terms=new Document();
			entity_name=entityDoc.getKey();
			terms=entityDoc.getValue();
			for(Document term:terms){
				entity_terms.append(term.getString("title").replace(".",""), term.getString("summary"));
			}
			entity.append(entity_name.replace(".", ""), entity_terms);
		}
		final_doc.append("entity", entity);
		System.out.println("inserting into mongodb...\n"+final_doc);
		test_collection.insertOne(final_doc);
	}
}
