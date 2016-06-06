package xml2mongodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class KB2mongodb {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<org.bson.Document> weibo_collection =null;

	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("weibo");
		weibo_collection = db.getCollection("PKBase");
	}
	public static void main(String[] args){
	      try {	
	         File inputFile = new File("resources/PKBase.xml");
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         doc.getDocumentElement().normalize();
	         System.out.println("Root element :" 
	            + doc.getDocumentElement().getNodeName());
	         NodeList nList = doc.getElementsByTagName("entity");
	         System.out.println("----------------------------");
	         for (int temp = 0; temp < nList.getLength(); temp++) {
	            Node nNode = nList.item(temp);
                org.bson.Document PKbase=new org.bson.Document();
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               String id=eElement.getAttribute("enity_id");
	               String title=eElement.getAttribute("title").toLowerCase();
	               title=tools.WordSeg.wordseg.trimText(title);
	               PKbase.append("id", StringEscapeUtils.escapeSql(id));
	               PKbase.append("title_unique", StringEscapeUtils.escapeSql(title));
	               
	               NodeList property=eElement.getChildNodes();
	               HashSet<String> name_table=new HashSet<>();
	               for(int i=0;i<property.getLength();i++){
	            	   Node childNode=property.item(i);
	            	   if(childNode.getNodeType()==Node.ELEMENT_NODE){
		            	   Element child=(Element) childNode;
		            	   name_table.add(child.getTagName());
	            	   }
	               }
	               for(String name:name_table){
	            	   NodeList name_value=eElement.getElementsByTagName(name);
	            	   List<String> value_list=new ArrayList<>();
	            	   for(int i=0;i<name_value.getLength();i++){
	            		   value_list.add(StringEscapeUtils.escapeSql(name_value.item(i).getTextContent()));
	            	   }
	            	   PKbase.append(name.replaceAll("\\.", ""), value_list);
	               }
	            }
	            System.out.println(PKbase);
	            weibo_collection.insertOne(PKbase);
	         }
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	   }
}
