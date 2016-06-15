package main.disambiguation.v4_PKBASE.subTools;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

public class PinyinEqual {
	static String wrong_pinyin;
	static String right_pinyin;
	
	private static boolean base_check(String right, String wrong){
		if(right.length()!=wrong.length()){
			return false;
		}
		right_pinyin=PinyinHelper.convertToPinyinString(right, " ", PinyinFormat.WITHOUT_TONE);
		wrong_pinyin=PinyinHelper.convertToPinyinString(wrong, " ", PinyinFormat.WITHOUT_TONE);
		return true;
	}
	public static boolean full_equal(String right, String wrong){
		if(base_check(right, wrong)==false){
			return false;
		}
		if(right_pinyin.equals(wrong_pinyin))
			return true;
		return false;
	}
	public static boolean hg_equal(String right, String wrong){
		if(base_check(right, wrong)==false){
			return false;
		}
		String[] rightArray=right_pinyin.split(" ");
		String[] wrongArray=wrong_pinyin.split(" ");
		if(rightArray.length!=wrongArray.length){
			System.err.println("pinyin convert error");
			return false;
		}
		for(int i=0;i<rightArray.length;i++){
//			if()
		}
		return false;
	}
}
