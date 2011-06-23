package com.xtremelabs.robolectric.sqlite;

import android.widget.ArrayAdapter;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicSqliteTest {
    //private ArrayAdapter<Integer> arrayAdapter;

    @Before public void setUp() throws Exception {
//        List<Integer> list = new ArrayList<Integer>();
//        list.add(1);
//        list.add(2);
//        list.add(3);
//
//        arrayAdapter = new ArrayAdapter<Integer>(Robolectric.application, 0, list);
    }

 
    @Test
    public void RunSqlite() {
    	 // load the sqlite-JDBC driver using the current class loader
        try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("SQlite JDBC cant be found! in RunSqLite",e1); 
		}
        
        Connection connection = null;
        try
        {
          // create a database connection
          connection = DriverManager.getConnection("jdbc:sqlite::memory:");
          Statement statement = connection.createStatement();
          statement.setQueryTimeout(30);  // set timeout to 30 sec.
          
          statement.executeUpdate("drop table if exists person");
          statement.executeUpdate("create table person (id integer, name string)");
          statement.executeUpdate("insert into person values(1, 'leo')");
          statement.executeUpdate("insert into person values(2, 'yui')");
          ResultSet rs = statement.executeQuery("select * from person");
          while(rs.next())
          {
            // read the result set
            System.out.println("name = " + rs.getString("name"));
            System.out.println("id = " + rs.getInt("id"));
          }
        }
        catch(SQLException e)
        {
          // if the error message is "out of memory", 
          // it probably means no database file is found
          System.err.println(e.getMessage());
        }
        finally
        {
          try
          {
            if(connection != null)
              connection.close();
          }
          catch(SQLException e)
          {
            // connection close failed.
            System.err.println(e);
          }
        }
      }

}

