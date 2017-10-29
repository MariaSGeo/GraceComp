package compiler;

import java.io.*;
import compiler.node.*;
import compiler.analysis.*;
import compiler.Library;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;
import java.io.File;

public class QuadstoA { //quads to assembly


	List<Quad> quads;
	HashMap<String,Func> vtable;
	Iterator<Quad> quadit;
    PrintStream assembly;
    int labelnum;
    int depthcall;
    int curFuncdepth;
    Func curFunc;
    Func arFunc;
    Func prvFunc;
    Library lib;
    Stack<Integer> offsets; 
    Stack<Func> calls;
    LinkedList<String> text;
    HashMap<String,String> htext;

	public QuadstoA(List<Quad> q,HashMap<String,Func> vtable,String gracepath,Library lib,LinkedList<String> text,HashMap<String,String> htext) { 
		try{
          
            String gracefile="";
            File f = new File(gracepath);
            gracefile= f.getName();
           
            assembly=new PrintStream("./src/assembly/"+gracefile.split("[.]")[0]+".asm");     
		}
        catch(FileNotFoundException e){

            System.err.println(e.getMessage());
            System.exit(-1);

        }
        this.lib=lib;
        this.text=text;
        offsets=new Stack<Integer>();
        calls=new Stack<Func>();
        depthcall=0;
        curFuncdepth=0;
        curFunc=null;
        prvFunc=null;
        quads=q;
        quadit=quads.listIterator();
        labelnum=0;
        this.htext=htext;
	 	this.quads=q;
	 	this.vtable=vtable;
	}

	private void emit(String instruction){assembly.println(instruction);}


    private String newLabel(){ // new assembly label

        String newLabel=new String("Label"+labelnum);
        labelnum++;
        return newLabel;

    }

    private void getAR(String a){//get where the var/param is local


		/*mov esi, dword ptr [ebp+4]
			mov esi, dword ptr [esi+4]
			mov esi, dword ptr [esi+4]
				...
			mov esi, dword ptr [esi+4]*/
    	
    	emit("	mov esi, dword ptr [ebp+8]");//for the 1st
    	if(curFunc.getMother().hasField(a))
    		return;
    	Func nf=curFunc.getMother();
    	while(!nf.hasField(a)){ //for all the next
    		emit("	mov esi, dword ptr [esi+8]");
    		nf=nf.getMother();
    	}

    	
    }

    private void load(String r,String a,boolean ref){
    	try{
    		Integer.parseInt(a.replaceAll("\\s",""));
    		emit("	load "+r+","+a);
    		return;
    	}catch(Exception e ) {
			char[] cArray = a.toCharArray();
    		if(cArray[0]=='\''){
				emit("	load "+r+",ASCII("+a.replaceAll("\\s","")+")");
				return;
			}
			if(cArray[0]=='\"'){
				emit("	lea "+r+",byte ptr "+htext.get(a));
			
				return;
			}

			String type = curFunc.getParameterType(a);
			if(type!=null){ //local parameter
				Integer offset=curFunc.getOffset(a)+16;

				if(ref){
					emit("	mov esi, dword ptr [ebp + " +offset+"]");			
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi]");
				}else{

					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [ebp + " +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [ebp + " +offset+"]");
				}
				return;
			}
			if((type=curFunc.getTmpType(a))!=null){ //local temp

				Integer offset=curFunc.getOffset(a)+4;

				if(ref){
					emit("	mov esi, dword ptr [ebp - " +offset+"]");			
					if(type.equals("int")||type.equals("boolean"))
						emit("	mov"+r+", dword ptr [esi]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi]");
				}else{

					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [ebp - " +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [ebp - " +offset+"]");
				}
				return;
			}
			if((type=curFunc.getVariableType(a))!=null){//local variable
				Integer offset=curFunc.getOffset(a)+4;

				if(ref){// local with ref
					emit("	mov esi, dword ptr [ebp - " +offset+"]");
					
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi]");
				}else{ 
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [ebp - " +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [ebp - " +offset+"]");
				}
				return;
			}
			if((type=curFunc.getInheritedPar(a))!=null){//not local parameter
				Integer offset=curFunc.getInhOffset(a)+16;

				if(ref){
					getAR(a);
					emit("	mov esi, dword ptr [esi + " +offset+"]");
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi]");


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi + " +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi + " +offset+"]");

				}

				return;
			}
			if((type=curFunc.getInheritedVar(a))!=null){//not local variable
								Integer offset=curFunc.getInhOffset(a)+4;

				if(ref){
					getAR(a);
					emit("	mov esi, dword ptr [esi - " +offset+"]");
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi]");


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi - " +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi - " +offset+"]");


				}

				return;
			}


    	}

    }
    private void loadAddr(String r,String a,boolean ref){

    	char[] cArray = a.toCharArray();
    	if(cArray[0]=='\"'){
				emit("	lea "+r+",byte ptr "+htext.get(a));
				return;
		}

			String type = curFunc.getParameterType(a);
			if(type!=null){ //local parameter
				Integer offset=curFunc.getOffset(a)+16;

				if(ref){
					emit("	mov "+r+", dword ptr [ebp + " +offset+"]");
					
				}else{
					if(type.equals("int"))
						emit("	lea"+r+", dword ptr [ebp + " +offset+"]");
					if(type.equals("char"))
						emit("	lea"+r+", byte ptr [ebp +" +offset+"]");
				}
				return;
			}
			if((type=curFunc.getVariableType(a))!=null){//local variable
				Integer offset=curFunc.getOffset(a)+4;

				if(ref){// local with ref
					emit("	mov "+r+", dword ptr [ebp - " +offset+"]");

				}else{ 
					if(type.equals("int"))
						emit("	lea"+r+", dword ptr [ebp - " +offset+"]");
					if(type.equals("char"))
						emit("	lea"+r+", byte ptr [ebp - " +offset+"]");
				}
				return;
			}

			if((type=curFunc.getTmpType(a))!=null){//local temp

				Integer offset=curFunc.getOffset(a)+4;

				if(ref){// local with ref
					emit("	mov "+r+", dword ptr [ebp - " +offset+"]");

				}else{ 
					if(type.equals("int")||type.equals("boolean"))
						emit("	lea"+r+", dword ptr [ebp - " +offset+"]");
					if(type.equals("char"))
						emit("	lea"+r+", byte ptr [ebp - " +offset+"]");
				}
				return;
			}
			if((type=curFunc.getInheritedPar(a))!=null){//not local parameter
				Integer offset=curFunc.getInhOffset(a)+16;

				if(ref){
					getAR(a);
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi+" +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi+" +offset+"]");


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	lea"+r+", dword ptr [esi + " +offset+"]");
					if(type.equals("char"))
						emit("	lea"+r+", byte ptr [esi + " +offset+"]");

				}

				return;
			}
			if((type=curFunc.getInheritedVar(a))!=null){//not local variable
				Integer offset=curFunc.getInhOffset(a)+4;
				
				if(ref){
					getAR(a);
					if(type.equals("int"))
						emit("	mov"+r+", dword ptr [esi-" +offset+"]");
					if(type.equals("char"))
						emit("	mov"+r+", byte ptr [esi-" +offset+"]");


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	lea"+r+", dword ptr [esi - " +offset+"]");
					if(type.equals("char"))
						emit("	lea"+r+", byte ptr [esi - " +offset+"]");


				}

				return;
			}


    }
    private void store(String r,String a,boolean ref){
    	char[] cArray = a.toCharArray();


			String type = curFunc.getParameterType(a);
			if(type!=null){ //local parameter
			Integer offset=curFunc.getOffset(a)+16;

				if(ref){
					emit("	mov esi, dword ptr [ebp+" +offset+"]");
					if(type.equals("int"))
						emit("	move dword ptr [esi],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [esi],"+r);
					
				}else{
					if(type.equals("int"))
						emit("	move dword ptr [ebp + " +offset+"],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [ebp + " +offset+"],"+r);
				}
				return;
			}
			if((type=curFunc.getVariableType(a))!=null){//local variable
				Integer offset=curFunc.getOffset(a)+4;

				if(ref){
					emit("	mov esi, dword ptr [ebp-" +offset+"]");
					if(type.equals("int"))
						emit("	move dword ptr [esi],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [esi],"+r);
					
				}else{
					if(type.equals("int"))
						emit("	move dword ptr [ebp - " +offset+"],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [ebp - " +offset+"],"+r);
				}
				return;
			}
			if((type=curFunc.getTmpType(a))!=null){//local tmp
				Integer offset=curFunc.getOffset(a)+4;

				if(ref){
					emit("	mov esi, dword ptr [ebp-" +offset+"]");
					if(type.equals("int")||type.equals("boolean"))
						emit("	move dword ptr [esi],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [esi],"+r);
					
				}else{
					if(type.equals("int"))
						emit("	move dword ptr [ebp - " +offset+"],"+r);
					if(type.equals("char"))
						emit("	move byte ptr [ebp - " +offset+"],"+r);
				}
				return;
			}
			if((type=curFunc.getInheritedPar(a))!=null){//not local parameter
				Integer offset=curFunc.getInhOffset(a)+16;
				
				if(ref){
					getAR(a);
					emit("	mov esi, dword ptr [esi+" +offset+"]");
					if(type.equals("int"))
						emit("	mov dword ptr [esi],"+r);
					if(type.equals("char"))
						emit("	mov byte ptr [esi],"+r);


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	mov dword ptr [esi + " +offset+"],"+r);
					if(type.equals("char"))
						emit("	mov byte ptr [esi + " +offset+"],"+r);

				}

				return;
			}
			if((type=curFunc.getInheritedVar(a))!=null){//not local variable
				Integer offset=curFunc.getInhOffset(a)+4;

				if(ref){
					getAR(a);
					emit("	mov esi, dword ptr [esi-" +offset+"]");
					if(type.equals("int"))
						emit("	mov dword ptr [esi],"+r);
					if(type.equals("char"))
						emit("	mov byte ptr [esi],"+r);


				}else{
					getAR(a);
					if(type.equals("int"))
						emit("	mov dword ptr [esi - " +offset+"],"+r);
					if(type.equals("char"))
						emit("	mov byte ptr [esi -" +offset+"],"+r);

				}

				return;
			}


    }
    private void updateAL(String calledfunc){
	
	int p=curFunc.getDepth();
	if(lib.has(calledfunc))
		return;
	//if(vtable.get(calledfunc) == null)
	//	System.out.println("HERE");

	//if(curFunc.getFuncSameScope(calledfunc)==null)

	int x=vtable.get(calledfunc).getDepth();
    	if(p<x){

    		emit("	push ebp");
    	}else if(p==x){

    		emit("	push dword ptr [ebp+8]");
    	}else{//p-x+1
    		int res=p-x+1;
    		for(int i=0;i<res;i++){
    			emit("	mov esi, dword ptr [ebp+8]");
    		}
    		emit("	push dword ptr [esi+8]");
    	}
		//if np < nx push ebp

		//np = nx push dword ptr [ebp+4]

		/*np > nx  mov esi, dword ptr [ebp+4]
		mov esi, dword ptr [esi+4]
		...
		mov esi, dword ptr [esi+4]
		push dword ptr [esi+4]*/
    }


    private Quad getnextQuad(){return quadit.next();}

    public void quadProc(){
    	emit("intel_syntax noprefix");
		emit(".text");
		emit(".global _start");
		emit("start:");
		emit("	push ebp");
		emit(" 	mov ebp, esp");
		Quad q=quads.get(quads.size()-1);
		emit("	call "+q.getsecond());
		//calls.push(vtable.get(q.getsecond()));
		emit("	mov eax, 0");
		emit("	mov esp,ebp");
		emit("	pop ebp");
		emit("	ret");

    	while(quadit.hasNext()){
	    	Quad quad=getnextQuad();
	    	String typeofQuad=quad.getfirst();
	    	emit("#"+quad.getNum()+" : "+quad.getfirst()+","+quad.getsecond()+","+quad.getthird()+","+quad.getfourth());
	    	if(typeofQuad.equals("unit")){
	    		prvFunc=curFunc;
	    		curFunc=vtable.get(quad.getsecond());

	    		emit("grace_"+quad.getsecond()+":");
	    		emit(quad.getNum()+":");
				emit("	push ebp");
				emit("	mov ebp, esp");
				int s=curFunc.getFuncSize();
				emit("	sub esp,"+s); // size for params and local vars


	    	}else if(typeofQuad.equals("endu")){
	    		emit("endgrace_"+quad.getsecond()+":");
				emit(quad.getNum()+":");

				emit(" add esp ,"+curFunc.getFuncSize());
				emit("	pop ebp");
				emit("	ret");
				//emit("name("+quad.getsecond()+") endp");


	    	}else if(typeofQuad.equals("ret")){
	    		emit(quad.getNum()+":");

	    		emit("	jmp endgrace_"+curFunc.getName());
	    		//curFunc=prvFunc;
	    		//calls.pop();
	    		depthcall--;

	    	}else if(typeofQuad.equals("call")){
	    		if(lib.has(quad.getfourth()))
				continue;
				//calls.push(vtable.get(q.getfourth()));
	    		emit(quad.getNum()+":");
	    		emit("	sub esp, 4");
				updateAL(quad.getfourth());
				emit("	call "+quad.getfourth());
				
				int s =vtable.get(quad.getfourth()).getParamssize();
				s+=8;
				emit("	add esp,"+s);//size of the called func
				depthcall++;

	    	}else if(typeofQuad.equals(":=")){
	    		emit(quad.getNum()+":");
	    		load("eax",quad.getsecond(),false);
				store("edx", quad.getfirst(),false);

	    	}else if(typeofQuad.equals("array")){
	    		//array, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax", quad.getthird(),false);
				emit("	mov ecx, size");
				emit("	imul ecx");
				loadAddr("ecx", quad.getsecond(),false);
				emit("	add eax, ecx");
				store("eax", quad.getfourth(),false);

	    	}else if(typeofQuad.equals("+")){

	    		//+, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax", quad.getsecond(),false);
				load("edx", quad.getthird(),false);
				emit("	add eax, edx"); 
				store("eax", quad.getthird(),false);

	    	}else if(typeofQuad.equals("-")){

	    		// -, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax", quad.getsecond(),false);
				load("edx", quad.getthird(),false);
				emit("	sub eax, edx"); 
				store("eax", quad.getthird(),false);
	    	}else if(typeofQuad.equals("*")){
	    		//*, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax",quad.getsecond(),false);
				load("ecx", quad.getthird(),false);
				emit("	imul ecx");
				store("eax", quad.getfourth(),false);

	    	}else if(typeofQuad.equals("/")){
				// /, x, y, z,
				emit(quad.getNum()+":");
	    		load("eax", quad.getsecond(),false);
				emit("	cwd");
				load("ecx", quad.getthird(),false);
				emit("	idiv ecx");
				store("eax", quad.getfourth(),false); 
	    	}else if(typeofQuad.equals("%")){
	    		//  %, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax", quad.getsecond(),false);
				emit("	cwd");
				load("ecx", quad.getthird(),false);
				emit("	idiv ecx");
				store("edx", quad.getfourth(),false);
	    	}else if(typeofQuad.equals("<")|| typeofQuad.equals(">")||typeofQuad.equals("=")){
	    		// =, x, y, z, <, x, y, z, >, x, y, z
	    		emit(quad.getNum()+":");
	    		load("eax", quad.getsecond(),false);
				load("edx", quad.getthird(),false);
				emit("	cmp eax, edx");

	    	}else if(typeofQuad.equals("ifb")){
				//ifb, x, -, z
				emit(quad.getNum()+":");
	    		load("al", quad.getsecond(),false);
				emit("	or al, al");
				emit("	jnz label("+quad.getfourth()+")");
	    	}else if(typeofQuad.equals("jump")){

	    		//jump, -, -, z, 
	    		emit(quad.getNum()+":");
	    		emit("	jmp label("+quad.getfourth()+")");
	    	}else if(typeofQuad.equals("par")){
	    		//par, x, V, 
	    		emit(quad.getNum()+":");
	    		if(quad.getthird().equals("V")){
	    			load("eax",quad.getsecond(),false);
					emit("	push eax");
	    		}else{
	    		//par, x, R, -, par, x, RET, -
	    			loadAddr("esi",quad.getsecond(),true);
					emit("	push esi");
	    		}

	    	}


	    }

	    Iterator<String> iterator = text.iterator();
	    if(text.size()>0){
	    	emit(".text");
	    }
	    while(iterator.hasNext()){
	    	emit(iterator.next());

	    }

    }


}
