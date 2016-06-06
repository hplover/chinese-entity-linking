package main;

import java.util.Arrays;

import org.bson.Document;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.hankcs.hanlp.HanLP;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class hanlptext {
	static MongoCredential credential =null;
	static MongoClient mongoClient =null;
	static MongoDatabase db =null;
	static MongoCollection<org.bson.Document> PKBASE =null;
	static MongoCollection<Document> PinYinBase = null;

	static{
		credential = MongoCredential.createCredential("mdbadmin","admin","bjgdFristDB2016".toCharArray());
		mongoClient = new MongoClient(new ServerAddress("idcbak.answercow.org",3006),Arrays.asList(credential));	
		db = mongoClient.getDatabase("weibo");
		PKBASE = db.getCollection("PKBase");
		PinYinBase=db.getCollection("PinYin");
	}
	public static void main(String[] args){
//		HanLP.parseDependency("LINKIN PARK歌曲的演唱者麦克·信田");
		String bb="123";
		String cc=bb.replaceAll("\\d", "");
		System.out.println(bb+"\nafter"+cc);
		String str = "一二";
	    String aString=PinyinHelper.convertToPinyinString(str, ",", PinyinFormat.WITH_TONE_MARK); // nǐ,hǎo,shì,jiè
	    String bString=PinyinHelper.convertToPinyinString(str, ",", PinyinFormat.WITH_TONE_NUMBER); // ni3,hao3,shi4,jie4
	    String cString=PinyinHelper.convertToPinyinString(str, ",", PinyinFormat.WITHOUT_TONE); // ni,hao,shi,jie
	    String dString=PinyinHelper.getShortPinyin(str); // nhsj
	    System.out.println(PinyinHelper.hasMultiPinyin('一'));
	    System.out.println(aString);
	    System.out.println(bString);
	    System.out.println(cString);
	    System.out.println(dString);
	    

		String pinyin="";
		Document title_pinyin=new Document();
	    String title="永嘉_(上海)";
	    if(title.contains("_")){
			title=title.substring(0, title.indexOf("_"));
		}
	    System.out.println(title);
	    Document isExist=PinYinBase.find(new Document("title",title)).first();
	    if(isExist!=null){
			System.out.println();
		}
		pinyin=PinyinHelper.convertToPinyinString(title, " ", PinyinFormat.WITH_TONE_NUMBER);
		title_pinyin.append("title", title);
		title_pinyin.append("pinyin", pinyin);
		PinYinBase.insertOne(title_pinyin);
		System.out.println(title_pinyin);
		title_pinyin.clear();
		
		
		pinyin=PinyinHelper.convertToPinyinString("北京", " ",PinyinFormat.WITHOUT_TONE);
		System.out.println(pinyin);
		
		
	}
}
