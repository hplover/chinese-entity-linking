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
//	static String[] getProperty={"nt","nz","nt","nr","nf","ns","n"};
//	static String[] nounWord={"n","nt","nz","nr","nf","nf"};
//	static Set<String> getN;
//	public static Set<String> getP;
	static{
//		getP=new HashSet<>(Arrays.asList(getProperty));
//		getN=new HashSet<>(Arrays.asList(nounWord));
		long a=System.currentTimeMillis();
		sgmt = new BigSegment(path);
		long b=System.currentTimeMillis();
		System.out.println("load segment time: "+(b-a));
	}
	
	public static String trimText(String text){
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
		text = LangUtils.T2S(new String(chs).trim());//鍚﹀垯鍚庨潰浼氭湁绌烘牸
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
//				System.out.println(result.word+" "+ttype);
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
//		System.out.println("Src:"+text_single);
//		text_single=trimText(text_single);
//		System.out.println("Trim:"+text_single);
		HashSet<String> hSet=new HashSet<>();//avoid replication
		String[] textarray=text_single.split(",");
//		System.out.print("Segment:");
		for(String text:textarray){
			List<Term> termReList = sgmt.getHanLPSegment(mode, text);
			for(Term result:termReList){
				String ttype=result.nature.name();
//				System.out.print(result.word+" ");
				if(hSet.contains(result.word))
					continue;
				else {
					if(!ttype.startsWith("n"))
						continue;
					hSet.add(result.word.toUpperCase());
				}
			}
		}
		return hSet;
	}
	
	public static HashMap<String, Triplet<Integer, Double, Double>> segWord_TF(String text_single,int mode) {//Integer means term count,the first Double means term frequence,the second Double means idf
//		text_single=trimText(text_single);
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
		String text = "鍦ㄤ粖骞村瀛ｇ殑鍐涙柟灏嗛璋冩暣涓紝鍙堟湁澶氫綅骞磋交灏嗛琚噸鐢細鎴愰兘鍐涘尯鏀挎不閮ㄤ富浠诲惔鏄屽痉涓皢锛屽崌浠绘�绘斂娌婚儴鍓富浠伙紱鏇炬槸鈥滄渶骞磋交涓皢鈥濈殑鍥介槻绉戞妧澶у鏍￠暱寮犺偛鏋楋紝璋冧换鎬昏澶囬儴鍓儴闀匡紱涓浗浜烘皯瑙ｆ斁鍐涘浗闃茬瀛︽妧鏈ぇ瀛﹀壇鏍￠暱鏉ㄥ鍐涘崌浠昏鏍℃牎闀匡紝鎴愪负鍓ぇ鍐涘尯绾у皢棰嗕腑涓烘暟涓嶅鐨勨��60鍚庘��,榛勪笢宸濇槸SB鍝堝搱鍝堟渤鍗楃渷鍖栧鐢熺墿涓庢湁鏈哄寲瀛﹂噸鐐瑰疄楠屽";
//		String text="鏂版椂鏈熺敓鐞嗙敓鎬佸鐨勫彂灞曪紝闈复鍒嗗瓙鐢熺墿瀛﹀拰缁勫锛�-omics锛夌瓑瀛︾鍜屾妧鏈墜娈电殑鎸戞垬锛屽悓鏃舵柊瀛︾鍜屾柊鎶�鏈彂灞曚篃涓虹敓鐞嗙敓鎬佸鐨勫彂灞曟彁渚涗簡鏂扮殑鏈洪亣銆備笉鍚屽姩鐗╃被缇ら�傚簲鍚勭涓嶅悓鐜锛堝挨鍏舵槸鏋佺鐜锛夌殑鏈虹悊锛岄渶瑕佷粠鍚勪釜缁勭粐灞傛涓娿�佸悇瀛︾浜ゅ弶杩涜鏁村悎鐮旂┒銆傚姩鐗╁涓嶆柇鍙樺寲鐫�鐨勭幆澧冪殑鐢熺悊鍔熻兘鐨勮皟鑺傚拰鏀瑰彉鍙婂叾閫傚簲鍜岃繘鍖栨満鐞嗭紝灏嗘槸鐢熺悊鐢熸�佸鏈潵鐨勯噸瑕佺爺绌朵富棰樸�傛垜浠篃鐪嬪埌锛岀敓鐞嗙敓鎬佸鍦ㄥ姩鐗╄涓哄銆佷繚鎶ょ敓鐗╁銆佺缇ょ敓鎬佸鍜岀兢钀界敓鎬佸涓湴浣嶅拰浣滅敤锛屾樉寰楁棩瓒嬮噸瑕併�備繚鎶ょ敓鐞嗗锛坈onservation physiology锛夈�佷唬璋㈢敓鎬佸锛坢etabolicecology锛夊拰瀹忕敓鐞嗗锛坢acrophysiology锛夌瓑鏂伴鍩熺殑鍏磋捣锛屼篃杩涗竴姝ュ厖瀹炲拰鎷撳睍浜嗙敓鐞嗙敓鎬佸鐨勫唴娑点��";
//		String text="澶氳仛閰堕摼寮忓弽搴旓紙PCR锛夋槸涓�椤圭敤浜庝綋澶栧鍒禗NA鐨勬瀬涓洪�氱敤鐨勬妧鏈�傜畝鑰岃█涔嬶紝PCR鎶�鏈彲浠ヤ娇鍗曢摼DNA琚鍒舵暟鐧句竾娆★紝涔熷厑璁哥敤浜嬪厛纭畾濂界殑鏂瑰紡瀵硅澶嶅埗鐨凞NA搴忓垪杩涜鏀瑰姩銆備緥濡傦紝PCR鎶�鏈彲浠ョ敤浜庡紩鍏ラ檺鍒舵�ч叾鍒囦綅鐐癸紝鎴栬�呭鐗瑰畾鐨凞NA纰卞熀杩涜绐佸彉锛堟敼鍙橈級銆侾CR鎶�鏈繕鍙互鐢ㄤ簬浠巆DNA鏂囧簱鑾峰緱鐗瑰畾鐨凞NA鐗囨锛屾垨鑰呬粠鍙︿竴涓搴︼紝鐢ㄤ簬鍒ゆ柇涓�涓猚DNA鏂囧簱涓槸鍚﹀惈鏈夌壒瀹氱殑DNA鐗囨銆�";
		System.out.println(text);
		List<Result> result=xx.segWord_Result(text,3);
		for(Result tt:result){
			System.out.println(tt.keyword+"\t"+tt.type);
		}
//		List<Result> result1=xx.segWord_Result(text,3);
//		for(Entry<String, Triplet<Integer, Double, Double>> tt:result1.entrySet()){
//			tt.getValue().add(tuple)
//			System.out.println(tt.getKey()+"\t"+tt.getValue());
//		}
	}
}
