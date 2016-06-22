package main.disambiguation.v4_PKBASE.subTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


public class Word2Vec {
	static boolean isLoad=false;
	private static int topNSize = 40;
	private static  HashMap<String, float[]> wordMap = new HashMap<String, float[]>();
	private static String path="resources/wiki.zh.text.vector";
	
	public static void loadSummaryModel(String path) throws IOException {
		if(isLoad==true){
			return ;
		}
		@SuppressWarnings("resource")
		BufferedReader bufferedReader=new BufferedReader(new FileReader(path));
		String line;
		String word;
		float vector=0;
		float[] value = null;
		String[] word_value = null;
		bufferedReader.readLine();
		while((line=bufferedReader.readLine()) != null){
			double len = 0;
			word_value=line.split(" ");
			word=word_value[0];
			value=new float[word_value.length-1];
			for(int i=1;i<word_value.length;i++){
				vector=Float.parseFloat(word_value[i]);
				len+=vector*vector;
				value[i-1]=vector;
			}
			len=Math.sqrt(len);
			for(int i=0;i<value.length;i++){
				value[i]/=len;
			}
			wordMap.put(word, value);
		}
		isLoad=true;
	}
	
	public static Set<WordEntry> distance(String queryWord) {
		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}
	
	public static double twoWordsDis(String w1,String w2){
		try {
			loadSummaryModel(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wordMap.isEmpty()){
			return 0;
		}
		double result = 0;
		float[] v1=wordMap.get(w1);
		float[] v2=wordMap.get(w2);
		if(v1==null||v2==null||v1.length!=v2.length){
			return -1;
		}
		for(int i=0;i<v1.length;i++){
			result+=v1[i]*v2[i];
		}
		return result;
	}
	
	public static double twoSetsDis(Set<String> s1, Set<String> s2, double similarity_threshold){
		try {
			loadSummaryModel(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double result=0;
		double results=0;
		int word_count=1;
		for(String w1:s1){
			for(String w2:s2){
				result=twoWordsDis(w1, w2);
				if(result>similarity_threshold){
					results+=result;
					word_count++;//因为similarity_threshold很小，所以不用担心{A,B,C},{A,W,Z,Y,X}这种情况
				}
			}
		}
		return results/word_count;
	}
	public static void main(String[] args){
		while(true){
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Enter two strings: ");
			String n = reader.nextLine(); // Scans the next token of the input as an int.
			String m = reader.nextLine();
			System.out.println(twoWordsDis(n, m));
			System.out.println();
		}
	}
}
	
