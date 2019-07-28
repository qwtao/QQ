import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class test_mysql {
    static final String DB_URL = "jdbc:mysql://localhost:3306/qq?useSSL=false";
    static final String USER = "root";
    static final String PASS = "123456";

    public static void main(String []args){
        Connection conn = null;

        try{
            Class.forName("com.mysql.jdbc.Driver");

            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                if(conn != null)
                    conn.close();
            } catch (SQLException se){
                se.printStackTrace();
            }


        }

    }
}
