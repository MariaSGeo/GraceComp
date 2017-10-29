package compiler;


import java.io.*;
import compiler.node.*;
import compiler.analysis.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;
import compiler.Library;

import compiler.Func;


public class TreeTraversalP1 extends DepthFirstAdapter {   
    
    
    HashMap<String,Func> vtable;
    Func curFunc;
    boolean decl_def; //true decl false def
    Library lib;
   
    public  TreeTraversalP1(Library lib){
        vtable = new HashMap<String,Func>();
        this.lib=lib;
        curFunc=null;
    }

    public HashMap<String,Func> getvTable(){return vtable;}

   
   
    @Override
    public void caseAProg(AProg node)
    {
        inAProg(node);
        if(node.getFuncDef() != null)
        {
            node.getFuncDef().apply(this);
        }

       for (Func func : vtable.values()) {
 
            if (!(func.getDefined())){
                System.out.println("Function "+func.getName() +" declared but not defined ");
                System.exit(-1);
            }

        }
        outAProg(node);
    }   

    @Override
    public void caseAHeader(AHeader node) 
    {
        inAHeader(node);
        if(node.getFuncId() != null)
        {
            node.getFuncId().apply(this);
        }
        {
            List<PParam> copy = new ArrayList<PParam>(node.getParams());
            for(PParam e : copy)
            {
                e.apply(this);
            }
        }
        if(node.getRetType() != null)
        {
            node.getRetType().apply(this);
        }
        outAHeader(node);
    }

    @Override
    public void caseADataTypeRetType(ADataTypeRetType node)
    {
        inADataTypeRetType(node);
        if(node.getDtype() != null)
        {
            node.getDtype().apply(this);
        }
        outADataTypeRetType(node);
    }

   

    @Override
    public void caseANothingRetType(ANothingRetType node)
    {
        inANothingRetType(node);
        if(node.getDtype() != null)
        {
            node.getDtype().apply(this);
        }
        outANothingRetType(node);
    }

   

    @Override
    public void caseAIntDataType(AIntDataType node)
    {
        inAIntDataType(node);
        if(node.getKeywordInt() != null)
        {
            node.getKeywordInt().apply(this);
        }
        outAIntDataType(node);
    }

   

    @Override
    public void caseACharDataType(ACharDataType node)
    {
        inACharDataType(node);
        if(node.getKeywordChar() != null)
        {
            node.getKeywordChar().apply(this);
        }
        outACharDataType(node);
    }

    
    @Override
    public void caseAParam(AParam node)
    {
        inAParam(node);
        List<TId> copy;
        LinkedList<String> paramlist = new LinkedList<String>();
        int flag=0;
        Func prvFunc=curFunc.getMother();

        if(node.getKeywordRef() != null)
        {
            node.getKeywordRef().apply(this);
        }
        {
            copy = new ArrayList<TId>(node.getParametersoftype());
            for(TId e : copy)
            {
                if(curFunc.hasParameter(e.toString())){
                    System.out.println("There are parameters with the same name "+e.toString()+ " " + curFunc.getName());
                    System.exit(-1);
                    flag=1;

                }else if(curFunc.hasChild(e.toString())||curFunc.getName().equals(e.toString())||((prvFunc!=null) && (prvFunc.getName().equals(e.toString()))))  {
                    System.out.println("There is a function with the same name in the current scope "+e.toString());
                    System.exit(-1);
                }else{
                    String str = node.getParamtype().toString();
                    LinkedList<Integer> dims=new LinkedList<Integer>();
                       
                    String[] split = str.split("\\s+");
                    for(int j=1;j<split.length;j++){
                        if(split[j].equals("[]"))
                            dims.add(null);                        
                        else
                            dims.add(Integer.valueOf(split[j])); 
                        
                        split[0]+=" []";
                    }
                    if((split.length>1) && (node.getKeywordRef() == null)){
                        System.out.println ("Array not passed by reference ");
                        System.exit(-1);

                    }
                    if(node.getKeywordRef() != null){
                        curFunc.newParam(e.toString(),split[0],true,dims);                   
                        paramlist.add(node.getParamtype().toString());
                    }else{
                        curFunc.newParam(e.toString(),split[0],false,dims);                   
                        paramlist.add(node.getParamtype().toString());
                    }
                }

                e.apply(this);
            }
        }
        if(flag==0)
            curFunc.newParamList(paramlist);
        if(node.getParamtype() != null)
        {
            node.getParamtype().apply(this);
        }
       
        outAParam(node);
    }

   

    @Override
    public void caseAFparType(AFparType node)
    {
        inAFparType(node);
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        if(node.getTokenEmptymatr() != null)
        {
            node.getTokenEmptymatr().apply(this);
        }
        {
            List<TLiteralNum> copy = new ArrayList<TLiteralNum>(node.getLiteralNum());
            for(TLiteralNum e : copy)
            {
                e.apply(this);
            }
        }
        outAFparType(node);
    }

  

    @Override
    public void caseAType(AType node)
    {
        inAType(node);
        if(node.getDataType() != null)
        {
            node.getDataType().apply(this);
        }
        {
            List<TLiteralNum> copy = new ArrayList<TLiteralNum>(node.getLiteralNum());
            for(TLiteralNum e : copy)
            {
                e.apply(this);
            }
        }
        outAType(node);
    }

   

    @Override
    public void caseAFuncDecl(AFuncDecl node)
    {

        inAFuncDecl(node);
        AHeader header= (AHeader) node.getHeader();

        if(lib.hasFunc(header.getFuncId().toString())){
            System.out.println("Library contains function named "+header.getFuncId().toString());
            System.exit(-1);
        }
        decl_def=true;
        Func prvFunc=curFunc;
        Func newfunction;
        if(curFunc==null){
            newfunction = new Func(header.getFuncId().toString(),header.getRetType().toString(),decl_def);
        }else{
            newfunction = new Func(header.getFuncId().toString(),header.getRetType().toString(),decl_def,curFunc);
        }
        curFunc=newfunction;
        if(prvFunc.hasChild(curFunc.getName())){
                System.out.println ("There is already a fucntion with that name in the current scope ( "+prvFunc.getName()+") : "+header.getFuncId().toString());
                System.exit(-1);

                return;
        }else{
                prvFunc.newChild(curFunc.getName(),curFunc);
        }

        if(node.getHeader() != null)
        {
            node.getHeader().apply(this);
        }
                vtable.put(curFunc.getName(),curFunc);

        curFunc=prvFunc;
        outAFuncDecl(node);
    } 

    @Override
    public void caseAVarDef(AVarDef node)
    {   
        Func prvFunc=curFunc.getMother();
        inAVarDef(node);
        {
            List<TId> copy = new ArrayList<TId>(node.getVariables());
            for(TId e : copy)
            {
                if(curFunc.hasParameter(e.toString())){
                    System.out.println("There is a parameter with the same name "+e.toString());
                    System.exit(-1);

                }
                else if(curFunc.hasVariable(e.toString())){
                    System.out.println("There is a variable with the same name "+e.toString());
                    System.exit(-1);

                }else if(curFunc.hasChild(e.toString())||curFunc.getName().equals(e.toString())||(prvFunc!=null && prvFunc.getName().equals(e.toString())))  {
                    System.out.println("There is a function with the same name in the current scope "+e.toString());
                    System.exit(-1);

                }else{
                    String str = node.getType().toString(); 
                    String[] split = str.split("\\s+");
                    LinkedList<Integer> dims=new LinkedList<Integer>();
                    for(int j=1;j<split.length;j++){
                       
                        dims.add(Integer.valueOf(split[j]));
                        //System.out.println(split[j]);

                        
                        split[0]+=" []";
                    }
                    curFunc.newVariable(e.toString(),split[0],dims);
                }
                e.apply(this);
            }
        }
        if(node.getType() != null)
        {
            node.getType().apply(this);
        }
        outAVarDef(node);
    }

    @Override
    public void caseAFuncDef(AFuncDef node)
    {

        inAFuncDef(node);
        Func newfunction;
        decl_def = false;
        boolean flag=false;
        Func prvFunc = curFunc;
        AHeader header= (AHeader) node.getHeader();

        if(lib.hasFunc(header.getFuncId().toString())){
            System.out.println("Library contains function named "+header.getFuncId().toString());
            System.exit(-1);
        }


        if(curFunc==null){
            List<PParam> args = new ArrayList<PParam>(header.getParams());
            if(args.size()!=0){
                System.out.println("Starting function can't have arguments " );
                    System.exit(-1);
            } 
            if(!(header.getRetType().toString().equals("nothing "))){
                System.out.println("Starting function must have return type : nothing " );
                System.exit(-1);
            }   
            newfunction = new Func(header.getFuncId().toString(),header.getRetType().toString(),decl_def);
            newfunction.setDepth(0);
            
        }else{
            newfunction = new Func(header.getFuncId().toString(),header.getRetType().toString(),decl_def,curFunc);
            newfunction.setDepth(curFunc.getDepth()+1);
        }

        curFunc = newfunction;
        if(prvFunc!=null){
            if((prvFunc.hasChild(header.getFuncId().toString()))    &&      (prvFunc.getChild(header.getFuncId().toString()).getDeclared())){
                
                if(node.getHeader() != null)
                    {   
                        node.getHeader().apply(this);
                    }
                Func declaredF = prvFunc.getChild(header.getFuncId().toString());
                int check =declaredF.compareHeaders(curFunc);
                if( check==1){
                    System.out.println("Declaration and definition return types do not match " );
                    System.exit(-1);
                }else if(check == 2){
                    System.out.println("Declaration and definition parameters do not match " );
                    System.exit(-1);
                }else if(check == 0){
                    //return;
                }
                //prvFunc.removeChild(header.getFuncId().toString());
                //prvFunc.newChild(curFunc.getName(),curFunc;
                    flag=true;
                //return;
            }

            if((prvFunc.hasChild(header.getFuncId().toString()))    &&      !(prvFunc.getChild(header.getFuncId().toString()).getDeclared())){
                System.out.println ("There is already a fucntion with that name in the current scope ( "+prvFunc.getName()+") : "+header.getFuncId().toString());
                System.exit(-1);
 
            }
            else{
                if(!flag)
                     if(node.getHeader() != null)
                    {   
                        node.getHeader().apply(this);
                    }
                prvFunc.removeChild(header.getFuncId().toString());
                prvFunc.newChild(curFunc.getName(),curFunc);
                curFunc.setDefined();
            }
        }

       

        {
            List<PLocalDef> copy = new ArrayList<PLocalDef>(node.getLocalDefList());
            for(PLocalDef e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getBlock());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        
      
        vtable.put(curFunc.getName(),curFunc);
        curFunc=prvFunc;

        outAFuncDef(node);
    }
}