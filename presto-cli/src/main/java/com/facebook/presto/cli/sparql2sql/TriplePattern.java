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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class TriplePattern {
	
	public static List<Triple> pipes(Element element) { return pipes(new ArrayList<Triple>(), element) ; }

    public static List<Triple> pipes(List<Triple> s, Element element){
    	 
        ElementVisitor v = new PatternVarsVisitor(s) ;
        ElementWalker.Walker walker = new WalkerSkipMinus(v) ;
        ElementWalker.walk(element, walker, v) ;
        return s ;
    }
    
    static class WalkerSkipMinus extends ElementWalker.Walker{

        protected WalkerSkipMinus(ElementVisitor visitor) {
            super(visitor) ;	           
        }
        
        @Override
        public void visit(ElementMinus el){
            proc.visit(el) ;
        }
    }

    static class PatternVarsVisitor extends ElementVisitorBase {
        private List<Triple> acc ;
        
        private PatternVarsVisitor(List<Triple> s){ 
        	acc = s ;        
        }  
        public void visit(ElementTriplesBlock el){
            for (Iterator<Triple> iter = el.patternElts() ; iter.hasNext() ; ){
                Triple t = iter.next() ;               
                acc.add(t);
            }
        }
    }		
}
