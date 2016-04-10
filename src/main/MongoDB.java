package main;
import java.util.Arrays;
import java.text.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
/**
 * @author HuangDC
 * @date 2016年4月3日
 * @description 
 */
public class MongoDB {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws ParseException{
		MongoCredential credential = MongoCredential.createCredential("mdbadmin","admin"," bjgdFristDB2016".toCharArray());
		System.out.println("***********");
		 
		 // 连接到 mongodb 服务器
	    MongoClient mongoClient = new MongoClient(new ServerAddress("218.76.52.43", 3006), Arrays.asList(credential));	//3006设置为Mongodb端口号
	    
	    MongoDatabase mongoDatabase = mongoClient.getDatabase("BaikeInfo");
	    MongoCollection<Document> collection=mongoDatabase.getCollection("Plover");
	    
	}
	

}

/*
{
	"word":"四叶草",//string
	"status":0,//Integer(0 means polysemy existing, 5 means unique,-1 means not existing)
	"default_url":"http://baike.baidu.com/subview/5642/7990450.htm",//String
	"default_desc":"车轴草属植物",//String
	"default_Synonym":"中国奥林匹克足球队",//String
	"default_summary":"四叶草是车轴草属植物（包括三叶草属和苜蓿草）的稀有变种，也有五叶以上，最多是十八叶。在西方认为能找到四叶草是幸运的表现，在日本则认为会得到幸福，所以又称幸运草。四叶草会被赋予这些意义是因为其非常珍罕性。别称牛角花，植物界。三叶草中或五千株只会有一株是四叶的。",//String
	"default_content":"<!DOCHTML>...",//String(HTML code, script removed)
	"default_label":["草本植物","车轴草属","..."],//List
	"default_keyword":["植物","车轴草"],//List(TFIDF feature)
	"default_action":["观赏","种"],//List(Hard to generate)
	"default_infobox":
	{
		"property1":"value1",
		"property2":"value2",
		"property3":["value3_1","value3_2","..."],
		"...":"..."
	},
	"Polysemy":[
		{
			"poly1_url":"http://baike.baidu.com/subview/5642/7990450.htm",//String
			"poly1_desc":"CLAMP日本漫画",//String
			"poly1_summary":"日本作者CLAMP的漫画作品，是一部极度唯美的文艺片。",//String
			"poly1_content":"<!DOCHTML>...",//String(HTML code, script removed)
			"poly1_label":["漫画"," 娱乐作品","..."],//List
			"poly1_keyword":["日漫","魔法"],//List(TFIDF feature)
			"poly1_action":["看","买"],//List(Hard to generate)
			"poly1_infobox":
			{
				"property1":"value1",
				"property2":"value2",
				"property3":["value3_1","value3_2","..."],
				"...":"..."
			}
		},
		{
			"poly2_url":"http://baike.baidu.com/subview/5642/7990450.htm",//String
			"poly2_desc":"沈阳农业大学首部原创校园电影",//String
			"poly2_summary":"《四叶草》是2015年上映的中国爱情片，影片讲述了大学校园中浪漫又青涩的爱情故事。",//String
			"poly2_content":"<!DOCHTML>...",//String(HTML code, script removed)
			"poly2_label":[" 影视作品"," 电影","..."],//List
			"poly2_keyword":["爱情","电影"],//List(TFIDF feature)
			"poly2_action":["看","下载"],//List(Hard to generate)
			"poly2_infobox":
			{
				"property1":"value1",
				"property2":"value2",
				"property3":["value3_1","value3_2","..."],
				"...":"..."
			}
		},
		{
			"...":"..."
		}
	],
	"date":"yy-mm-dd HH:MM:SS"
}
 
 */

























