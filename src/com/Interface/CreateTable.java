package com.Interface;
import java.sql.*;
import java.util.*;

public class CreateTable {
    static final String DB_URL = "jdbc:mysql://localhost:3306/qq?useSSL=false";
    static final String USER = "root";
    static final String PASS = "123456";
    public CreateTable(int id, String name, String pwd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ArrayList<String> strArray = new ArrayList<String>();
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            String sql ="insert into admin(id,name,pwd,head,back) value(?,?,?,?,?);  ";
            pstmt = conn.prepareStatement(sql);
            //test admin_id
            pstmt.setInt(1,id);
            pstmt.setString(2,name);
            pstmt.setString(3,pwd);
            pstmt.setString(4,"9.gif");
            pstmt.setString(5,"19.jpg");
            pstmt.executeUpdate();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(pstmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
}
