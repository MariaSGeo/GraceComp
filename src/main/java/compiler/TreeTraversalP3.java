package compiler;


import java.io.*;
import compiler.node.*;
import compiler.analysis.*;
import compiler.Temp;
import compiler.Library;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;


public class TreeTraversalP3 extends DepthFirstAdapter {   //for intermediate code generation
    
    HashMap<String,Func> vtable;
    Stack<String> tempregs;
    Stack<Temp> temps;
    List<Temp> regs; 
    Stack<Boolean> callexpr;
    LinkedList<String> atext;

    List<Quad> tempquads;
    List<Quad> quads;
    
    Stack<Quad> condtruequads;
    Stack<Quad> condfalsequads;

    Stack<Integer> condtrue;
    Stack<Integer> condfalse;

    int quadnumber;

    HashMap<String,String> hashText;

    Func curFunc;
    Library lib;

    int register;

    public TreeTraversalP3( HashMap<String,Func> vtable,Library lib){
        this.vtable=vtable;
        this.lib=lib;
        regs=new LinkedList<Temp>();
        tempregs= new Stack<String>();
        tempquads= new LinkedList<Quad>();
        quads=new LinkedList<Quad>();
        callexpr = new Stack<Boolean>();
        atext=new LinkedList<String>();

        quadnumber=0;
        register=0;

    }

    public void procFuncs(){

        for (Func f : vtable.values()) {

            f.sortVars();
            f.procParams();
            f.sortTemps();

        }
    }

    public Stack<Boolean> getCallStack(){return callexpr;}
    public Stack<String> getRegs(){return tempregs;}

    public LinkedList<String> getatext(){ //for the next step (assembly generation)
        hashText=new HashMap<String,String>();
        int counter=0;
        LinkedList<String> ntext=new LinkedList<String>();
        for(String str:atext){
            ntext.add("String"+counter+" : .asciz "+str);
            hashText.put(str,"String"+counter);
            counter++;
        }
        atext=ntext;

        return atext;


    }

    public HashMap<String,String> gethashtext(){return hashText;}
    public List<Quad> getQuads(){return quads;}

    public int nextQuad(){int quadnu=quadnumber+1; return quadnumber;}

    public Quad genQuad(String p1,String p2,String p3,String p4){

        Quad q= new Quad(p1,p2,p3,p4,quadnumber);
        quads.add(q);
        quadnumber++;
        return q;
 
    }

    public Temp newTemp(String type){
        Temp tmp = curFunc.newTemp(type);
        return tmp;  
    }

    public LinkedList<Quad> makeList(Quad x){
        LinkedList<Quad> q=new LinkedList<Quad>();
        q.add(x);
        return q;
    }

    public LinkedList<Integer> makeList(Integer x){
        LinkedList<Integer> q=new LinkedList<Integer>();
        q.add(x);
        return q;
    }

    public LinkedList<Quad> mergeL( LinkedList<LinkedList<Quad>> lists){

        LinkedList<Quad> q = new LinkedList<Quad>();
        for (LinkedList<Quad> list : lists)
            q.addAll(list);
        return q;

    }

    public LinkedList<Quad> EmptyListQ(){return new LinkedList<Quad>();}
    public LinkedList<Integer> EmptyListI(){return new LinkedList<Integer>();}


    public LinkedList<Quad> backPatch(LinkedList<Quad> quads,String p){
        LinkedList<Quad> nq = new LinkedList<Quad>();
        for (Quad q : quads){
            q.patch(p);
            nq.add(q);
        }
        return nq;
            
    }


    public HashMap<String,Func> getVtable(){return vtable;}

    @Override
    public void caseStart(Start node)
    {
        inStart(node);
        node.getPProg().apply(this);
        node.getEOF().apply(this);
        outStart(node);
    }

    public void inAProg(AProg node)
    {
        defaultIn(node);
    }

    public void outAProg(AProg node)
    {
        defaultOut(node);
    }

    @Override
    public void caseAProg(AProg node)
    {
        inAProg(node);
        if(node.getFuncDef() != null)
        {
            node.getFuncDef().apply(this);
        }
        outAProg(node);
    }


    @Override
    public void caseAAssignStmt(AAssignStmt node)
    {
        inAAssignStmt(node);
        if(node.getTo() != null)
        {
            node.getTo().apply(this);
        }
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Quad q=genQuad(":=",res1,"-",res2);
        outAAssignStmt(node);
    }

    

    @Override
    public void caseAIfStmt(AIfStmt node)
    {
        inAIfStmt(node);
        Quad q,fl;
        int size;
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        String cond=tempregs.pop();
        Integer j =nextQuad()+2;
        q=genQuad("ifb",cond,"-",j.toString());
        fl=genQuad("jump","-","-","*");

        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getThen());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        

            
        {         
            List<PStmt> copy = new ArrayList<PStmt>(node.getElse());
            size=copy.size();

            if(size>0){
               
                q=genQuad("jump","-","-","*");
            }
            j=nextQuad();
            fl.patch(j.toString());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        if(size>0){
            j=nextQuad();

            q.patch(j.toString());
        }

        outAIfStmt(node);
    }

    
    @Override
    public void caseAWhileStmt(AWhileStmt node)
    {
        inAWhileStmt(node);
        Quad q;
        Integer j;
        Integer condline=nextQuad();
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        String cond=tempregs.pop();
        j =nextQuad()+2;
        q=genQuad("ifb",cond,"-",j.toString());       
        q=genQuad("jump","-","-","*"); 

        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getBody());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        genQuad("jump","-","-",condline.toString());
        j=nextQuad();
        q.patch(j.toString());
        outAWhileStmt(node);
    }

    
    @Override
    public void caseAFuncCallStmt(AFuncCallStmt node)
    {
        inAFuncCallStmt(node);
        callexpr.push(false);

        if(node.getFuncCall() != null)
        {
            node.getFuncCall().apply(this);
        }
        outAFuncCallStmt(node);
    }

    @Override
    public void caseAReturnStmt(AReturnStmt node)
    {
        inAReturnStmt(node);
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
            genQuad("par", tempregs.pop(),"RET","-");
        }
        genQuad("ret","-","-","-");
        outAReturnStmt(node);
    }

    

    @Override
    public void caseAAddExpr(AAddExpr node)
    {
        inAAddExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("int");
        genQuad("+",res1,res2,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outAAddExpr(node);
    }

   

    @Override
    public void caseASubExpr(ASubExpr node)
    {
        inASubExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("int");
        genQuad("-",res1,res2,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outASubExpr(node);
    }

  

    @Override
    public void caseAMultExpr(AMultExpr node)
    {
        inAMultExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("int");
        genQuad("*",res1,res2,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outAMultExpr(node);
    }

    
    @Override
    public void caseADivExpr(ADivExpr node)
    {
        inADivExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("int");
        genQuad("/",res1,res2,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outADivExpr(node);
    }

    

    @Override
    public void caseAModExpr(AModExpr node)
    {
        inAModExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("int");
        genQuad("%",res1,res2,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outAModExpr(node);
    }

    

    @Override
    public void caseAFuncCallExpr(AFuncCallExpr node)
    {
        inAFuncCallExpr(node);
        callexpr.push(true);

        if(node.getFuncCall() != null)
        {
            node.getFuncCall().apply(this);
        }
        outAFuncCallExpr(node);
    }


    @Override
    public void caseALValueExpr(ALValueExpr node)
    {
        inALValueExpr(node);
        if(node.getLValue() != null)
        {
            node.getLValue().apply(this);
        }
        outALValueExpr(node);
    }


    @Override
    public void caseACharnumExpr(ACharnumExpr node)
    {
        inACharnumExpr(node);
        if(node.getLiteralChar() != null)
        {
            node.getLiteralChar().apply(this);
        }
        tempregs.push(node.getLiteralChar().toString());
        outACharnumExpr(node);
    }


    @Override
    public void caseANumberExpr(ANumberExpr node)
    {
        inANumberExpr(node);
        if(node.getLiteralNum() != null)
        {
            node.getLiteralNum().apply(this);
        }
        tempregs.push(node.getLiteralNum().toString());      
        outANumberExpr(node);
    }

    @Override
    public void caseANegativeExpr(ANegativeExpr node)
    {
        inANegativeExpr(node);
        if(node.getNegative() != null)
        {
            node.getNegative().apply(this);
        }
        outANegativeExpr(node);
    }

    
    @Override
    public void caseAAndCond(AAndCond node)
    {
       
        Integer i,jump;
        inAAndCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        
        String res1 = tempregs.pop();
        Quad first = genQuad("ifb",res1,"-","*");
        Quad second =genQuad("jump","-","-","*");
        jump=nextQuad();
        first.patch(jump.toString());

        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        
        String res2 = tempregs.pop(); 
        i=nextQuad();
        jump=i+2; //+1
        second.patch(jump.toString());
        Temp w = newTemp("boolean");
        tempregs.push("$"+w.getN());
        
        genQuad("&&",res2,res1,"$"+w.getN()); 
       
        outAAndCond(node);
    }

   
    @Override
    public void caseAOrCond(AOrCond node)
    {
        Integer i,jump;
        inAOrCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("boolean");
        genQuad("||",res2,res1,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outAOrCond(node);
    }

   
    @Override
    public void caseAExprCond(AExprCond node)
    {
        inAExprCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getOperation() != null)
        {
            node.getOperation().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        String res1 = tempregs.pop();
        String res2 = tempregs.pop();
        Temp w = newTemp("boolean");
        genQuad(node.getOperation().toString(),res2,res1,"$"+w.getN());
        tempregs.push("$"+w.getN());
        outAExprCond(node);
    }


    @Override
    public void caseANegativeCond(ANegativeCond node)
    {
        inANegativeCond(node);
        if(node.getNegative() != null)
        {
            node.getNegative().apply(this);
        }
        String res1 = tempregs.pop();
        Temp w = newTemp("boolean");
        genQuad("!",res1,"-","$"+w.getN());
        tempregs.push("$"+w.getN());        
        outANegativeCond(node);
    }


    @Override
    public void caseAIdLValue(AIdLValue node)
    {
        inAIdLValue(node);
        int size=0;
        Integer sum=0;
        Integer timessum=0;
        Temp w1,w2 ,w3; //w1 sum w2 multsum
        String type=curFunc.getFieldType1(node.getIdentifier().toString());//getfieldtype1
        if (type==null)
            type=curFunc.getInheritedValue(node.getIdentifier().toString());
        String[] split = type.split("\\s+");
        type=split[0];
        Quad q;
        if(node.getIdentifier() != null)
        {
            node.getIdentifier().apply(this);
        }

        {
            List<PExpr> copy = new ArrayList<PExpr>(node.getEpxrs());
            size=copy.size();
            for(PExpr e : copy)
            {
                e.apply(this);
            }
        }
        String [] dims=new String[size];
        Integer [] dimsN=new Integer[size];
       
            
        if(size>0){
            LinkedList<Integer> dimsNl = curFunc.getDims(node.getIdentifier().toString());
            if(dimsNl==null)
                dimsNl= curFunc.getInheritedDims(node.getIdentifier().toString());
            int counter=0;
            for(Integer i:dimsNl){
                dimsN[counter]=i;
                //System.out.print(i+" ");
                counter++;
            }
            //System.out.println(dimsNl.size());

            w1=newTemp("int");
            w2=newTemp("int");
            q=genQuad("+","$"+w2.getN(),"0","$"+w2.getN());
            for (int i=0;i<size;i++){

                dims[i]=tempregs.pop();


            }
            for (int i=0;i<size;i++){
                timessum=0;
                q=genQuad("+","$"+w1.getN(),"0","$"+w1.getN());

                for(int j=i+1;j<size;j++){
                    timessum=+dimsN[j];
                    q=genQuad("+","$"+w1.getN(),dimsN[j].toString(),"$"+w1.getN());

                }
                q=genQuad("*","$"+w1.getN(),dims[i],"$"+w1.getN());

                q=genQuad("+","$"+w2.getN(),"$"+w1.getN(),"$"+w2.getN());

            }
            if(type.equals("int")){
                q=genQuad("*","$"+w2.getN(),"4","$"+w2.getN());

            }
            w3=newTemp(type); 
          //  System.out.println("W3 TYPE "+type);
            q=genQuad("array",node.getIdentifier().toString(),"$"+w2.getN(),"$"+w3.getN());
            tempregs.push("[$"+w3.getN()+"]");
        }else{

            tempregs.push(node.getIdentifier().toString());
        }

      
       
        outAIdLValue(node);
    }

   
    @Override
    public void caseAStringLValue(AStringLValue node)
    {
        inAStringLValue(node);
        int size=0;
        int sum=0;
        int timessum=0;
        Temp w,w1;
        Quad q;
        if(node.getLiteralString() != null)
        {
            node.getLiteralString().apply(this);
        }
        String idn=node.getLiteralString().toString();
        {
            List<PExpr> copy = new ArrayList<PExpr>(node.getEpxrs());
            size=copy.size();
            for(PExpr e : copy)
            {
                e.apply(this);
            }
        }
        String [] dims=null;
        int [] dimsN=null;
        
        if(atext.isEmpty()){
            atext.add(node.getLiteralString().toString());

        }
        LinkedList<String> ntext =new LinkedList<String>(atext);
        boolean flag=false;
        for(String str : ntext){
            if((node.getLiteralString().toString().equals(str))){
                 flag=true;
                 break;           
            }
        }
        if(!flag){
            atext.add(node.getLiteralString().toString());
        }
        if(size>0){
            w1= newTemp("char");//String[i]
            w=newTemp("int");
            String index=tempregs.pop();

            q=genQuad("*","4",index,"$"+w.getN());
            q=genQuad("array",node.getLiteralString().toString(),"$"+w.getN(),"$"+w1.getN());          
            tempregs.push("[$"+w1.getN()+"]");

        }else{

            tempregs.push(node.getLiteralString().toString());
        }
        outAStringLValue(node);
    }


    @Override
    public void caseAFuncCall(AFuncCall node)
    {
        Quad q;
        inAFuncCall(node);
        if(node.getFuncId() != null)
        {
            node.getFuncId().apply(this);
        }
       

        {
            List<PExpr> copy = new ArrayList<PExpr>(node.getArgs());
            for(PExpr e : copy)
            {
                e.apply(this);
            }
        }

        
        Func compareFunc=curFunc.getAncFunc(node.getFuncId().toString());
        
        if(compareFunc==null)
            compareFunc=curFunc.getFuncSameScope(node.getFuncId().toString());
        if(compareFunc==null)
          if(curFunc.hasChild(node.getFuncId().toString()))
            compareFunc=curFunc.getChild(node.getFuncId().toString());
        if(compareFunc==null)
            compareFunc=lib.get(node.getFuncId().toString());  
        if(compareFunc==null)
            compareFunc=curFunc;
        

        List<PExpr> copy = new ArrayList<PExpr>(node.getArgs());        
        int nargs= copy.size();
        LinkedList<String> exprs =new LinkedList<String>();
        for(int i=0 ;i<nargs;i++){
            String k= tempregs.pop();
            exprs.add(k);
        }
        //Collections.reverse(exprs);
        Iterator<String> exprsit=exprs.listIterator();
        boolean flag=false;
        LinkedList<Integer> reft=compareFunc.getRefptypes();
        //Collections.reverse(reft);
        for(int i=nargs-1 ;i>=0;i--){
            String k = exprsit.next();
            flag = false;
            for(Integer j:reft){
                if((i+1)==j){
                    q=genQuad("par",k,"R","-");
                    flag=true;
                }
            }
            if(!flag) {  
                q=genQuad("par",k,"V","-");
            }
        }
        q=genQuad("call","-","-",node.getFuncId().toString());
        if(callexpr.pop()){
            String ret=compareFunc.getRetType();
            Temp w=newTemp(ret);
            //genQuad(":=","$$"+node.getFuncId().toString(),"-","$"+w.getN());
            tempregs.push("$"+w.getN());
        }
        outAFuncCall(node);
    }

   

    @Override
    public void caseAFuncDef(AFuncDef node)
    {
        inAFuncDef(node);
        Func prvFunc = curFunc;
        AHeader header= (AHeader) node.getHeader();
        if (curFunc==null){

            curFunc = vtable.get(header.getFuncId().toString());
        }else{
            curFunc = vtable.get(header.getFuncId().toString());

            if(curFunc==null){
                System.out.println("NULL from  mother "+prvFunc.getName()+ " to child "+ header.getFuncId().toString());
            }
        }
        //Quad q=genQuad("unit",curFunc.getName(),"-","-");
        if(node.getHeader() != null)
        {
            node.getHeader().apply(this);
        }
        {
            List<PLocalDef> copy = new ArrayList<PLocalDef>(node.getLocalDefList());
            for(PLocalDef e : copy)
            {
                e.apply(this);
            }
        }
        Quad q=genQuad("unit",curFunc.getName(),"-","-");
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getBlock());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        q=genQuad("endu",curFunc.getName(),"-","-");

        curFunc=prvFunc;

        outAFuncDef(node);
    }

    

   
}