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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.airlift.log.Logger;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;

public class GetSQL {

	private Logger LOG;
	public GetSQL(){
        /**
         * Tao Yang
         * 22/06/2014
         * use airlift Logger instead of log4j
         * because the dependent jar file link broken when maven works on log4j
         */
		LOG = Logger.get(GetSQL.class);
	}
	public String parse(String sparqlquery){
		
		// This method returns select + from + where
		String select = "";
		String from = "";
		String where = "";
		
		// parse the sparql query string
		Sparql sparql = new Sparql(sparqlquery);
		sparql.parse();
		
		// check basic information about sparql query
		int edgeNum = sparql.getEdgeNum();
		if(edgeNum <= 0){
			LOG.error("invalid sparql query!");
			return null;
		}
		
		List<Triple> triples = sparql.getTriples();
		
		// mark if one triple has been visited or not
		boolean[] visited = new boolean[edgeNum];
		int i = 0;
		for(; i < visited.length ; i ++)
			visited[i] = false;
		
		// join variable map, key is the variable name, value contains the triple id and subject/object info
		Map<String, NodeAttribute> nodemap = new HashMap<String, NodeAttribute>(); 
		
		// converse the first triple pattern 
		Triple t = triples.get(0);		
		Node subject = t.getSubject();
		Node object = t.getObject();
		Node predicate = t.getPredicate();
		int visitedNum = 1;
		visited[0] = true;
		
		if(predicate.isVariable()){
			LOG.error("predicate is variable, unsupported format!");
			return null;
		}
		
		from += " from " + getTableName(predicate) + " t" + String.valueOf(visitedNum);
		
		if(subject.isVariable()){
			nodemap.put(subject.toString(), new NodeAttribute(visitedNum, true));
			if(object.isConcrete()){
				where += " where t" + String.valueOf(visitedNum) + ".o = " + getValue(predicate, object.toString(), true); 
			}else{
				nodemap.put(object.toString(), new NodeAttribute(visitedNum, false));
			}
			
		}else if(object.isVariable()){
			
			nodemap.put(object.toString(), new NodeAttribute(visitedNum, false));
			where += " where t" + String.valueOf(visitedNum) + ".s = " + getValue(predicate, subject.toString(), false);	
			
		}else{
			
			LOG.error("invalid sparql query(both subject and object are concrete value)!");
			return null;
			
		}	
		
		// for the other triples, generate the joining clause and fill the where clause
		while(visitedNum < edgeNum){
			for(i = 0 ; i < edgeNum ; i ++){
				// if not visited, check if connected
				if(!visited[i]){
					t = triples.get(i);
					subject = t.getSubject();
					object = t.getObject();
					predicate = t.getPredicate();
					visitedNum ++;
					
					// if && else if, this triple connected with some visited one
					if(subject.isVariable() && nodemap.containsKey(subject.toString())){
						
						// add join
						from += " join " + getTableName(predicate) + " t" + String.valueOf(visitedNum) 
							+ getJoinClause(nodemap.get(subject.toString()), new NodeAttribute(visitedNum, true));
						
						// add concrete value to where clause
						if(object.isConcrete()){
							where += getWhereClause(new NodeAttribute(visitedNum, false)) + getValue(predicate, object.toString(), true);
						}
						// otherwise put it in map. If already contains, just update due to Map<Object, Object>
						else{
							nodemap.put(object.toString(), new NodeAttribute(visitedNum, false));
						}
						
						visited[i] = true;
						
					}else if(object.isVariable() && nodemap.containsKey(object.toString())){
						
						from += " join " + getTableName(predicate) + " t" + String.valueOf(visitedNum) 
							+ getJoinClause(nodemap.get(object.toString()), new NodeAttribute(visitedNum, false));		
						if(subject.isConcrete()){
							where += getWhereClause(new NodeAttribute(visitedNum, true)) + getValue(predicate, subject.toString(), false);
						}
						// subject is a variable but not visited
						else{
							nodemap.put(subject.toString(), new NodeAttribute(visitedNum, true));
						}
 						visited[i] = true;
 						
					}
					// this triple is not connected with visited one. Try to find another.
					else{
						
						visitedNum --;
					}
				}
			}
		}
		
		// use the nodemap to get the select clause
		select = getSelectClause(nodemap, sparqlquery);
		
		return select + from + where;
	}
	private String getTableName(Node predicate){		
		return predicate.toString().substring(predicate.toString().lastIndexOf('#') + 1);
	}
	private String getValue(Node predicate, String value, boolean isObject){
		String simplePre = getTableName(predicate);
		if(simplePre.equals("type") && isObject)
			return "'" + value.substring(value.lastIndexOf('#') + 1) + "'";
		else{
			return "'" + dictionize(value) + "'";
		}
	}
	private String getJoinClause(NodeAttribute left, NodeAttribute right){
		String result = " on t" + left.getTripleID() + ".";
		if(left.isSubject())
			result += "s";
		else
			result += "o";
		result += " = t" + right.getTripleID() + ".";
		if(right.isSubject())
			result += "s";
		else
			result += "o";
		return result;		
	}
	
	private String getWhereClause(NodeAttribute attr){
		String result = " and t" + String.valueOf(attr.getTripleID()) + ".";
		if(attr.isSubject())
			result += "s";
		else
			result += "o";
		result += " = ";
		return result;
	}
	
	private String getSelectClause(Map<String, NodeAttribute> nodemap, String sparql){
		String result = "select";
		
		// get the content between select and where keyword
		String str = sparql.toLowerCase();
		int start = str.indexOf("select"), end = str.indexOf("where");
		if(start == -1 || end == -1 || start > end+5){
			LOG.error("invalid sparql query with no 'SELECT' or 'WHERE' keyword!");
			return null;
		}
		
		// split using ',' or white space as delimiter
		str = sparql.substring(start + 7, end - 1);
		String[] variables = str.split(",|\\s");
		
		// go through the variables, fill the select clause with corresponding table and subject/object attributes.
		NodeAttribute attr = null;
		for(int i = 0 ; i < variables.length ; i ++){
			attr = nodemap.get(variables[i]);
			if(attr == null){
				LOG.error("cannot match variable " + variables[i] + " in where clause!");
				System.exit(-1);
			}
			result += " t" + attr.getTripleID() + ".";
			if(attr.isSubject())
				result += "s";
			else
				result += "o";
			if(i != variables.length - 1)
				result += ",";
		}
		return result;
	}
	private String dictionize(String str){
		if(str.length() < 1){
			LOG.error("string length of " + str + " is less than 1");
			return null;
		}

        /*
		String simp = "";
		char ch;
		
		for(int i = 0 ; i < str.length() ; i ++){
			ch = str.charAt(i);
			
			if( (ch >= 'A' && ch <= 'Z' ) || (ch >= '0' && ch <= '9')){
				simp += ch;
				if(ch == 'A'){
					if(str.charAt(i+3) == 'o'){
						simp += 'O';
						i += 7;
					}
					else if(str.charAt(i+3) == 'i'){
						simp += 'I';
						i += 7;
					}
					else{
						System.out.println("what the hell is this: " + str);
					}
				}
				
			}
		}		
		return simp;
		*/

        return str.substring(str.lastIndexOf('/') + 1);
	}
	public static void main(String[] args){
		
		/*
		String sparqlquery = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE " +
				"{?X rdf:type ub:GraduateStudent . " +
				" ?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. }";
		*/

        String sparqlquery =
                "PREFIX ruc: <http://www.ruc.edu.cn/univ-bench.owl#>" +
                 "SELECT ?X ?Y WHERE {" +
                 " ?X ruc:description ?Y ." +
                 " ?X ruc:title <欢迎访问党委组织部网站>. }" ;


//		QuerySet queries = new QuerySet();
		GetSQL sql = new GetSQL();

        String sqlquery = sql.parse(sparqlquery);
        System.out.println(sqlquery);

         /*
		for(int i = 1 ; i <= queries.getTotalNumber(); i ++)
		{
			String sqlquery = sql.parse(queries.getQuery(i));
			System.out.println("Query" + i + " " + sqlquery);		
		}
		*/
	}
}
