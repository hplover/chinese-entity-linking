package WordSeg;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.hankcs.hanlp.dependency.nnparser.parser_dll;
import com.hankcs.hanlp.seg.common.Term;
import com.mongodb.AggregationOptions.Builder;

import javatuples.Pair;
import javatuples.Triplet;

public class wordseg {
	static String path="C:\\javarequest\\models\\nlp\\";
	static BigSegment sgmt = null;
	public static HashMap<String, Long> allterms=new HashMap<>();
	public static long alldocterms=0;
	static PrintStream original=System.out;
	static String[] getProperty={"nt","nz","nt","nr"};//stop word
	static String[] nounWord={"n","nt","nz","nr"};
	static Set<String> getN;
	public static Set<String> getP;
	static{
		getP=new HashSet<>(Arrays.asList(getProperty));
		getN=new HashSet<>(Arrays.asList(nounWord));
		sgmt = new BigSegment(path);
	}
	
	private static String trimText(String text){
		if(text.length() > 6003){
			text = text.substring(0, 6001);
		}
		char[] chs = new char[text.length()];
		text = LangUtils.mapFullWidthLetterToHalfWidth(text);
		text = LangUtils.mapChineseMarksToAnsi(text);
		text = LangUtils.mapFullWidthNumberToHalfWidth(text);
		text = LangUtils.removeEmptyLines(text);
		text = LangUtils.removeExtraSpaces(text);
		text = LangUtils.removeLineEnds(text);
		int id = 0;
		for(int i=0;i<text.length();i++){
			char c = text.charAt(i);
			if(LangUtils.isChinese(c)|| ((int)c >31&& (int)c<128)){
				chs[id]=c;
				id++;
			}
		}
		text = LangUtils.T2S(new String(chs).trim());//否则后面会有空格
		return LangUtils.replaceAnsiMarkWithSpace(text);
	}
	
	@SuppressWarnings("null")
	public List<Result> segWord_Result(String text_single,int mode) {
		System.out.println("Src:"+text_single);
		text_single=trimText(text_single);
		System.out.println("Trim:"+text_single);
		List<Result> ReList = new ArrayList<>();
		HashSet<String> hSet=new HashSet<>();//avoid replication
		String[] textarray=text_single.split(",");
		System.out.print("Segment:");
		for(String text:textarray){
			List<Term> termReList = sgmt.getHanLPSegment(mode, text);
			for(Term result:termReList){
				String ttype=result.nature.name();
				System.out.print(result.word+" ");
				if(hSet.contains(result.word))
					continue;
				else {
					hSet.add(result.word);
				}
				if(!getN.contains(ttype))
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
//		System.out.println("Src:"+text_single);
		text_single=trimText(text_single);
//		System.out.println("Trim:"+text_single);
		HashSet<String> hSet=new HashSet<>();//avoid replication
		String[] textarray=text_single.split(",");
//		System.out.print("Segment:");
		for(String text:textarray){
			List<Term> termReList = sgmt.getHanLPSegment(mode, text);
			for(Term result:termReList){
				String ttype=result.nature.name();
				System.out.print(result.word+" ");
				if(hSet.contains(result.word))
					continue;
				else {
					if(!getN.contains(ttype))
						continue;
					hSet.add(result.word);
				}
			}
		}
		return hSet;
	}
	
	public static HashMap<String, Triplet<Integer, Double, Double>> segWord_TF(String text_single,int mode) {//Integer means term count,the first Double means term frequence,the second Double means idf
		text_single=trimText(text_single);
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
				if(!getN.contains(ttype))
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
		String text = "在今年夏季的军方将领调整中，又有多位年轻将领被重用：成都军区政治部主任吴昌德中将，升任总政治部副主任；曾是“最年轻中将”的国防科技大学校长张育林，调任总装备部副部长；中国人民解放军国防科学技术大学副校长杨学军升任该校校长，成为副大军区级将领中为数不多的“60后”";
//		String text="新时期生理生态学的发展，面临分子生物学和组学（-omics）等学科和技术手段的挑战，同时新学科和新技术发展也为生理生态学的发展提供了新的机遇。不同动物类群适应各种不同环境（尤其是极端环境）的机理，需要从各个组织层次上、各学科交叉进行整合研究。动物对不断变化着的环境的生理功能的调节和改变及其适应和进化机理，将是生理生态学未来的重要研究主题。我们也看到，生理生态学在动物行为学、保护生物学、种群生态学和群落生态学中地位和作用，显得日趋重要。保护生理学（conservation physiology）、代谢生态学（metabolicecology）和宏生理学（macrophysiology）等新领域的兴起，也进一步充实和拓展了生理生态学的内涵。";
//		String text="多聚酶链式反应（PCR）是一项用于体外复制DNA的极为通用的技术。简而言之，PCR技术可以使单链DNA被复制数百万次，也允许用事先确定好的方式对被复制的DNA序列进行改动。例如，PCR技术可以用于引入限制性酶切位点，或者对特定的DNA碱基进行突变（改变）。PCR技术还可以用于从cDNA文库获得特定的DNA片段，或者从另一个角度，用于判断一个cDNA文库中是否含有特定的DNA片段。";
		System.out.println(text);
//		List<Result> result=xx.segWord_Result(text,3);
//		for(Result tt:result){
//			System.out.println(tt.keyword+"\t"+tt.type);
//		}
		HashMap<String, Triplet<Integer, Double, Double>> result1=xx.segWord_TF(text,3);
//		for(Entry<String, Triplet<Integer, Double, Double>> tt:result1.entrySet()){
//			tt.getValue().add(tuple)
//			System.out.println(tt.getKey()+"\t"+tt.getValue());
//		}
	}
}
