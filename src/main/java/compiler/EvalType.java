package compiler;


import java.io.*;

import java.util.*;


public class EvalType  {   
    
    String type;
    String name;//example : if it was created in a call expression then the name is "call";
   	boolean isConstant;
   	LinkedList<Integer> declareddims;//if it was declared as a matrix
   	int dims; // lval expr*
   	String valtype;


    public EvalType(String type,String name,boolean isConstant,LinkedList<Integer> dims,int numofdims){
       this.type = type;
       this.name=name;
       this.declareddims=dims;
       this.isConstant=isConstant;
       this.dims=numofdims;
       //this.valtype=type;

    }

    public String getTypeE(){return type;}

    public String getNameE(){return name;}

    public boolean isConstant(){return isConstant;}

    public LinkedList<Integer> getDims(){return declareddims;}

    public int getNumofdims(){return dims;}

}