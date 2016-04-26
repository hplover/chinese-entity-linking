package main.disambiguation.v3_recordEntityGetByParallel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.mongodb.client.MongoCollection;

import net.sf.json.JSONArray;
import tools.WordSeg.wordseg;
/**
 * @since 2016年3月30日
 * @author HuangDC
 * @desc 爬取单个实体的信息
 */
public class GetBasicInfo {
		private Date date=new Date();
	    private int status=-1;
	    private String word="";
	    private String url="";
	    private LinkedHashMap<String, String> poly=new LinkedHashMap<String, String>();
	    private Document content=null;
	    private String synonym="";
		private ArrayList<String> label=new ArrayList<>();
		private List<String> keywords=new ArrayList<>();
		private String desc="";
		private org.bson.Document infobox=new org.bson.Document();
		private String summary="";
		private String context="";
		private String title="";
		
	    private int index;
	   	static String user_agent="";
	    static List<String> user_agents=new ArrayList<>(); 
	    static int useragent_len=0;
	    static String baikePrefix="http://baike.baidu.com";
	    
	    /**
	     * 是否已经获得各个部分的信息的标识
	     */
	    private boolean isDesc=false,isKeyWords=false,isTitle=false,isPoly=false,isSynonym=false,isLabel=false,isInfoBox=false,isSummary=false,isContext=false;
		
	    
	   	/**
	   	 * 读取爬取数据时需要用来伪装的客户端
	   	 */
	    static{
	    	boolean flag=true;
	    	int i=0;
	    	while(flag){
	    		try {
	    			flag=false;
	    		} catch (Exception e) {
	    			i++;
	    			if(i==3){
	    				System.err.println("can not access internet.");
	    				System.exit(-1);
	    			}
	    		}
	    	}
	    	try (
	    		    InputStream fis = new FileInputStream("resources/user_agents");
	    		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    		    BufferedReader br = new BufferedReader(isr);
	    		) {
	    			String line="";
	    		    while ((line = br.readLine()) != null) {
	    		        user_agents.add(line);
	    		    }
	    		} catch (IOException e) {
					e.printStackTrace();
				}
	    	useragent_len=user_agents.size();
	    }
	    
	    /**
	     * 判断输入的input是否存在于数据库中的索引表里，若存在则返回对应的实体，若不存在则爬取网页
	     * @param collection_index 数据库的索引表
	     * @param setEntity 存储对应的实体信息
	     * @param input:可以是输入的关键词，也可以是百科的网址
	     */
	    public GetBasicInfo(String input, HashSet<org.bson.Document> setEntity, MongoCollection<org.bson.Document> collection_index) throws UnsupportedEncodingException{
	    	if(collection_index!=null&&GetBaikeInfo.InMongoDB(input, setEntity)){}
	    	else{
	    		index=new Random().nextInt(useragent_len)%(useragent_len+1);
		    	user_agent=user_agents.get(index);
		    	if(isURL(input)){
		    		url=input;
		    	}
		    	else {
		    		word=input.toUpperCase();
			    	url="http://baike.baidu.com/search/word?word="+URLEncoder.encode(input,"utf-8");
				}
		    	content=getBaiKeWeb(url);
	    	}
	    }
	    /**
	     * 判断输入是否为网址
	     * @param str
	     * @return
	     */
	    private boolean isURL(String str) {
	    	str=str.replaceAll("^((https|http|ftp|rtsp|mms)?://)[^\\s]+", "");
	    	if(str.isEmpty()){
	    		return true;
	    	}
			return false;
		}
	    /**
	     * 爬取网页信息
	     * @param url
	     * @return
	     */
	    private static Document crawlWebContent(String url) {
	    	int i=0;
	    	boolean flag=true;
	    	Document content = null;
	    	while(flag){
	    		try {
	    			content=Jsoup.connect(url).userAgent(user_agent).get();
	    			flag=false;
	    		} catch (Exception e) {
	    			i++;
	    			try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	    			if(i==3){
	    				System.out.println("failed to connect "+url);
	    				return null;
	    			}
	    		}
	    	}
			return content;
		}
	    /**
	     * 获取百科整个网页，并初始化status（-1表示不存在，1表示存在异义项，5表示存在唯一实体）
	     * @param tempurl
	     * @return
	     */
	    private Document getBaiKeWeb(String tempurl) {
	    	Document webpage=crawlWebContent(tempurl);
	    	if(webpage==null||webpage.toString().length()<1000){
	    		return null;
	    	}
	        String no_result_re="百度百科尚未收录词条";
	        boolean no_result=webpage.toString().contains(no_result_re);
	        String no_exist_re="<p class=\"sorryCont\"><span class=\"sorryTxt\">";
	        boolean no_exist=webpage.toString().contains(no_exist_re);
	        if(no_exist||no_result){
	        	status=-1;
	        	return null;
	        }
	        Elements item_results=webpage.select("li[class=item]");
	    	if(item_results.size()==0){
	    		status=5;
	    		return webpage;
	    	}
	        String main_content="<div class=\"main-content\">";
        	if(webpage.toString().contains(main_content)){
        		status=1;
        		return webpage;
        	}
	        status=-1;
	        return null;
		}
	    /**
	     * 获取描述信息
	     * @return
	     */
	    public String getDesc(){
	    	if(isDesc||status!=1){
	    		return desc;
	    	}
	    	Elements span = null;
	    	span=content.getElementsByClass("selected");
	    	if(span!=null&&span.size()>0)
	    	{
	    		desc=span.first().text();
	    		return desc;
	    	}
	    	isDesc=true;
	    	return "";
	    }
	    /**
	     * 获取异义项信息，存储的格式为<描述信息,对应的URL>，此处并没有爬取该URL，GeiBaikeInfo.java并行完成此工作
	     * @return
	     */
	    protected LinkedHashMap<String, String> getPoly(){
	    	if(isPoly||status!=1){
	    		return poly;
	    	}
    		Elements a_results=content.select("li[class=item]").select("a");
    		for(int i=0;i<a_results.size();i++){
    			poly.put(a_results.get(i).attr("title"), baikePrefix+a_results.get(i).attr("href").replace("#viewPageContent", ""));
    		}
    		isPoly=true;
	    	return poly;
	    }
	    public String getWord(){
	    	return word;
	    }
	    /**
	     * 获取keywords
	     * @return
	     */
	    public List<String> getKeyWords(){
	    	if(isKeyWords||content==null){
	    		return keywords;
	    	}
	    	Elements Ekeywords=content.select("meta[name=keywords]");
	    	if(!Ekeywords.isEmpty()){
	    		keywords=Arrays.asList(Ekeywords.first().attr("content").split(" "));
	    	}
	    	isKeyWords=true;
	    	return keywords;
	    }
	    /**
	     * 获取title
	     * @return
	     */
	    public String getTitle(){
	    	if(isTitle||content==null){
	    		return title;
	    	}
	    	title=content.title().replace("_百度百科", "");
	    	isTitle=true;
	    	return title;
	    }
	    /**
	     * 获取别名
	     * @return
	     */
	    public String getAlias(){
	    	if(isSynonym||content==null||status!=5){
	    		return synonym;
	    	}
	    	isPoly=true;
	    	if(!title.equals(word))
	    		return title;
	    	return synonym;
	    }
	    /**
	     * 获取标签
	     * @return
	     */
	    public ArrayList<String> getLabel(){
	    	if(isLabel||content==null){
	    		return label;
	    	}
        	Elements xx=content.select("span[class=taglist]");
        	for(int i1=0;i1<xx.size();i1++){
        		label.add(xx.get(i1).text());
        	}
	    	isLabel=true;
	    	return label;
	    }
	    /**
	     * 获取网页正文
	     * @return
	     */
	    @SuppressWarnings("finally")
		public String getContext(){
	    	if(isContext||content==null){
	    		return context;
	    	}
			try{
				Elements bb=content.select("div[class=main-content]");
				try {
					//得到正文，然后做预处理（去除标点符号，繁转简体）
					context=wordseg.trimText(bb.first().text());
				} catch (Exception e) {
					System.err.println("class=main-content not found");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				isContext=true;
				return context;
			}
	    }
	    /**
	     * 获取URL
	     * @return
	     */
	    public String getURL(){
	    	return url;
	    }
	    /**
	     * 获取当前word的状态（-1为没找到，1为存在异义项，5为唯一实体）
	     * @return
	     */
	    public int getStatus(){
	    	return status;
	    }
	    /**
	     * 获取实体的infobox信息，返回的类型为Document
	     * @return
	     */
		public org.bson.Document getInforBox() {
			if(isInfoBox||content==null){
				return infobox;
			}
			Elements basicInfo=content.getElementsByClass("basic-info");
			if(basicInfo.isEmpty()){
				return infobox;
			}
			Elements dtList = basicInfo.first().select("dt");
			Elements ddList = basicInfo.first().select("dd");
			if ((!dtList.isEmpty()) && (!ddList.isEmpty())) {
				org.bson.Document outDocument = new org.bson.Document();
				for (int i = 0; i < dtList.size(); i++) {
					String property = dtList.get(i).text().replaceAll("[^\u4e00-\u9fa5]", "");
					String value = ddList.get(i).text().replaceAll("\"", "");
					String[] valueTokens = value.split(",|;|；|、|，|。");
					if(valueTokens.length==0){
						continue;
					}
					if (valueTokens[valueTokens.length-1].endsWith("等")) {
						valueTokens[valueTokens.length-1] = valueTokens[valueTokens.length-1].replace("等", "");
					}
					ArrayList<String> valueSet = new ArrayList<>();
					for (int j = 0; j < valueTokens.length; j++) {
						valueSet.add(valueTokens[j]);
					}
					outDocument.put(property, valueSet);
				}
				isInfoBox=true;
				infobox=outDocument;
				return infobox;
			}
			return infobox;
		}
		
		/**
		 * 获取summary
		 * @return
		 */
		public String getSummary() {
			if(isSummary||content==null){
				return summary;
			}
			Elements summarys = content.getElementsByClass("lemma-summary");
			isSummary=true;
			if (summarys.isEmpty()) {
				return "";
			}else {
				summary=summarys.first().text();
				return summary;
			}
		}	    
		/**
		 * 获取时间
		 * @return
		 */
		public Date getDate() {
			return date;
		}
	    
	    public static void main(String args[]) throws Exception{
	    	long start=System.currentTimeMillis();
	    	String[] bb={"123","sdf","jaksdf"};
	    	System.out.println(Arrays.asList(bb));
	    	GetBasicInfo extract=new GetBasicInfo("TFBOYS",null,null);
	    	System.out.println("keywors:"+extract.getKeyWords());
	    	System.out.println("time:"+extract.getDate());
	    	System.out.println("title:"+extract.getTitle());
	    	
	    	System.out.println("first:");
	    	System.out.println("word:"+extract.getWord());
	    	System.out.println("status:"+extract.getStatus());
	    	System.out.println("url:"+extract.getURL());
	    	System.out.println("desc:"+extract.getDesc());
	    	System.out.println("synonym:"+extract.getAlias());
	    	System.out.println("summary:"+extract.getSummary());
	    	System.out.println("content:\n\t"+extract.getContext().replace("\n", ""));
	    	System.out.println("=============================================");
	    	System.out.println("label:"+extract.getLabel());
	    	System.out.println("infobox:"+extract.getInforBox());
	    	System.out.println("polysemy:"+extract.getPoly().size()+" "+extract.getPoly().toString());
//	    	System.out.println(extract.getContext());
	    	long middle=System.currentTimeMillis();
//	    	System.out.println("\n\n"+wordseg.trimText(extract.getContext()));
//	    	long bb=System.currentTimeMillis();
	    	System.out.println("first:"+(middle-start));
//	    	System.out.println("se:"+(bb-middle));
	    }
}
