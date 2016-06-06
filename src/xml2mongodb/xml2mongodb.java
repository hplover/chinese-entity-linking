package xml2mongodb;
import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
public class xml2mongodb {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<org.bson.Document> weibo_collection =null;

	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("weibo");
		weibo_collection = db.getCollection("unLabelquery");
	}
	
   public static void main(String[] args){
      try {	
         File inputFile = new File("resources/unLabelquery");
         
         DocumentBuilderFactory dbFactory 
            = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(inputFile);
         doc.getDocumentElement().normalize();
         System.out.println("Root element :" 
            + doc.getDocumentElement().getNodeName());
         NodeList nList = doc.getElementsByTagName("weibo");
         System.out.println("----------------------------");
         boolean isError=true;
         for (int temp = 0; temp < nList.getLength(); temp++) {
        	org.bson.Document EL_annotation=new org.bson.Document();
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               String id=eElement.getAttribute("id");
               String content=eElement.getElementsByTagName("content").item(0).getTextContent();
               NodeList entity_name=eElement.getElementsByTagName("name");
               NodeList entity_startoffset=eElement.getElementsByTagName("startoffset");
               NodeList entity_endoffset=eElement.getElementsByTagName("endoffset");
               NodeList entity_kb=eElement.getElementsByTagName("kb");
               
               org.bson.Document entity=new org.bson.Document();
               int size=entity_name.getLength();
               for(int i=0;i<size;i++){
            	   org.bson.Document subentity=new org.bson.Document();
            	   try {
						subentity.append("name", entity_name.item(i).getTextContent());
						subentity.append("startoffset", entity_startoffset.item(i).getTextContent());
						subentity.append("endoffset", entity_endoffset.item(i).getTextContent());
						subentity.append("kb", entity_kb.item(i).getTextContent());
						entity.append("entity"+i, subentity);
					} catch (Exception e) {
						System.err.println("xml format error; location:"+i);
						isError=false;
					}
               }
               if(isError==false){
            	   isError=true;
            	   continue;
               }
               EL_annotation.append("id", id);
               EL_annotation.append("content", content);
               EL_annotation.append("entity", entity);
               System.out.println(EL_annotation);
               weibo_collection.insertOne(EL_annotation);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

