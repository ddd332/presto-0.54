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

import com.facebook.presto.sql.tree.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by deke on 5/24/14.
 */
public class QueryRewrite {

    private class AttributeRecord{
        private String origin;
        private String target;
        private int param;

        public AttributeRecord(String s1, String s2, int param){
            origin = s1;
            target = s2;
            this.param = param;
        }

        public String getOrigin(){
            return origin;
        }

        public String getTarget(){
            return target;
        }

        public int getParam(){
            return param;
        }
    }

    class Pair{
        private Integer key;
        private String value;

        public Pair(Integer key, String value){
            this.key = key;
            this.value = value;
        }

        public Integer getKey(){
            return key;
        }

        public String getValue(){
            return value;
        }
    }

    String sql;
    String rewriteSql;
    Map<String, Pair> tablepair;
    QueryPrinter printer;
    MySQLConnection connection;

    private static String getTargetTable = "select ID, ORIGIN, TARGET from TBL_TRANSFORM";

    public QueryRewrite(String sql){
        setSql(sql);
        init();
    }
    private void init(){
        rewriteSql = getSql().toLowerCase();
        printer = new QueryPrinter();
        connection = new MySQLConnection();
        tablepair = new HashMap<>();
    }
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    private boolean checkFrom(){

        boolean isTransform = false;
        printer.parseQuery(getSql());
        List<Table> tables = printer.getFroms();
        Map<String, Pair> targets = getTableFromMysql();
        for(int i = 0 ; i < tables.size() ; i ++){
            String str = tables.get(i).getName().toString();
            if(targets.keySet().contains(str)){
                System.out.println(tables.get(i).getName() + " has been transformed!");
                tablepair.put(str, targets.get(str));
                isTransform = true;
            }
        }
        return isTransform;
    }

    /*
    // Usage: parse table from string like "a.b.c.d", returns "a"
    private String parseTable(String str){
        int loc = str.indexOf('.');
        if(loc != -1)
            return str.substring(0, loc);
        else
            return str;
    }
    */

    private Map<Integer, List<AttributeRecord>> getAttribute(){
        if(tablepair == null || tablepair.size() == 0){
            System.err.println("No table involved, check from should return false!");
            return null;
        }
        if(!connection.open()){
            System.err.println("DB Connection is closed after fetch table!");
            connection.openConnection();
        }
        String query = "select TBL_ID, ORIGIN_ATTR, TARGET_ATTR, FLOOR_PARAM from PARTITION_INFO where TBL_ID in (" ;
        Iterator iterator = tablepair.keySet().iterator();
        while(iterator.hasNext()){
            query += tablepair.get(iterator.next()).getKey();
            if(iterator.hasNext())
                query += ", ";
            else
                query += ") order by TBL_ID";
        }
        Map<Integer, List<AttributeRecord>> attributes = new HashMap<>();
        try{
            connection.executeQuery(query);
            ResultSet rs = connection.getResultSet();
            while(rs.next()){
                int id = rs.getInt("TBL_ID");
                List<AttributeRecord> records;
                if(attributes.containsKey(id)){
                    records = attributes.get(id);

                }else{
                    records = new ArrayList<>();
                }
                records.add(new AttributeRecord(rs.getString("ORIGIN_ATTR"), rs.getString("TARGET_ATTR"), rs.getInt("FLOOR_PARAM")));
                attributes.put(id, records);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return attributes;
    }

    private boolean checkWhere(){

        // TODO: check where clause to see if the transformed table appears here, whether with it's attributes and conditions.
        boolean isTransform = false;
        List<ComparisonExpression> whereExp = printer.getWheres();

        Map<Integer, List<AttributeRecord>> attributes = getAttribute();

        // Traverse each where clause
        for(ComparisonExpression e : whereExp){
            if(!(e.getLeft() instanceof QualifiedNameReference)){
                System.err.println("do not support " + e.getLeft().getClass().getName() + " in where clause!");
                return false;
            }
            QualifiedNameReference left = (QualifiedNameReference)(e.getLeft());
            String table = left.getPrefix().toString();
            if(tablepair.keySet().contains(table)){

                String attr = left.getSuffix().toString();
                int id = tablepair.get(table).getKey();

                // TODO: check if this attr appears in database
                for(AttributeRecord ar : attributes.get(id)){
                    if(ar.getOrigin().equals(attr)){

                        // TODO: get one attribute here, going to check the parameter!
                        System.out.println("catch one attribute! " + left.toString());
                        addConstraint(e, tablepair.get(table).getValue(), ar);
                        isTransform = true;
                    }
                }
                rewriteSql = rewriteSql.replaceAll(table, tablepair.get(table).getValue());
            }
        }
        return isTransform;
    }


    private void addConstraint(ComparisonExpression expression, String replaceTable, AttributeRecord record ){
        String constraints = "";
        Expression exp = expression.getRight();
        double value;
        if(exp instanceof LongLiteral){
            value = ((LongLiteral) exp).getValue();
        }else if(exp instanceof DoubleLiteral){
            value = ((DoubleLiteral) exp).getValue();
        }else{
            System.err.println("Do not support partition of type " + exp.getClass().getName());
            return;
        }

        ComparisonExpression.Type type = expression.getType();

        switch (type){
            case EQUAL:
                constraints += replaceTable + "." + record.getTarget() + " = " + (int)(value/record.getParam());
                break;
            case LESS_THAN:
                for(int i = 0 ; i < value/record.getParam(); i ++)
                {
                    if(i != 0)
                        constraints += " or ";
                    constraints += replaceTable + "." + record.getTarget() + " = " + i;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                for(int i = 0 ; i <= value/record.getParam(); i ++)
                {
                    if(i != 0)
                        constraints += " or ";
                    constraints += replaceTable + "." + record.getTarget() + " = " + i;
                }
                break;
            default:
                System.err.println("No upper bound with comparison " + type.getValue() + ", cannot using partition information!");
                return;
        }

        rewriteSql += " and (" + constraints + ")";

    }

    private Map<String, Pair> getTableFromMysql(){
        if(!connection.open())
            connection.openConnection();
        Map<String, Pair> tables = new HashMap<>();
        try{
            connection.executeQuery(getTargetTable);
            ResultSet rs = connection.getResultSet();

            while(rs.next()){
                tables.put(rs.getString("ORIGIN"), new Pair(rs.getInt("ID"), rs.getString("TARGET")));
            }
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        return tables;
    }


    public String rewrite(){
        if(checkFrom() && checkWhere())
//            System.out.println(rewriteSql);

        connection.close();
        return rewriteSql;
    }

}
