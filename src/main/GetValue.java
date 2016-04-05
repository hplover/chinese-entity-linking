package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.sf.json.JSONArray;
/**
 * @author HuangDC
 * @date 2016年3月30日
 * @description 
 */
public class GetValue {
	    private int status=-1;
	    private String word="";
	    private String url="";
	    private HashMap<String,String> poly=new HashMap<>();
	    private Document content=null;
	    private String synonym="";
		private String label="";
		
	    private int index;
	   	static String user_agent="";
	    static List<String> user_agents=new ArrayList<>(); 
	    static int useragent_len=0;
	    
	   	static String baikePrefix="http://baike.baidu.com";
	   	
	    static{
	    	Document content = null;
	    	boolean flag=true;
	    	String url="http://www.baidu.com";
	    	int i=0;
	    	while(flag){
	    		try {
	    			content=Jsoup.connect(url).userAgent(user_agent).get();
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
	    
	    
	    public GetValue(String keyword) throws UnsupportedEncodingException{
	    	this.word=keyword;
	    	index=new Random().nextInt(useragent_len)%(useragent_len+1);
	    	user_agent=user_agents.get(index);
	    	url="http://baike.baidu.com/search/word?word="+URLEncoder.encode(keyword,"utf-8");
	    	content=getBaiKeWeb(url);
	    }
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
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			System.out.println("connect failed..try again:"+url);
	    			if(i==3){
	    				System.err.println("can not connect to "+url+"\nreturn null...");
	    				return null;
	    			}
	    		}
	    	}
			return content;
		}
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
	    public String getDesc(){
	    	Elements span = null;
	    	if(content!=null&&status==1)
	    		span=content.getElementsByClass("selected");
	    	if(span!=null&&span.size()>0)
	    		return span.first().text();
	    	return "";
	    }
	    public HashMap<String, String> getPolysemant(){
	    	if(content==null||status!=1){
	    		return null;
	    	}
    		Elements a_results=content.select("li[class=item]").select("a");
    		for(int i=0;i<a_results.size();i++){
    			poly.put(a_results.get(i).attr("title"), baikePrefix+a_results.get(i).attr("href").replace("#viewPageContent", ""));
    		}
	    	return poly;
	    }
	    public String getWord(){
	    	return word;
	    }
	    public String getSynonym(){
	    	if(content==null||status!=5){
	    		return synonym;
	    	}
	    	String title=content.title().replace("_百度百科", "");
	    	if(!title.equals(word))
	    		return title;
	    	return synonym;
	    }
	    public String getLabel(){
	    	if(content!=null){
	        	Elements xx=content.select("span[class=taglist]");
	        	for(int i1=0;i1<xx.size();i1++){
	        		label=label+xx.get(i1).text()+" ";
	        	}
	    	}
	    	return label;
	    }
	    public String getContent(){
	    	if(content!=null)
	    		return content.toString().substring(0, 100);
	    	return "";
	    }
	    public String getURL(){
	    	return url;
	    }
	    public int getStatus(){
	    	return status;
	    }
		public String getInforBox() {
			Elements basicInfo=content.getElementsByClass("basic-info");
			if(basicInfo.isEmpty()){
				return "";
			}
			Elements dtList = basicInfo.first().select("dt");
			Elements ddList = basicInfo.first().select("dd");
			if ((!dtList.isEmpty()) && (!ddList.isEmpty())) {
				JSONArray outJSONArray = new JSONArray();
				for (int i = 0; i < dtList.size(); i++) {
					String property = dtList.get(i).text().replaceAll("[^\u4e00-\u9fa5]", "");
					String value = ddList.get(i).text().replaceAll("\"", "");
					String[] valueTokens = value.split(",|;|；|、|，|。");
					if (valueTokens[valueTokens.length-1].endsWith("等")) {
						valueTokens[valueTokens.length-1] = valueTokens[valueTokens.length-1].replace("等", "");
					}
					JSONArray valueJson = new JSONArray();
					for (int j = 0; j < valueTokens.length; j++) {
						valueJson.add(j, valueTokens[j]);
					}
					outJSONArray.add(0, property);
					outJSONArray.add(1, valueJson);
				}
				return outJSONArray.toString();
			}
			return null;
		}
		public String getSummary() {
			Elements summarys = content.getElementsByClass("lemma-summary");
			if (summarys.isEmpty()) {
				return null;
			}else {
				return summarys.first().text();
			}
		}	    
	    
	    public static void main(String args[]) throws UnsupportedEncodingException{
//	    	String content=Extract.crawlWebContent("http://baike.baidu.com/view/14351.htm?fromtitle=中国人民解放军国防科学技术大学&fromid=1223537&type=syn");
//	    	System.out.println(content);
//	    	String re="<div class=\"para\" label-module=\"para\">\n.*?<a target=\"_blank\" href=\"(.*?)\">.*?：(.*?)</a>";
//	        find(re,content);
//	    	中国人民解放军国防科学技术大学 苹果 国奥 虾极霸掣
//	    	String url="http://baike.baidu.com/view/1555561.htm";//都会
//	    	String url="http://baike.baidu.com/subview/5642/7990450.htm";//四叶草
	    	GetValue extract=new GetValue("北大");
	    	System.out.println("word:"+extract.getWord());
	    	System.out.println("status:"+extract.getStatus());
	    	System.out.println("url:"+extract.getURL());
	    	System.out.println("desc:"+extract.getDesc());
	    	System.out.println("synonym:"+extract.getSynonym());
	    	System.out.println("summary:"+extract.getSummary());
//	    	System.out.println("content"+extract.getContent());
	    	System.out.println("label:"+extract.getLabel());
	    	System.out.println("infobox:"+extract.getInforBox());
	    	
	    }
}
