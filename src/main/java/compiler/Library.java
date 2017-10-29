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
import compiler.EvalType;


public class Library {  //code provided for the Grace library  the assembly part is not completed


    HashMap<String,Func> lib;


    public Library(){

    	lib = new  HashMap<String,Func>();
		//fun puti (n : int) : nothing;

    	Func newfunc = new Func("puti ","nothing",false);
    	newfunc.newParam("n","int",false,null);
    	lib.put(newfunc.getName(),newfunc);

		//fun putc (c : char) : nothing;
    	newfunc = new Func("putc ","nothing",false);
    	newfunc.newParam("c","char",false,null);
    	lib.put(newfunc.getName(),newfunc);
    	
		//fun puts (ref s : char[]) : nothing;
		newfunc = new Func("puts ","nothing",false);
    	newfunc.newParam("c","char []",true,null);
    	//System.out.println(newfunc.getName());
    	lib.put(newfunc.getName(),newfunc);

		//fun strcpy (ref trg, src : char[]) : nothing;

    	newfunc = new Func("strcpy ","nothing",false);
    	newfunc.newParam("trg","char []",true,null);
     	newfunc.newParam("src","char []",true,null);
    	lib.put(newfunc.getName(),newfunc);

		//fun strcat (ref trg, src : char[]) : nothing;

    	newfunc = new Func("strcat ","nothing",false);
    	newfunc.newParam("trg","char []",true,null);
     	newfunc.newParam("src","char []",true,null);
    	lib.put(newfunc.getName(),newfunc);

		//fun gets (n : int, ref s : char[]) : nothing;
    	newfunc = new Func("gets ","nothing",false);
		  newfunc.newParam("n","int",false,null);
     	newfunc.newParam("src","char []",true,null);    	
     	lib.put(newfunc.getName(),newfunc);

		//fun geti () : int;
    	newfunc = new Func("geti ","int",false);
    	lib.put(newfunc.getName(),newfunc);

    	//fun getc () : char;
    	newfunc = new Func("getc ","char",false);
    	lib.put(newfunc.getName(),newfunc);
    	//fun abs (n : int) : int;
    	newfunc = new Func("abs ","int",false);
    	newfunc.newParam("n","int",false,null);
    	lib.put(newfunc.getName(),newfunc);

    	//fun ord (c : char) : int;
    	newfunc = new Func("ord ","int",false);
    	newfunc.newParam("c","char",false,null);
    	lib.put(newfunc.getName(),newfunc);

    	//fun chr (n : int) : char;
    	newfunc = new Func("chr ","char",false);
    	newfunc.newParam("n","int",false,null);
    	lib.put(newfunc.getName(),newfunc);

    	//fun strlen (ref s : char[]) : int;

		newfunc = new Func("strlen ","int",false);
    	newfunc.newParam("s","char []",true,null);
    	lib.put(newfunc.getName(),newfunc);

		//fun strcmp (ref s1, s2 : char[]) : int;

		newfunc = new Func("strcmp ","int",false);
    	newfunc.newParam("s1","char []",true,null);
     	newfunc.newParam("s2","char []",true,null);
    	lib.put(newfunc.getName(),newfunc);

    }

    public Func get(String name){return lib.get(name);}

    public boolean has(String name){return lib.containsKey(name);}


   public boolean compatibleTypes(String type1 , String type2){ // prototype call int [] [] [] [] 


   		String[] split1 = type1.split("\\s+");
        String[] split2 = type2.split("\\s+");
        split1[0]=split1[0].replaceAll("\\s","");
        split2[0]=split2[0].replaceAll("\\s","");
   		if(type1.equals("int")){
   			if(type2.equals("int"))
   				return true;
   			else if(split2[0].equals("int"))
   				return true;
   			else
   				return false;

   		}else if(type1.equals("char")){
   			if(type2.equals("char"))
   				return true;
   			else if(split2[0].equals("char"))
   				return true;
   			else if(split2[0].equals("String") && split2.length==2)
				return true;
   			else 
   				return false;


  	 	}else if(type1.equals("String")){
  	 		if(type2.equals("String"))
  	 			return true;
  	 		else if(split2[0].equals("char") && split2.length==2)
  	 			return true;
  	 		else
  	 			return false;



   		}else if (split1.length>1 && split1[0].equals("int")){
   			if(split1.length == split2.length && split2[0].equals("int"))
   				return true;
   			else 
   				return false;


   		}else if(split1.length>1 && split1[0].equals("char")){
   			if(split1.length == 2 && (type2.equals("String")))
   				return true;
   			else if(split1.length==split2.length && split2[0].equals("char"))
   				return true;
   			else 
   				return false;

   		}else
   			return false;
  	 	
	} 

  public boolean compatibleTypes(EvalType type1 , EvalType type2){

/*System.out.println("EvalType "+right.getTypeE());
            System.out.println("EvalType "+right.getNameE());
            System.out.println("EvalType "+right.isConstant());
            System.out.println("EvalType "+right.getDims().size());
            System.out.println("EvalType "+right.getNumofdims());*/

    String[] split1 = type1.getTypeE().split("\\s+");
    String[] split2 = type2.getTypeE().split("\\s+");
    split1[0]=split1[0].replaceAll("\\s","");
    split2[0]=split2[0].replaceAll("\\s","");

    if(split1[0].equals("char") && split2[0].equals("char"))
      return true;
            
    if(split1[0].equals("int") && split2[0].equals("int"))
      return true;

    if(split1[0].equals("String") && type1.getNumofdims()==1 && split2[0].equals("char")) 
      return true; 

    if(split1[0].equals("char") && type2.getNumofdims()==1 && split2[0].equals("String")) 
      return true; 


    if(split1[0].equals("String") && type1.getNumofdims()==1 && type2.getNumofdims()==1 && split2[0].equals("String")) 
      return true; 
    


    return false;











  }
  public boolean hasFunc(String name){return lib.containsKey(name);}

	public void libPrint(){
		  for (Func f : lib.values() ){
	   	    System.out.println(f.getName());
	   	    HashMap<String,Func> children = f.getChildren();
	   	    System.out.println("\t NUMBER OF CHILDREN "+children.size());
	   	    for(String chname:children.keySet())
               System.out.println("\t"+f.getName()+" ___ "+chname); 
           	HashMap<String,String> vars = f.getVars();	
           	System.out.println("\t NUMBER OF VARS "+vars.size());
           	for(Map.Entry<String, String> entry : vars.entrySet())
    			System.out.println("\t"+f.getName()+" ___ "+entry.getKey()+ " : "+entry.getValue());
          	HashMap<String,String> params = f.getParams();	
           	System.out.println("\t NUMBER OF PARAMS "+params.size());
           	for(Map.Entry<String, String> entry : params.entrySet())
    			System.out.println("\t"+f.getName()+" ___ "+entry.getKey()+ " : "+entry.getValue());

	   }


	}

  public LinkedList<String> generateAssembly(){

      LinkedList<String> libraryA=new LinkedList<String>();

      //puti
      libraryA.add("grace_puti:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("mov  eax, [ebp+8]");
      libraryA.add("call printf");
      


     // libraryA.add("endof(grace_puti): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
     // libraryA.add("name(grace_puti) endp");



      
      //putc
      libraryA.add("grace_putc:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("mov  eax, [ebp+8]");
      libraryA.add("call printf");
      



      //libraryA.add("endof(grace_putc): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_putc) endp");

      //puts

      libraryA.add("grace_puts:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("mov  eax, [ebp+8]");
      libraryA.add("call printf");
      



      //libraryA.add("endof(grace_puts): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_puts) endp");

      //geti
      libraryA.add("grace_geti:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("push formating_geti");//; arguments are right to left (first parameter)
      libraryA.add("call scanf");
      //libraryA.add("pop eax");
      libraryA.add("mov [ebp+12] ,eax");

      



      //libraryA.add("endof(grace_geti): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_geti) endp");

      //getc
      libraryA.add("grace_getc:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("push formating_geti");//; arguments are right to left (first parameter)
      libraryA.add("call scanf");
      //libraryA.add("pop eax");
      libraryA.add("mov [ebp+12] ,eax");
      



      //libraryA.add("endof(grace_getc): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_getc) endp");



      //gets
      libraryA.add("grace_gets:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");

      libraryA.add("mov eax, DWORD PTR stdin");
      libraryA.add("push eax");
      libraryA.add("mov eax, DWORD PTR [ebp + 8]");
      libraryA.add("push eax");
      libraryA.add("mov eax, DWORD PTR [ebp + 12]");
      libraryA.add("push eax");
      libraryA.add("call fgets");
      libraryA.add("add esp, 12");
      libraryA.add("mov eax, 10 # Carriage return");
      libraryA.add("push eax");
      libraryA.add("mov eax, DWORD PTR [ebp + 12]");
      libraryA.add("push eax");
      libraryA.add("call strchr");
      libraryA.add("add esp, 8");
      libraryA.add("cmp eax, 0");
      libraryA.add("je endof(grace_gets)");
      libraryA.add("mov BYTE PTR [eax], 0");
      
     // libraryA.add("endof(grace_gets): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
     // libraryA.add("name(grace_gets) endp");



      //abs

      libraryA.add("grace_abs:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("mov  ebx, [ebp+8]");
      libraryA.add("push ebx");
      libraryA.add("call abs");
      libraryA.add("mov [ebp+12] ,eax");


      



      //libraryA.add("endof(grace_abs): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_abs) endp");

      //ord

      libraryA.add("grace_ord:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      
      libraryA.add("mov  eax, [ebp+8]");

      libraryA.add("mov [ebp+12] ,eax");


      //libraryA.add("endof(grace_ord): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_ord) endp");

      //chr

      libraryA.add("grace_chr:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      libraryA.add("mov  eax, [ebp+8]");

      libraryA.add("mov [ebp+12] ,eax");

      



      //libraryA.add("endof(grace_chr): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_chr) endp");

      //strlen
      
      libraryA.add("grace_strlen:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      



      //libraryA.add("endof(grace_strlen): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_strlen) endp");


      //strcmp

      libraryA.add("grace_strcmp:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      



      //libraryA.add("endof(grace_strcmp): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_strcmp) endp");

      //strcpy

       libraryA.add("grace_strcpy:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      



      //libraryA.add("endof(grace_strcpy): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_strcpy) endp");

      //strcat

       libraryA.add("grace_strcat:");
      libraryA.add("push ebp");
      libraryA.add("mov ebp, esp");
      libraryA.add("sub esp, local_variables_size");
      



      //libraryA.add("endof(grace_strcat): mov sp, bp");
      libraryA.add("pop bp");
      libraryA.add("ret");
      //libraryA.add("name(grace_strcat) endp");

      return libraryA;



  }


}