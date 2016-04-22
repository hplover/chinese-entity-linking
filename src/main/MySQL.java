package main;

import java.sql.*;

public class MySQL {
//	  访问地址:    223.255.18.169  端口:3306
//  用户名:nudt
//  密码: bjgdFrist666
    public static void main(String[] args){
        try{
            //调用Class.forName()方法加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("成功加载MySQL驱动！");
                
            String url="jdbc:mysql://223.255.18.169:3306/gdkj";    //JDBC的URL    
            Connection conn;

            conn = DriverManager.getConnection(url,    "nudt","bjgdFrist666");
            Statement stmt = conn.createStatement(); //创建Statement对象
            System.out.println("成功连接到数据库！");

            String sql = "show databases";    //要执行的SQL
            ResultSet rs = stmt.executeQuery(sql);//创建数据对象
            while(rs.next()){
            	System.out.println(rs.getString(1) + "\t");
            }
            sql="use gdkj";
            stmt.executeQuery(sql);
            sql="show tables";
            rs=stmt.executeQuery(sql);
            while(rs.next()){
            	System.out.println(rs.getString(1) + "\t");
            }
            sql="select * from view_bdzd limit 4";
            rs=stmt.executeQuery(sql);
            System.out.println("\n\n");
            while(rs.next()){
//            	System.out.println(rs.getString(1));
//            	System.out.println(rs.getString(2));
//            	System.out.println(rs.getString(3));
//            	System.out.println(rs.getString(4));
//            	System.out.println(rs.getString(5));
//            	System.out.println(rs.getString(6));
            	System.out.println("BB:"+rs.toString());
            }
            sql="select * from view_bdzd";
            rs=stmt.executeQuery(sql);
            System.out.println("\n\nbasjdflaksdjf:"+rs.getFetchSize());
//            while(rs.next()){
//            	System.out.println(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t"+rs.getString(4)+"\t"+rs.getString(5)+"\t"+rs.getString(6)+"\t"+rs.getString(7)+"\t"+rs.getString(8)+"\t"+rs.getString(9)+"\t"+rs.getString(10)+"\t"+rs.getString(11)+"\t"+rs.getString(12)+"\t");
//            }
//                System.out.println("编号"+"\t"+"姓名"+"\t"+"年龄");
//                while (rs.next()){
//                    System.out.print(rs.getInt(1) + "\t");
//                    System.out.print(rs.getString(2) + "\t");
//                    System.out.print(rs.getInt(3) + "\t");
//                    System.out.println();
//                }
                rs.close();
                stmt.close();
                conn.close();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
    }
}
