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


public class Quad {  //quads for intermediate code
    
    String p1;
    String p2;
    String p3;
    String p4;

    int quadnum;

    
    public Quad(String str1,String str2,String str3,String str4,int q){
          p1=str1;
          p2=str2;
          p3=str3;
          p4=str4;
          quadnum=q;
    }

    public Quad(){
      p1="-";
      p2="-";
      p3="-";
      p4="-";
    }


    public void change(int place,String str){

      if(place==1)
        p1=str;
      if(place==2)
        p2=str;
      if(place==3)
        p3=str;
      if(place==4)
        p4=str;

    }

    public int getNum(){return quadnum;}

    public void patch(String str){p4=str;}

    public void printQuad(){System.out.println(quadnum +" : "+p1+","+p2+","+p3+","+p4);}

    public void printQuad2(){System.out.print(quadnum +" : "+p1+","+p2+","+p3+","+p4);}


    public String getfirst(){return p1;}

    public String getsecond(){return p2;}

    public String getthird(){return p3;}

    public String getfourth(){return p4;}



}