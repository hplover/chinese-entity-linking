package main.disambiguation.v4_PKBASE.subTools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

public class PinyinSim {
	public static HashMap<String, String> name_pinyin=new HashMap<>();
	public static Set<String> kbname=new HashSet<>();
	private static boolean isLoad=false;

	public static void loadPinyin() throws SAXException, IOException, ParserConfigurationException{
		if(isLoad){
			return ;
		}
		String title="";
		String pinyin="";
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
              kbname.add(title);
              if(title.contains("_")){
  				title=title.substring(0, title.indexOf("_"));
  			  }
              if(name_pinyin.containsKey(title))
            	  continue;
  			  pinyin=PinyinHelper.convertToPinyinString(title, " ", PinyinFormat.WITHOUT_TONE);
  			  name_pinyin.put(title, pinyin);
           }
        }
        isLoad=true;
	}
	
	public static Set<String> queryPinyin(String pinyin){//返回与输入拼音相关的词
		Set<String> names = new HashSet<>();
		Set<String> result_names=new HashSet<>();
		if(name_pinyin.isEmpty()){
			return names;
		}
		for(Entry<String, String> name_py:name_pinyin.entrySet()){
			if(name_py.getValue().equals(pinyin)){//获得与输入拼音有相同的拼音的词
				names.add(name_py.getKey());
			}
		}
		for(String name:names){//遍历得到的词集合
			if(kbname.contains(name)){//如果知识库中有这些词则保存在结果中
				result_names.add(name);
			}
			else{
				for(String kbString:kbname){//没有这些词的话，保存以这些词开头的词
					if(kbString.contains(name)){
						result_names.add(kbString);
					}
				}
			}
		}
		return result_names;
	}
}
