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


public class TreeTraversalP2 extends DepthFirstAdapter {   
    
    HashMap<String,Func> vtable;

    Func curFunc;
    Stack<EvalType> typeStack; 
    boolean retCheck;
    Stack<Boolean> callexpr;
    Stack<Boolean> lvalexpr;
    Library lib;
    Stack<Boolean> l_r_vals;//true lval , false rval

    public TreeTraversalP2( HashMap<String,Func> vtable,Library lib){
        this.vtable=vtable;
        typeStack = new  Stack<EvalType>(); 
        retCheck=false;
        callexpr = new Stack<Boolean>();
        lvalexpr=new Stack<Boolean>();
        l_r_vals = new Stack<Boolean>();
        this.lib=lib;

    }
    public Stack<EvalType> getStack(){return typeStack;}
    public Stack<Boolean> getCallStack(){return callexpr;}
    public Stack<Boolean> getLvalStack(){return lvalexpr;}

    public HashMap<String,Func> getVtable(){return vtable;}


   


    @Override
    public void caseAAssignStmt(AAssignStmt node)
    {


        
        inAAssignStmt(node);
        lvalexpr.push(true);
        //l_r_vals.push(true);
        EvalType left,right;


        if(node.getTo() != null)
        {
            node.getTo().apply(this);
        }
        left=typeStack.pop();


        //l_r_vals.push(false);

        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
        }

        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , assign operation , ( array as lvalue not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        right=typeStack.pop();
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , assign operation ,( array as rvalue not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        l_r_vals.pop();
        l_r_vals.pop();
        boolean result = lib.compatibleTypes(left,right);      
        if(!result){
            //System.out.println("EvalType "+left.getTypeE()+" "+left.getNameE()+" "+left.isConstant()+" "+left.getDims().size()+" "+left.getNumofdims());
           /* System.out.println("EvalType "+right.getTypeE());
            System.out.println("EvalType "+right.getNameE());
            System.out.println("EvalType "+right.isConstant());
            System.out.println("EvalType "+right.getDims().size());
            System.out.println("EvalType "+right.getNumofdims());*/
            String[] split1 = left.getTypeE().split("\\s+");
            String[] split2 = right.getTypeE().split("\\s+");
            split1[0]=split1[0].replaceAll("\\s","");
            split2[0]=split2[0].replaceAll("\\s","");
            System.out.println("Incompatible types, assign operation "+split1[0]+ " "+split2[0] + " "+curFunc.getName());
            System.exit(-1);
        }
        outAAssignStmt(node);
    }

   
    @Override
    public void caseAIfStmt(AIfStmt node)
    {
        inAIfStmt(node);
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        EvalType cond=typeStack.pop();
        if(!(cond.getTypeE().equals("boolean"))){
            System.out.println("Not boolean (condition) "+curFunc.getName());
            System.exit(-1);
 
        }
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getThen());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getElse());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        outAIfStmt(node);
    }

   

    @Override
    public void caseAWhileStmt(AWhileStmt node)
    {
        inAWhileStmt(node);
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        EvalType cond=typeStack.pop();
        if(!(cond.getTypeE().equals("boolean"))){
            System.out.println("Not boolean (condition)"+ cond.getTypeE() + " "+curFunc.getName());
            System.exit(-1);

        }
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getBody());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
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
        EvalType ret=null;
        if(node.getExpr() != null)
        {
            node.getExpr().apply(this);
            l_r_vals.pop();
            ret = typeStack.pop();
            retCheck=true;


        }
        

        String curf = curFunc.getRetType().replaceAll("\\s","");
        if(ret!=null)
            if(!(curf.equals(ret.getTypeE()))){
           
                System.out.println("Function "+ curFunc.getName() +"has return type "+curFunc.getRetType()+ " instead "+ret.getTypeE()+" was found");
                System.exit(-1);
            
            }
        outAReturnStmt(node);
    }

  

    @Override
    public void caseAAddExpr(AAddExpr node)
    {
        int line=1;
        Node n=(Node) (PExpr) node;
        inAAddExpr(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        l_r_vals.pop();
        l_r_vals.pop();
        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
         leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");

        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , addexpr operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , addexpr operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        if(!(leftType.equals("int")) || !(rightType.equals("int"))){
            System.out.println("Not valid type, add operation (int required)"+curFunc.getName());
            System.exit(-1);           
        }

        else if(!(leftType.equals(rightType))){
            System.out.println("Incompatible types, add operation "+curFunc.getName());
            System.exit(-1);
        }
        else {
            l_r_vals.push(true);
            typeStack.push(new EvalType("int","addexpr",false,null,0));
        }

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
        l_r_vals.pop();
        l_r_vals.pop();

        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
         leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");

        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , subexpr operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , subexpr operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        if(!(leftType.equals("int")) || !(rightType.equals("int"))){
            System.out.println("Not valid type,sub operation (int required)"+curFunc.getName());
            System.exit(-1);
        }

        else if(!(leftType.equals(rightType))){
            System.out.println("Incompatible types, sub operation "+curFunc.getName());
            System.exit(-1);
        }
        else {
            l_r_vals.push(true);

            typeStack.push(new EvalType("int","subexpr",false,null,0));

        }

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
        l_r_vals.pop();
        l_r_vals.pop();

        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
        leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");

        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , multexpr operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , multexpr operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        if(!(leftType.equals("int")) || !(rightType.equals("int"))){
            System.out.println("Not valid type, mult operation "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(leftType.equals(rightType))){
            System.out.println("Incompatible types, mult operation "+curFunc.getName());
            System.exit(-1);
        }
        else {
            l_r_vals.push(true);

            typeStack.push(new EvalType("int","multexpr",false,null,0));

        }

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
        l_r_vals.pop();

        l_r_vals.pop();

        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
        leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");

        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , divexpr operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , divexpr operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        if(!(leftType.equals("int")) || !(rightType.equals("int"))){
            System.out.println("Not valid type, div operation "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(leftType.equals(rightType))){
            System.out.println("Incompatible types, div operation "+curFunc.getName());
            System.exit(-1);
        }
        else {
            l_r_vals.push(true);

            typeStack.push(new EvalType("int","divexpr",false,null,0));

        }

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
        l_r_vals.pop();

        l_r_vals.pop();

         EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
         leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");

         if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , modexpr operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , modexpr operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }
        if(!(leftType.equals("int")) || !(rightType.equals("int"))){
            System.out.println("Not valid type, mod operation "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(leftType.equals(rightType))){
            System.out.println("Incompatible types, mod operation "+curFunc.getName());
            System.exit(-1);
        }
        else{ 
            l_r_vals.push(true);

            typeStack.push(new EvalType("int","modexpr",false,null,0));

        }

        outAModExpr(node);
    }


    @Override
    public void caseAFuncCallExpr(AFuncCallExpr node)
    {
        inAFuncCallExpr(node);
        callexpr.push(true);
        l_r_vals.push(true);
        if(node.getFuncCall() != null)
        {
            node.getFuncCall().apply(this);
        }
        //callexpr=false;
        outAFuncCallExpr(node);
    }


    @Override
    public void caseALValueExpr(ALValueExpr node)
    {
        inALValueExpr(node);
        //l_r_vals.push(true);
        lvalexpr.push(true);
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
        l_r_vals.push(false);
        if(node.getLiteralChar() != null)
        {
            typeStack.push(new EvalType("char","charconst",true,null,0));

            node.getLiteralChar().apply(this);
        }
        outACharnumExpr(node);
    }

   

    @Override
    public void caseANumberExpr(ANumberExpr node)
    {
        inANumberExpr(node);
        if(node.getLiteralNum() != null)
        {
            l_r_vals.push(false);
            EvalType y=new EvalType("int","intconst",true,null,0);
                typeStack.push(y);

            node.getLiteralNum().apply(this);
        }
        outANumberExpr(node);
    }

    @Override
    public void caseANegativeExpr(ANegativeExpr node)
    {
        inANegativeExpr(node);
        l_r_vals.pop();
        if(node.getNegative() != null)
        {
            node.getNegative().apply(this);
        }
        EvalType n;
        n=typeStack.pop();
        if(!(n.getTypeE().equals("int"))){
            System.out.println("Not valid type, negative "+curFunc.getName());
            System.exit(-1);
        }
        else{
            l_r_vals.push(true);
            typeStack.push(new EvalType("int","negexpr",n.isConstant(),null,0));

    }
        outANegativeExpr(node);
    }

   

    @Override
    public void caseAAndCond(AAndCond node)
    {
        inAAndCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        if(!(left.getTypeE().equals("boolean")) || !(right.getTypeE().equals("boolean"))){
            System.out.println("Not valid type, cond and  operation "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(left.getTypeE().equals(right.getTypeE()))){
            System.out.println("Incompatible types, cond and operation "+curFunc.getName());
            System.exit(-1);
        }
        else{ 
            typeStack.push(new EvalType("boolean","cond",true,null,0));

        }

        outAAndCond(node);
    }

    
    @Override
    public void caseAOrCond(AOrCond node)
    {
        inAOrCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        if(!(left.getTypeE().equals("boolean")) || !(right.getTypeE().equals("boolean"))){
            System.out.println("Not valid type, cond or  operation "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(left.getTypeE().equals(right.getTypeE()))){
            System.out.println("Incompatible types, cond and operation "+curFunc.getName());
            System.exit(-1);
        }
        else{ 
            typeStack.push(new EvalType("boolean","cond",true,null,0));
        }            
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
        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        l_r_vals.pop();
        l_r_vals.pop();

        EvalType left,right;
        left=typeStack.pop();
        right=typeStack.pop();
        String leftType  = left.getTypeE().replace("[]", ""); 
        String rightType = right.getTypeE().replace("[]", "");
        leftType =leftType.replaceAll("\\s","");
        rightType =rightType.replaceAll("\\s","");
        if(left.getDims()!=null){
            if(left.getDims().size()!=left.getNumofdims()){
                System.out.println("Not compatible type , exprcond operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
        if(right.getDims()!=null){
            if(right.getDims().size()!=right.getNumofdims()){
                System.out.println("Not compatible type , exprcond operation ,( array  not supported || less or more dims given ");
                System.exit(-1);
            }
        }

        if(!(leftType.equals("char")) && !(rightType.equals("char")) && !(leftType.equals("int")) && !(rightType.equals("int"))){
            System.out.println("Not valid type, cond operations operation "+rightType+" "+leftType+" "+curFunc.getName());
            System.exit(-1);
        }

        else if(!(left.getTypeE().equals(right.getTypeE()))){
            System.out.println("Incompatible types, cond and operation "+curFunc.getName());
            System.exit(-1);
        }
        else{ 
            typeStack.push(new EvalType("boolean","cond",true,null,0));

        }
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
         EvalType n;
        n=typeStack.pop();
         if(n.getDims()!=null){
            if(n.getDims().size()!=n.getNumofdims()){
                System.out.println("Not compatible type , negative operation , ( array  not supported || less or more dims given)");
                System.exit(-1);
            }
        }
    
        if(!(n.getTypeE().equals("boolean"))){
            System.out.println("Not valid type, negative cond "+curFunc.getName());
            System.exit(-1);
        }
        else{ 
            typeStack.push(new EvalType("boolean","cond",true,null,0));

        }
        outANegativeCond(node);
    }

  

    


     @Override
    public void caseAIdLValue(AIdLValue node)
    {
        inAIdLValue(node);
        int size=0;
        String ltype=null;
        if(node.getIdentifier() != null)
        {
            if(curFunc.hasField(node.getIdentifier().toString())){
                ltype=curFunc.getFieldType1(node.getIdentifier().toString());
            }
            else if(curFunc.getInheritedValue(node.getIdentifier().toString())!=null)//(curFunc.getInheritedValue(node.getIdentifier().toString())!=null)
                ltype=curFunc.getInheritedValue(node.getIdentifier().toString());

            else{
                System.out.println("Variable "+node.getIdentifier().toString()+" is not declared in this scope "+curFunc.getName());
                System.exit(-1);
            }
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
        //System.out.println(ltype);
        for (int i=0;i<size;i++){
            EvalType type=typeStack.pop();
            l_r_vals.pop();

            if (!(type.getTypeE().equals("int"))){
                System.out.println("index not of int type");
                System.exit(-1);
            }
            ltype+=" []";

        }
        //System.out.println(ltype);

        LinkedList<Integer> decldims=curFunc.getDims(node.getIdentifier().toString());
        if(decldims==null)
            decldims=curFunc.getInheritedDims(node.getIdentifier().toString());

       

        if(lvalexpr.pop()){
            l_r_vals.push(true);
            typeStack.push(new EvalType(ltype,node.getIdentifier().toString(),false,decldims,size));
        }

        outAIdLValue(node);
    }
    
    @Override
    public void caseAStringLValue(AStringLValue node)
    {
        inAStringLValue(node);
        int size=0;
        String ltype=null;
        if(node.getLiteralString() != null)
        {
            node.getLiteralString().apply(this);
        }
        {
            List<PExpr> copy = new ArrayList<PExpr>(node.getEpxrs());
            size=copy.size();
            for(PExpr e : copy)
            {
                e.apply(this);
            }
        }
        if(size>1){
            System.out.println ("String has only one dimension ");
            System.exit(-1);
        }
        ltype="String";

        for (int i=0;i<size;i++){
            l_r_vals.pop();

            EvalType type=typeStack.pop();
            if (!(type.getTypeE().equals("int"))){
                System.out.println("index not of int type");
                System.exit(-1);
            }
            ltype="String";
        }
        if(lvalexpr.pop()){
            l_r_vals.push(true);
            typeStack.push(new EvalType (ltype,"string",false,null,size));
        }
        outAStringLValue(node);
    }


    @Override
    public void caseAFuncCall(AFuncCall node)
    {
        inAFuncCall(node);
        if(node.getFuncId() != null)
        {
            node.getFuncId().apply(this);
        }
     

        if(!(node.getFuncId().toString().equals(curFunc.getName())) && !(curFunc.hasChild(node.getFuncId().toString())) 
            && (curFunc.getAncFunc(node.getFuncId().toString())==null) && (curFunc.getFuncSameScope(node.getFuncId().toString())==null) && !(lib.has(node.getFuncId().toString()))) {
            System.out.println("Function has not been declared or is not in valid scope: "+node.getFuncId().toString() + " "+curFunc.getName());
            System.exit(-1);
        }

        {
            List<PExpr> copy = new ArrayList<PExpr>(node.getArgs());
            for(PExpr e : copy)
            {
                e.apply(this);
            }
        }
        List<PExpr> copy = new ArrayList<PExpr>(node.getArgs()); 
        LinkedList<Boolean> refexprs = new LinkedList<Boolean>();       
        int nargs= copy.size();
        Func callfunc = new Func(node.getFuncId().toString(),"call",false);
        for(int i=0 ;i<nargs;i++){
            String k= typeStack.pop().getTypeE();
            refexprs.add(l_r_vals.pop());
            callfunc.newParam("arg",k,false,null);//.replace("[]", "")); 

        }
        Collections.reverse(refexprs);
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
                       
        int result = callfunc.compareFuncs(compareFunc,lib);
        if(result!=0){
            if(result==2)
                System.out.println ("Not compatible types (call-definition) "+ callfunc.getName());
            if(result==1)
                System.out.println ("Not same number of parameters (call-definition) " + callfunc.getName());
            System.exit(-1);
        }

        LinkedList<Integer> refts=compareFunc.getRefptypes();
       
        Iterator<Boolean> exprsref = refexprs.listIterator();
        for(int i = 0 ; i<refexprs.size();i++){
            Boolean refexpr = exprsref.next();
            for (Integer reft:refts){
                if((reft-1==i) && (!refexpr)){
                    System.out.println ("Only lvalues can be passed as references : call " + callfunc.getName()+" "+i);
                    System.exit(-1);
                }
            }                 

        }

        

    if(callexpr.pop()){
        l_r_vals.push(true);
        EvalType ret= new EvalType(compareFunc.getRetType().replaceAll("\\s",""),"call",false,null,0);
        typeStack.push(ret);
    }
                

        outAFuncCall(node);
    }

    @Override
    public void caseAFuncDef(AFuncDef node)
    {
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
        retCheck=false;
        inAFuncDef(node);
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
        {
            List<PStmt> copy = new ArrayList<PStmt>(node.getBlock());
            for(PStmt e : copy)
            {
                e.apply(this);
            }
        }
        if( (retCheck==false)&& !(curFunc.getRetType().equals("nothing "))){
            System.out.println("Function with return type "+curFunc.getRetType()+" does not have return statement "+curFunc.getName());
            System.exit(-1);
        }
        curFunc=prvFunc;
        outAFuncDef(node);
    }


   
}