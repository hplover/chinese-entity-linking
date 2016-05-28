package main;

import com.hankcs.hanlp.HanLP;

public class hanlptext {
	public static void main(String[] args){
//		HanLP.parseDependency("LINKIN PARK歌曲的演唱者麦克·信田");
		String bb="123";
		String cc=bb.replaceAll("\\d", "");
		System.out.println(bb+"\nafter"+cc);
	}
}
