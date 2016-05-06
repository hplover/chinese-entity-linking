package main.disambiguation.v3_recordEntityGetByParallel.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * @since 2016年3月30日
 * @author HuangDC
 * @desc 爬取单个实体的信息
 */
public class GetProxy {
		private Date date=new Date();
	    private static int index;
	   	static String user_agent="";
	    static List<String> user_agents=new ArrayList<>(); 
	    static int useragent_len=0;
	    static String baikePrefix="http://baike.baidu.com";
		
	    
	   	/**
	   	 * 读取爬取数据时需要用来伪装的客户端
	   	 */
	    static{
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
	    public List<IPPort> GetAllProxy(int pages) throws UnsupportedEncodingException{
		    List<IPPort> ret= new ArrayList<IPPort>();
		    Document content = null;
	    		index=new Random().nextInt(useragent_len)%(useragent_len+1);
		    	user_agent=user_agents.get(index);
		    	String url = "http://www.xicidaili.com/nn/";
		    	for(int i=1;i<pages;i++){
			    	content=getProxyWeb(url+i);
			    	ret.addAll(getIPPort(content));
		    	}
		    	System.out.println(ret.size());
		    	return ret; 
		    	
	    }
	    
	    /**
	     * 爬取网页信息
	     * @param url
	     * @return
	     */
	    private static Document crawlProxyContent(String url) {
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
	    				int outtime=new Random().nextInt(3000);
	    				outtime = outtime + 2000;
						Thread.sleep(outtime);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	    			if(i==4){
	    				System.out.println("failed to connect "+url);
	    				return null;
	    			}
	    		}
	    	}
			return content;
		}
	    /**
	     * 获取百科整个网页，并初始化status（-1表示不存在，1表示存在异义项，5表示存在唯一实体）
	     * @param proxyURL
	     * @return
	     */
	    private static Document getProxyWeb(String proxyURL) {
	    	Document webpage=crawlProxyContent(proxyURL);
	    	if(webpage==null||webpage.toString().length()<1000){
	    		return null;
	    	}
	        return webpage;
		}
	    @SuppressWarnings("finally")
		private static List<IPPort> getIPPort(Document content){
		    List<IPPort> ret= new ArrayList<IPPort>();
		    if(content==null){
			    return null;
			    }
		    try{
				Elements all_row=content.getElementById("ip_list").getElementsByTag("tr");
				//得到正文，然后做预处理（去除标点符号，繁转简体）
				for(Element e:all_row){
					    String str = e.text();  
					    String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3} \\d{1,5}";  
					    Pattern p = Pattern.compile(regex);  
					    Matcher m = p.matcher(str);  
					    while (m.find()) {  
					        String[] ipport = m.group().split(" ");
					        ret.add(new IPPort(ipport[0],ipport[1]));
					    }  
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				return ret;
			}
		}
		public Date getDate() {
			return date;
		}
	    
	    public static void main(String args[]) throws Exception{
	    	long start=System.currentTimeMillis();
	    	GetProxy resultTest=new GetProxy();
	    	List<IPPort> bb=resultTest.GetAllProxy(50);
	    	FileWriter fw=new FileWriter(new File("resources/proxy"));
	    	for(IPPort tt:bb){
	    		System.out.println(tt.getIp()+"\t"+tt.getPort());
	    		fw.write(tt.getIp()+"\t"+tt.getPort()+"\n");
	    	}
	    	fw.close();
//	    	System.out.println("result:\n"+resultTest.GetAllProxy(10));
	    	long middle=System.currentTimeMillis();
	    	System.out.println("time:"+(middle-start));
	    }
}
