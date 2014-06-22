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
import com.google.common.base.Optional;

import java.util.*;

/**
 * Created by deke on 5/23/14.
 */
public class QueryPrinter<C>{


    List<SelectItem> selects;
    List<Expression> wheres;
    Set<Table> froms;
    QueryPrintVisitor printer;

    public QueryPrinter(){
        printer = new QueryPrintVisitor();
        selects = new ArrayList<SelectItem>();
        wheres = new ArrayList<Expression>();
        froms = new HashSet<Table>();
    }
    public void parseQuery(String sql){
        Query query = (Query)(SqlParser.createStatement(sql));
        QuerySpecification specification = (QuerySpecification)(query.getQueryBody());
        rewrite(specification, null);
    }

    public <T extends Expression> T rewrite(T node, C context)
    {
        return (T) printer.process(node, new Context<>(context, false));
    }

    public <T extends Node> T rewrite(T node, C context)
    {
//        return (T) printer.process(node, new Context<>(context, false));
        printer.process(node, new Context<>(context, false));
        return null;
    }

    public List<SelectItem> getSelects(){
        return selects;
    }
    public List<Expression> getWheres(){
        return wheres;
    }
    public List<Table> getFroms(){
        List<Table> lists = new ArrayList<>();
        Iterator<Table> iter = froms.iterator();
        while(iter.hasNext()){
            lists.add(iter.next());
        }
        return lists;
    }

    // if the inner class has it's own generic type "C", QueryPrinter do not have to add <C>
    public class QueryPrintVisitor
            extends AstVisitor<Node, QueryPrinter.Context<C>> {

        @Override
        public Node visitSelect(Select node, Context<C> context){
            selects = node.getSelectItems();
            return null;
        }


        @Override
        public Node visitJoin(Join node, Context<C> context){
            // TODO: add the join visit code
            if(node.getLeft() instanceof Table){
                froms.add((Table)node.getLeft());
            }
            else
                rewrite(node.getLeft(), null);
            if(node.getRight() instanceof Table){
                froms.add((Table)node.getRight());
            }
            else
                rewrite(node.getRight(), null);
            return null;
        }

        @Override
        public Node visitComparisonExpression(ComparisonExpression node, Context<C> context){
            Expression left = node.getLeft();
            Expression right = node.getRight();

            // make sure that the comparison always appears like "a.b > 100" instead of "100 < a.b"
            if(left instanceof Literal && right instanceof QualifiedNameReference){
                ComparisonExpression rewritenNode;
                ComparisonExpression.Type type = node.getType();
                switch(type){
                    case LESS_THAN:
                        rewritenNode = new ComparisonExpression(ComparisonExpression.Type.GREATER_THAN, right, left);
                        break;
                    case LESS_THAN_OR_EQUAL:
                        rewritenNode = new ComparisonExpression(ComparisonExpression.Type.GREATER_THAN_OR_EQUAL, right, left);
                        break;
                    default:
                        rewritenNode = new ComparisonExpression(type, right, left);
                }
                wheres.add(rewritenNode);
            }
            else
                wheres.add(node);
            return null;
        }

        @Override
        public Node visitLogicalBinaryExpression(LogicalBinaryExpression node, Context<C> context){
            if(context == null){
                rewrite(node.getLeft(), null);
                rewrite(node.getRight(), null);
            }
            else{
                rewrite(node.getLeft(), context.get());
                rewrite(node.getRight(), context.get());
            }
            return null;
        }

        @Override
        public Node visitQualifiedNameReference(QualifiedNameReference node, Context<C> context){

            QualifiedName name = node.getName();
            int num = name.getParts().size();

            for(int i = 0 ; i < num - 1 ; i ++)
                System.out.print(name.getParts().get(i) + ".");
            System.out.print(name.getParts().get(num - 1) + "\t");
            return null;
        }

        @Override
        public Node visitSingleColumn(SingleColumn node, Context<C> context){
            rewrite(node.getExpression(), null);
            return null;
        }

        public void visitFrom(List<Relation> from){
            for(int i = 0 ; i < from.size() ; i ++){
                if(from.get(i) instanceof QueryBody){
                    System.err.println("nested query unsupported!");
                    return;
                }
                rewrite(from.get(i), null);
            }

        }
        public void visitWhere(Optional<Expression> where){
            rewrite(where.get(), null);
        }

        @Override
        public Node visitQuerySpecification(QuerySpecification query, Context<C> context){
            // TODO: add query visit code
            rewrite(query.getSelect(), null);
            printer.visitWhere(query.getWhere());
            printer.visitFrom(query.getFrom());
            return null;
        }
    }

    private void printSelect(){
        SelectItem item;
        System.out.println("Select:");
        for(int i = 0 ; i < selects.size() ; i ++){
            item = selects.get(i);
            rewrite(item, null);
        }
        System.out.println();
    }
    private void printWhere(){
        System.out.println("Where:");
        Expression exp;
        for(int i = 0 ; i < wheres.size() ; i ++){
            exp = wheres.get(i);
            if(exp instanceof ComparisonExpression){
                System.out.print("left: " + ((ComparisonExpression) exp).getLeft().toString() + "\t");
                System.out.print(((ComparisonExpression) exp).getType().getValue() + "\t");
                System.out.print("right: " + ((ComparisonExpression) exp).getRight().toString() + "\n");

            }
        }
    }

    private void printFrom(){
        System.out.println("from:");
        Iterator<Table> iter = froms.iterator();
        while(iter.hasNext()){
            System.out.print(iter.next().getName() + "\t");
        }
        System.out.println();
    }
    public void print(){
        printSelect();
        printFrom();
        printWhere();
    }
    public static class Context<C>
    {
        private final boolean defaultRewrite;
        private final C context;

        private Context(C context, boolean defaultRewrite)
        {
            this.context = context;
            this.defaultRewrite = defaultRewrite;
        }

        public C get()
        {
            return context;
        }

        public boolean isDefaultRewrite()
        {
            return defaultRewrite;
        }
    }
}
