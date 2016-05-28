package tools.WordSeg;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Triplet;

import java.util.Map.Entry;

import com.hankcs.hanlp.dependency.nnparser.parser_dll;
import com.hankcs.hanlp.seg.common.Term;
import com.mongodb.AggregationOptions.Builder;


public class wordseg {
	static String path="C:/javarequest/models/nlp/";
	static BigSegment sgmt = null;
	public static HashMap<String, Long> allterms=new HashMap<>();
	public static long alldocterms=0;
	static PrintStream original=System.out;

	static{
		long a=System.currentTimeMillis();
		sgmt = new BigSegment(path);
		long b=System.currentTimeMillis();
		System.out.println("load segment time: "+(b-a));
	}
	
	public static String trimText(String text){
		if(text.length() > 6003){
			text = text.substring(0, 6001);
		}
		text = LangUtils.mapFullWidthLetterToHalfWidth(text);
		text = LangUtils.mapChineseMarksToAnsi(text);
		text = LangUtils.mapFullWidthNumberToHalfWidth(text);
		text = LangUtils.removeEmptyLines(text);
		text = LangUtils.removeExtraSpaces(text);
		text = LangUtils.removeLineEnds(text);
//		char[] chs = new char[text.length()];
//		int id = 0;
//		for(int i=0;i<text.length();i++){
//			char c = text.charAt(i);
//			if(LangUtils.isChinese(c)|| ((int)c >31&& (int)c<128)){
//				chs[id]=c;
//				id++;
//			}
//		}
//		text = LangUtils.T2S(new String(chs).trim());
		return LangUtils.replaceAnsiMarkWithSpace(text);
	}
	
	@SuppressWarnings("null")
	public List<Result> segWord_Result(String text_single,int mode) {
		System.out.println("Src:"+text_single);
		text_single=trimText(text_single);
		System.out.println("Trim:"+text_single);
		List<Result> ReList = new ArrayList<>();
		HashSet<String> hSet=new HashSet<>();
		String[] textarray=text_single.split(",");
		System.out.print("Segment:");
		for(String text:textarray){
			List<Term> termReList = sgmt.getHanLPSegment(mode, text);
			for(Term result:termReList){
				String ttype=result.nature.name();
				if(hSet.contains(result.word))
					continue;
				else {
					hSet.add(result.word);
				}
				if(!ttype.startsWith("n"))
					continue;
				Result temp = new Result();
				temp.keyword=result.word;
				temp.type=ttype;
				ReList.add(temp);
			}
		}
		System.out.println();
		return ReList;
	}
	
	public static Set<String> segWord_Set(String text_single,int mode) {
		text_single=trimText(text_single);
		HashSet<String> hSet=new HashSet<>();

		List<Term> termReList = sgmt.getHanLPSegment(mode, text_single);
		for(Term result:termReList){
			String ttype=result.nature.name();
			if(hSet.contains(result.word))
				continue;
			else {
				if(!ttype.startsWith("n"))
					continue;
				if(!result.word.replaceAll("\\d", "").isEmpty()&&result.word.length()>1)
					hSet.add(result.word.toUpperCase());
			}
		}
		
		return hSet;
	}
	
	public static HashMap<String, Triplet<Integer, Double, Double>> segWord_TF(String text_single,int mode) {

		String[] textarray=text_single.split(",");
		HashMap<String, Triplet<Integer, Double, Double>> tf=new HashMap<>();
		String ttype=null;
		String tword=null;
		long alltermscount=0;
		for(String text_line:textarray){
			List<Term> seg=sgmt.getHanLPSegment(mode, text_line);
			for(Term result:seg){
				ttype=result.nature.name();
				tword=result.word;
				if(!ttype.startsWith("n"))
					continue;
				Triplet<Integer, Double, Double> temp=null;
				if(tf.containsKey(tword))
					temp=new Triplet<Integer, Double,Double>((int)tf.get(tword).getValue0()+1, 0.0,0.0);
				else {
					temp=new Triplet<Integer, Double,Double>(1, 0.0,0.0);
				}
				tf.put(tword, temp);
				alltermscount++;
			}
		}
		for(Entry<String, Triplet<Integer, Double, Double>> term:tf.entrySet()){
			String word=term.getKey();
			Triplet<Integer, Double, Double> pair=term.getValue();
			synchronized (allterms) {
				if(allterms.containsKey(word)){
					allterms.put(word, allterms.get(word)+pair.getValue0());
				}
				else{
					allterms.put(word, (long)pair.getValue0());
				}
				alldocterms++;
			}
			double frequence=(double)pair.getValue0()/(double)alltermscount;
			Triplet<Integer, Double, Double> pair_new=new Triplet<Integer, Double,Double>(pair.getValue0(), frequence,0.0);
			tf.put(word, pair_new);
		}
		return tf;
	}

	public static void main(String[] args) throws FileNotFoundException{
		wordseg xx=new wordseg();
		//String text = "在今年夏季的军方将领调整中，又有多位年轻将领被重用：成都军区政治部主任吴昌德中将，升任总政治部副主任；曾是“最年轻中将”的国防科技大学校长张育林，调任总装备部副部长；国防科技大学副校长杨学军升任该校校长，成为副大军区级将领中为数不多的“60后”。";
		//String text = "朱主爱的粉丝是不是四叶草，四叶草其实是TFBoys的粉丝啦，不是朱主爱的粉丝。不过朱主爱的艺名叫倒四叶草没错。";
//		String text="LINKIN PARK歌曲的演唱者麦克·信田";
		String text="Barack Obama was born in Hawaii, he was elected in 2008. Will Donald Trump destroy New York City";

		List<Result> result=xx.segWord_Result(text,3);
		for(Result tt:result){
			System.out.println(tt.keyword+"\t"+tt.type);
		}





	}
}
