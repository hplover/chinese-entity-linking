package main;

import java.sql.SQLException;

public class Mysql {
//	  访问地址:    223.255.18.169  端口:3306
//    用户名:nudt
//    密码: bjgdFrist666
	public static void main(String[] args) throws SQLException {  
        //1.注册驱动  
        try {  
            Class.forName("com.mysql.jdbc.Driver");  
        } catch (ClassNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
          
        //2.创建数据库的连接  
        //useUnicode=true&characterEncoding=GBK：支持中文  
        java.sql.Connection conn = java.sql.DriverManager.getConnection(  
                "jdbc:mysql://223.255.18.169:3306/gdkj?useUnicode=true&characterEncoding=GBK",  
                "nudt", "bjgdFrist666");  
          
        //3获取表达式SQL  
        java.sql.Statement stmt = conn.createStatement();  
          
        //4.执行SQL  
        String sql = "select * from test";  
        java.sql.ResultSet res = stmt.executeQuery(sql);  
          
        //5.打印结果集里的数据  
        while(res.next()) {  
            System.out.print("the id: ");  
            System.out.println(res.getInt(1));  
            System.out.print("the user: ");  
            System.out.println(res.getString("user"));  
            System.out.print("the address: ");  
            System.out.println(res.getString("addr"));  
            System.out.println();  
        }  
          
          
        //测试插入数据库的功能：  
        //String inSql = "insert into test(user,addr) values('插入2','新地址2')";  
        //stmt.executeUpdate(inSql);  
          
        //6.释放资源，关闭连接（这是一个良好的习惯）  
        res.close();  
        stmt.close();  
        conn.close();  
    }  
}
