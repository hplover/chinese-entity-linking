package main.disambiguation.v1_NoRecordGetBySequence;

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
import org.jsoup.select.Elements;
/**
 * @author HuangDC
 * @date 2016年3月30日
 * @description 
 */
public class Extract {
	    private int status;
	    private String keyword;
	    private String tempurl;
	    private int flag;
	    private String url;
	    private HashMap<String,String> poly;
	    private Document content;
	    private String synonym;
	    private int index;
		private String label;
	   	static String user_agent="";
	    static List<String> user_agents=new ArrayList<>(); 
	    static int useragent_len=0;
	   	static String baikePrefix="http://baike.baidu.com";
	   	
	    static{
	    	boolean flag=true;
	    	String url="http://www.baidu.com";
	    	int i=0;
	    	while(flag){
	    		try {
	    			Jsoup.connect(url).userAgent(user_agent).get();
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
	    
	    private void init(){
	    	this.status=-1;
	    	this.keyword="";
	    	this.tempurl="";
	    	this.flag=0;
	    	this.url="";
	    	this.poly=new HashMap<>();
	    	this.content=null;
	    	this.synonym="";
	    	this.index=new Random().nextInt(useragent_len)%(useragent_len+1);
	    	this.label="";
	    	this.user_agent=user_agents.get(index);
	    }
	    
	    public void getResult(String keyword,String url){
	    	init();
	    	this.keyword=keyword;
	    	if(url==null||url==""){
	    		try {
					this.tempurl="http://baike.baidu.com/search/word?word="+URLEncoder.encode(keyword,"utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	    	}
	    	else{
	    		this.flag=1;
	    		this.tempurl=url;
	    	}
	    	this.content=this.fetchBaiKeWeb();
	    	this.poly=this.fetchPolysemant();
	    	this.synonym=this.fetchSynonym();
	    	this.label=this.fetchLabel();
	    }
	    
	    public static Document crawlWebContent(String url) {
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
	    private Document fetchBaiKeWeb() {
	    	Document webpage=Extract.crawlWebContent(this.tempurl);
	    	if(webpage==null||webpage.toString().length()<1000){
	    		return null;
	    	}
	        String no_result_re="百度百科尚未收录词条";
	        boolean no_result=webpage.toString().contains(no_result_re);
	        String no_exist_re="<p class=\"sorryCont\"><span class=\"sorryTxt\">";
	        boolean no_exist=webpage.toString().contains(no_exist_re);
	        if(no_exist||no_result){
	        	this.status=-1;
	        	return null;
	        }
	        String list_result_re="<div class=\"para\" label-module=\"para\">\n.*?<a target=\"_blank\" href=\"(.*?)\">.*?·(.*?)</a>";
	        String main_content="<div class=\"main-content\">";
        	if(webpage.toString().contains(main_content)){
        		this.status=1;
        		return webpage;
        	}
	        List<ArrayList<String>> list_results=find(list_result_re, webpage.toString());
	        if(!list_results.isEmpty()){
	        	for(ArrayList<String> result:list_results){
	        		this.poly.put(result.get(1), baikePrefix+result.get(0));
	        	}
	        	this.status=0;
	        	return null;
	        }
	        this.status=-1;
	        return null;
		}
	    private HashMap<String, String> fetchPolysemant(){
	    	if(this.content==null||this.flag==1){
	    		if(this.status==0){
	    			return this.poly;
	    		}
	    		else{
	    			return null;
	    		}
	    	}
	    	Elements item_results=this.content.select("li[class=item]");
	    	if(item_results.size()==0){
	    		this.url=this.tempurl;
	    		this.status=5;
	    	}
	    	else{
	    		Elements span_results=item_results.select("span[class=selected]");
	    		Elements a_results=item_results.select("a");
	    		for(int i=0;i<span_results.size();i++){
	    			this.poly.put(span_results.get(i).text(), this.tempurl);
	    		}
	    		for(int i=0;i<a_results.size();i++){
	    			this.poly.put(a_results.get(i).attr("title"), baikePrefix+a_results.get(i).attr("href").replace("#viewPageContent", ""));
	    		}
	    	}
	    	return this.poly;
	    }
	    private String fetchSynonym(){
	    	if(this.status!=5){
	    		return null;
	    	}
	    	String title=this.content.title().replace("_百度百科", "");
	    	if(!title.equals(this.keyword))
	    		return title;
	    	return "";
	    }
	    private String fetchLabel(){
	    	if(this.content!=null){
	        	Elements xx=this.content.select("span[class=taglist]");
	        	for(int i1=0;i1<xx.size();i1++){
	        		this.label=this.label+xx.get(i1).text()+" ";
	        	}
	    	}
	    	return this.label;
	    }
	    public Document getContent(){
	    	return this.content;
	    }
	    public String getSynonym(){
	    	return this.synonym;
	    }
	    public int getStatus(){
	    	return this.status;
	    }
	    public HashMap<String,String> getPolysemant() {
			return this.poly;
		}
	    public String getURL(){
	    	return this.url;
	    }
	    public String getLabel(){
	    	return this.label;
	    }
	    public void setURL(String url){
	    	this.url=url;
	    }
	    
	    private List<ArrayList<String>> find(String regex,String s) {
	        Matcher m = Pattern.compile(regex).matcher(s);
	        List<ArrayList<String>> result=new ArrayList<>();
	        int gourpcount=m.groupCount();
	        while (m.find()) {
	        	ArrayList<String> subresult=new ArrayList<>();
	        	for(int i1=1;i1<=gourpcount;i1++){
	        		subresult.add(m.group(i1));
	        	}
	        	result.add(subresult);
	        }
	        return result;
	    }
	
	    public static void main(String args[]){
//	    	String content=Extract.crawlWebContent("http://baike.baidu.com/view/14351.htm?fromtitle=中国人民解放军国防科学技术大学&fromid=1223537&type=syn");
//	    	System.out.println(content);
//	    	String re="<div class=\"para\" label-module=\"para\">\n.*?<a target=\"_blank\" href=\"(.*?)\">.*?：(.*?)</a>";
//	        find(re,content);
//	    	中国人民解放军国防科学技术大学 苹果 国奥 虾极霸掣
	    	Extract extract=new Extract();
	    	extract.getResult("中国人民解放军国防科学技术大学", "");
	    }
}
