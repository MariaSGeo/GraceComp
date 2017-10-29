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
import compiler.Func;


public class Temp {  //temp var 
    
    int regN;//number
    String regT;//type
    
    
    public Temp(int n,String str){
          regN=n;
          regT=str;
      
    }


    public int getN(){return regN;}

    public String getT(){return regT;}



}
