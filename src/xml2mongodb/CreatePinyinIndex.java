package xml2mongodb;
/*
 * 把知识库中的实体名字的拼音存下来
 */
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.w3c.dom.Document;

public class CreatePinyinIndex {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<org.bson.Document> PKBASE =null;
	static MongoCollection<org.bson.Document> PinYinBase = null;

	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("weibo");
		PKBASE = db.getCollection("PKBase");
		PinYinBase=db.getCollection("PinYin");
	}
	
	public static void main(String args[]) throws SAXException, IOException, ParserConfigurationException{
		String title="";
		String pinyin="";
		org.bson.Document title_pinyin=new org.bson.Document();
		
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
           if (nNode.getNodeType() == Node.ELEMENT_NODE) {
              Element eElement = (Element) nNode;
              title=eElement.getAttribute("title").toLowerCase();
              title=tools.WordSeg.wordseg.trimText(title);
              if(title.contains("_")){
  				title=title.substring(0, title.indexOf("_"));
  			  }
              org.bson.Document isExist=PinYinBase.find(new org.bson.Document("title",title)).first();
  		      if(isExist!=null){
  				continue;
  			  }
  			  pinyin=PinyinHelper.convertToPinyinString(title, " ", PinyinFormat.WITH_TONE_NUMBER);
  			  title_pinyin.append("title", title);
  			  title_pinyin.append("pinyin", pinyin);
  			  PinYinBase.insertOne(title_pinyin);
  			  System.out.println(title_pinyin);
  			  title_pinyin.clear();
           }
        }
	}
	
	
}
