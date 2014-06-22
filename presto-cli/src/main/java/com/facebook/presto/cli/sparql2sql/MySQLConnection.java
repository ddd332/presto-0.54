/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.presto.cli.sparql2sql;

import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.*;

import java.sql.*;
import java.sql.Statement;

import java.util.List;

/**
 * Created by deke on 5/23/14.
 */
public class MySQLConnection {

    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;

    public boolean open(){
        if(conn == null)
            return false;
        return true;
    }
    public void openConnection(){
        try{
            conn = null;
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://10.77.50.209:3306/hive", "root", "111111");

        }catch(ClassNotFoundException e){
            System.out.println("Unable to find driver");
            e.printStackTrace();
        }catch(SQLException e){
            System.out.println("Error in get Connection");
            e.printStackTrace();
        }
    }
    public void executeQuery(String sql){
        if(conn == null)
            return;
        try{
            if(stmt == null)
                stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

        }catch(SQLException e){
            System.out.println("Error in get results");
            e.printStackTrace();
        }
    }

    public ResultSet getResultSet(){
        return rs;
    }
    public void close(){
        try{
            if(rs != null)
                rs.close();
            if(stmt != null)
                stmt.close();
            if(conn != null)
                conn.close();
        }catch(SQLException e){
            System.out.println("Error in closing");
            e.printStackTrace();
        }
    }

    protected void finalize(){
        try{
            super.finalize();
            close();
        }catch(Throwable e){
            e.printStackTrace();
        }

    }

    public static void main(String args[]){
//        MySQLConnection my = new MySQLConnection();
//        my.openConnection();
        String sql = "select * from TBLS limit 2";
        String sqlComplex = "select testpartition.filename,format_.o,title.s,identifier.o,id.o,date_.o " +
                "from testpartition join format_ on (testpartition.filename=format_.s) " +
                "join title on (testpartition.filename=title.s) " +
                "join identifier on (testpartition.filename=identifier.s) " +
                "join id on (testpartition.filename=id.s) " +
                "join date_ on (testpartition.filename=date_.s) " +
                "where testpartition.gid0=1  and testpartition.sid=2 and testpartition.fid<100000 and testpartition.pfid=0";
        String sqlOriginal = "select idTable.filename,format_.o,title.s,identifier.o,id.o,date_.o " +
                "from idTable join format_ on (idTable.filename=format_.s) " +
                "join title on (idTable.filename=title.s) " +
                "join identifier on (idTable.filename=identifier.s) " +
                "join id on (idTable.filename=id.s) " +
                "join date_ on (idTable.filename=date_.s) " +
                "where idTable.gid0=1  and idTable.sid=2 and idTable.fid<100000";

        QueryRewrite my = new QueryRewrite(sqlOriginal);
        String sqlRewrite = my.rewrite();
        System.out.println(sqlRewrite);


    }
}
