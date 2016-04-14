package tools.WordSeg;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ProcessEntityName {
	public static void main(String [] args) throws IOException{
		File file=new File("E://entityName");
		@SuppressWarnings("resource")
		BufferedReader bre = new BufferedReader(new FileReader(file));//此时获取到的bre就是整个文件的缓存流
		String xx="";
		long i_sum=0;
		long i_norepeat=0;
		HashSet<String> hSet=new HashSet<>();
		List<String> result=new ArrayList<>();
		while ((xx = bre.readLine())!= null) // 判断最后一行不存在，为空结束循环
		{
			i_sum++;
			xx=LangUtils.replaceAnsiMarkWithSpace(xx);
			if(xx.isEmpty()){
				continue;
			}
			if(i_sum%1000==0){
				System.out.println("processing... "+i_norepeat+"/"+i_sum);
			}
			if(!hSet.contains(xx)){
				if(!xx.isEmpty()||xx.length()!=0){
					hSet.add(xx);
					result.add(xx);
					i_norepeat++;
				}
			}
			else {
				continue;
			}
		}
//		Iterator<String> iterator=hSet.iterator();
		i_sum=0;
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E://entityOut_v2"), "utf-8"));
//		while(iterator.hasNext()){
//			
//			xx=iterator.next();
//			writer.write(xx+"\n");
//		}
		for(String ll:result){
			i_sum++;
			if(i_sum%1000==0){
				System.out.println("writing... "+i_sum+"/"+i_norepeat);
			}
			writer.write(ll+"\n");
		}
		writer.close();
	}
}
