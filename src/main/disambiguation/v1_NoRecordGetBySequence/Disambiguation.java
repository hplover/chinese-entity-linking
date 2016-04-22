package main.disambiguation.v1_NoRecordGetBySequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import tools.WordSeg.Result;
import tools.WordSeg.wordseg;

/**
 * @author HuangDC
 * @date 2016年3月17日
 * @description 
 */
public class Disambiguation {
	private boolean debug;
	wordseg wseg=new wordseg();
//    private HashMap<String, List<String>> result=new HashMap<>();
	private List<Result> results=new ArrayList<>();
	Extract bke=new Extract();
	public Disambiguation(boolean debug){
		this.debug=debug;
	}
	
	/**
	 * @param bke2
	 * @param keyword
	 * @param rekeywords
	 * @return
	 */
	private int disambiguation(Extract bke2, String keyword, List<Result> rekeywords) {
		String webpage=bke2.getContent().toString();
    	int target=0;
    	int start=webpage.indexOf("<div class=\"main-content\">");
    	String subpage=webpage.substring(start);
    	for(Result kw:rekeywords){
    		if(!kw.keyword.equals(keyword)){
    			target=target+StringUtils.countMatches(subpage, kw.keyword);
    		}
    	}
    	return target;
	}
    
    public void entitylinking(String keywords_input,int mode){
    	this.results=wseg.segWord_Result(keywords_input, mode);
    	if(debug)
            System.out.println("-1(none) for entiry not found;\n0(Polysemants) for list page;\n1(Polysemant and content) for page with Polysemant;\n5(content and label and url not Polysemant maybe synonym) for unique page");
        for(Result rekeyword:this.results){ 
        	String keyword=rekeyword.keyword;
			if(debug)
                System.out.println(keyword+"\n==============================================================");
			bke.getResult(keyword, "");
			int status=bke.getStatus();
            if(debug)
                System.out.println("status:"+status);
            if(status==-1){
            	if(debug)
                    System.out.println(keyword+" entity not found");
                System.out.println("==============================================================\n\n");
                continue;
            }
            else if(status==5){
            	String bkeSynonym=bke.getSynonym();
            	if(bkeSynonym.length()!=0){
                	if(debug)
                		System.out.println("synonyms:"+bkeSynonym);
                	rekeyword.keywordSynonym=bkeSynonym;
                }
                String bkeURL=bke.getURL();
                if(debug)
                    System.out.println("URL:"+bkeURL);
                String bkelable=bke.getLabel();
                if(debug)
                    System.out.println("label:"+bkelable);
                rekeyword.keywordURL=bkeURL;
                rekeyword.label=bkelable;
            }
            else if(status==0 || status==1){
            	HashMap<String,String> bkePolysemant = bke.getPolysemant();
                int tempTarget=0;
                String tempUrl="";
                String tempPl="";
                String templabel="";
                Iterator<Entry<String, String>> it = bkePolysemant.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> pair = it.next();
                    String pl=(String) pair.getKey();
                    String url=(String) pair.getValue();
                    bke.getResult(pl, url);
                    String lab=bke.getLabel();
                    if(lab=="")
                    	lab="null";
                    if(bke.getContent()==null){
                    	if(debug){
                    		System.out.println(pl+"\t"+url+"<-- not exist");
                    	}
                    	continue;
                    }
                    int target=this.disambiguation(bke, keyword, this.results);
                    if(target>=tempTarget){
                    	tempTarget=target;
                    	tempUrl=url;
                    	tempPl=pl;
                    	templabel=lab;
                    }
                    if(debug){
                    	System.out.println(pl+"\t"+url+"\t"+lab);
                    }
                }
                rekeyword.keywordURL=tempUrl;
                rekeyword.keywordSynonym=tempPl;
                rekeyword.label=templabel;
            }
            else{
            	System.err.println("no such status");
            }
            if(debug)
                System.out.println("==============================================================\n\n");
        }
        return ;
    }

	public void saveCrawl(String keywords_input,int mode){
		this.results=wseg.segWord_Result(keywords_input, mode);
    	if(debug)
            System.out.println("-1(none) for entiry not found;\n0(Polysemants) for list page;\n1(Polysemant and content) for page with Polysemant;\n5(content and label and url not Polysemant maybe synonym) for unique page");
        for(Result rekeyword:this.results){ 
        	String keyword=rekeyword.keyword;
			if(debug)
                System.out.println(keyword+"\n==============================================================");
			bke.getResult(keyword, "");
			int status=bke.getStatus();
            if(debug)
                System.out.println("status:"+status);
            if(status==-1){
            	if(debug)
                    System.out.println(keyword+" entity not found");
                System.out.println("==============================================================\n\n");
                continue;
            }
            else if(status==5){
            	String bkeSynonym=bke.getSynonym();
            	if(bkeSynonym.length()!=0){
                	if(debug)
                		System.out.println("synonyms:"+bkeSynonym);
                	rekeyword.keywordSynonym=bkeSynonym;
                }
                String bkeURL=bke.getURL();
                if(debug)
                    System.out.println("URL:"+bkeURL);
                String bkelable=bke.getLabel();
                if(debug)
                    System.out.println("label:"+bkelable);
                rekeyword.keywordURL=bkeURL;
                rekeyword.label=bkelable;
            }
            else if(status==0 || status==1){
            	HashMap<String,String> bkePolysemant = bke.getPolysemant();
                int tempTarget=0;
                String tempUrl="";
                String tempPl="";
                String templabel="";
                Iterator<Entry<String, String>> it = bkePolysemant.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> pair = it.next();
                    String pl=(String) pair.getKey();
                    String url=(String) pair.getValue();
                    bke.getResult(pl, url);
                    String lab=bke.getLabel();
                    if(lab=="")
                    	lab="null";
                    if(bke.getContent()==null){
                    	if(debug){
                    		System.out.println(pl+"\t"+url+"<-- not exist");
                    	}
                    	continue;
                    }
                    int target=this.disambiguation(bke, keyword, this.results);
                    if(target>=tempTarget){
                    	tempTarget=target;
                    	tempUrl=url;
                    	tempPl=pl;
                    	templabel=lab;
                    }
                    if(debug){
                    	System.out.println(pl+"\t"+url+"\t"+lab);
                    }
                }
                rekeyword.keywordURL=tempUrl;
                rekeyword.keywordSynonym=tempPl;
                rekeyword.label=templabel;
            }
            else{
            	System.err.println("no such status");
            }
            if(debug)
                System.out.println("==============================================================\n\n");
        }
        return ;
	}
    
    public void printResult(){
    	for(Result result:this.results){
    		System.out.println(result.keyword+"\t"+result.keywordSynonym+"\t"+result.keywordURL+"\t"+result.type+"\t"+result.label);
    	}
    }
	public static void main(String[] args){
		long startTime=System.currentTimeMillis();   //获取开始时间  
		
		String text = "在今年夏季的军方将领调整中，又有多位年轻将领被重用：成都军区政治部主任吴昌德中将，升任总政治部副主任；曾是“最年轻中将”的国防科技大学校长张育林，调任总装备部副部长；中国人民解放军国防科学技术大学副校长杨学军升任该校校长，成为副大军区级将领中为数不多的“60后”";
//		String text="训练 奥体中心 外场 球员";
//		String text="多聚酶链式反应（PCR）是一项用于体外复制DNA的极为通用的技术。简而言之，PCR技术可以使单链DNA被复制数百万次，也允许用事先确定好的方式对被复制的DNA序列进行改动。例如，PCR技术可以用于引入限制性酶切位点，或者对特定的DNA碱基进行突变（改变）。PCR技术还可以用于从cDNA文库获得特定的DNA片段，或者从另一个角度，用于判断一个cDNA文库中是否含有特定的DNA片段。";
//		String text="主任";
//		String text="中将";
//		String text="你认为tfboys拥有最强大的粉丝群吗？没错！为什么？因为只要是四叶草看了这条微博都会转（来自四叶草）";
		Disambiguation entityLinkingService=new Disambiguation(true);
    	entityLinkingService.entitylinking(text,3);
    	entityLinkingService.printResult();

		long endTime=System.currentTimeMillis(); //获取结束时间  
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");   
    }
    	
}
