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

import io.airlift.log.Logger;
//import org.apache.log4j.Logger;

public class QuerySet {

	private String[] queries;
	private static int QNUM = 19;
	private Logger LOG = Logger.get(QuerySet.class);
	
	public QuerySet(String[] queries){
		this.queries = queries;
	}
	public QuerySet(){
		
		queries = new String[QNUM];
				
		queries[1] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:GraduateStudent .  " +
				"	?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. " +
				"	} ";
				
		queries[2] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
				"SELECT ?X ?Y ?Z WHERE {" +
				"	?X rdf:type ub:GraduateStudent .   " +
				"	?Y rdf:type ub:Department . " +
				"	?Z rdf:type ub:University. " +
				"	?X ub:memberOf ?Y .   " +
				"	?Y ub:subOrganizationOf ?Z .   " +
				"	?X ub:undergraduateDegreeFrom ?Z" +
				"	} ";
				
		queries[3] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:Publication .  " +
				"	?X ub:publicationAuthor  <http://www.Department0.University0.edu/AssistantProfessor0> ." +
				"	}";
				
		queries[4] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y1 ?Y2 ?Y3 WHERE {" +
				"	?X rdf:type ub:Professor .  " +
				"	?X ub:worksFor <http://www.Department0.University0.edu> .  " +
				"	?X ub:name ?Y1 .  " +
				"	?X ub:emailAddress ?Y2 ." +
				"	?X ub:telephone ?Y3" +
				"	}";
				
		queries[5] =
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE { " +
				"	?X rdf:type ub:Student .  " +
				"	?X ub:memberOf <http://www.Department0.University0.edu>. " +
				"	}";
		/*
		queries[5] = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
			"SELECT ?X WHERE { " +
			"	{ ?X rdf:type ub:FullProfessor } UNION { ?X rdf:type ub:AssociateProfessor } " +
			"	UNION {?X rdf:type ub:AssistantProfessor } UNION {?X rdf:type ub:GraduateStudent }" +
			"	UNION {?X rdf:type ub:UndergraduateStudent }  UNION {?X rdf:type ub:Lecture }" +
			"	?X ub:memberOf <http://www.Department0.University0.edu>. " +
			"	}";
		*/
				
		queries[6] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:Student . " +
				"	}";
				
		queries[7] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y WHERE {" +
				"	?X rdf:type ub:Student .  " +
				"	?Y rdf:type ub:Course .  " +
				"	?X ub:takesCourse ?Y .  " +
				"	<http://www.Department0.University0.edu/AssociateProfessor0> ub:teacherOf ?Y " +
				"	}";
				
		queries[8] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y ?Z WHERE {" +
				"	?X rdf:type ub:Student .  " +
				"	?Y rdf:type ub:Department .  " +
				"	?X ub:memberOf ?Y .  " +
				"	?Y ub:subOrganizationOf <http://www.University0.edu> .  " +
				"	?X ub:emailAddress ?Z" +
				"	}";
				
		queries[9] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"	PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"	SELECT ?X ?Y ?Z WHERE {" +
				"	?X rdf:type ub:Student .  " +
				"	?Y rdf:type ub:Faculty .  " +
				"	?Z rdf:type ub:Course .  " +
				"	?X ub:advisor ?Y .  " +
				"	?Y ub:teacherOf ?Z .  " +
				"	?X ub:takesCourse ?Z" +
				"	}";
				
		queries[10] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:Student .  " +
				"	?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>" +
				"	}";
				
		queries[11] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE{" +
				"	?X rdf:type ub:ResearchGroup .  " +
				"	?X ub:subOrganizationOf ?Y ." +
				"	?Y rdf:type ub:Department ." +
				"	?Y ub:subOrganizationOf <http://www.University0.edu>" +
				"	}";
			
		queries[12] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y WHERE {" +
				"	?X rdf:type ub:FullProfessor ." +
				"	?X ub:headOf ?Y . " +
				"	?Y rdf:type ub:Department . " +
				"	?Y ub:subOrganizationOf <http://www.University0.edu> ." +
				"	}";
				
//				"	?X rdf:type ub:Chair .  " +
//				"	?Y rdf:type ub:Department .  " +
//				"	?X ub:worksFor ?Y .  " +
//				"	?Y ub:subOrganizationOf <http://www.University0.edu>" +
//				"	}";
				
		queries[13] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE{" +
				"	?X rdf:type ub:Person .  " +
				"	<http://www.University0.edu> ub:hasAlumnus ?X" +
				"	}";
		/*
		queries[13] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE{" +
				"	{ ?X rdf:type ub:FullProfessor } UNION { ?X rdf:type ub:AssociateProfessor } " +
				"	UNION {?X rdf:type ub:AssistantProfessor } UNION {?X rdf:type ub:GraduateStudent }" +
				"	UNION {?X rdf:type ub:UndergraduateStudent }  UNION {?X rdf:type ub:Lecture }" +
				"	{?X ub:undergraduateDegreeFrom <http://www.University0.edu>} UNION " +
				"	{?X ub:masterDegreeFrom <http://www.University0.edu>} UNION" +
				"	{?X ub:doctoralDegreeFrom <http://www.University0.edu>} " +
				"	}";
		*/
				
		queries[14] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:UndergraduateStudent" +
				"	}";
		
		queries[15] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y ?Z WHERE {" +
				"	?X rdf:type ub:Professor ." +
				"	?X ub:worksFor ?Z." +
				"	?Z ub:subOrganizationOf <http://www.University0.edu>." +
				"	?X ub:publicationAuthor ?Y." +
				"	}";
		
		queries[16] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X ?Y WHERE {" +
				"	?X rdf:type ub:Professor." +
				"	?X ub:worksFor ?Z." +
				"	?Z ub:subOrganizationOf <http://www.University0.edu> ." +
				"	?Y ub:advisor ?X." +
				"	?Y rdf:type ub:GraduateStudent" +
				"	}";
		
		queries[17] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:GraduateStudent ." +
				"	?X ub:memberOf ?Y ." +
				"	?Y ub:subOrganizationOf <http://www.University0.edu> ." +
				"	?X ub:takesCourse ?Z ." +
				"	?Z rdf:type ub:GraduateCourse ." +
				"	}";
		
		queries[18] = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>" +
				"SELECT ?X WHERE {" +
				"	?X rdf:type ub:GraduateCourse ." +
				"	?Y ub:teacherOf ?X ." +
				"	?Y ub:worksFor ?Z ." +
				"	?Z ub:subOrganizationOf <http://www.University0.edu> ." +
				"	}";
		
	}
	
	public void setQuery(int num, String queryString){
		queries[num] = queryString;
	}
	public int getTotalNumber(){
		return QNUM-1;
	}
	public String getQuery(int num){
		if(num >= 1 && num < QNUM)
			return queries[num];
		else{
			LOG.error("index of queries is out of range! only 1-" + QNUM + "is supported");
			return null;
		}
	}
	
}
