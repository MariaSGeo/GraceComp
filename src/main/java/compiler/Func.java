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
import compiler.Temp;



public class Func {   
    
    String name;
    String retType;
    HashMap<String,String> variables;
    HashMap<String,String> parameters;
    HashMap<String,String> tempshash;
    LinkedList<String> parameters_order;
    HashMap<String,Func> children;
    int depth;
    LinkedList<LinkedList<String>> paramlist; //formal param list
    
    LinkedList<Func>  childrenList; //children funcs
    
    LinkedList<String> ptypes;      //all types
    LinkedList<Integer> refptypes;  //all ref types
    
    LinkedList<Temp> temps;         //temp variables

    boolean declaredfirst;          //if the func was declared before its definition
    boolean definedsecond;          //if the func was defined

    HashMap<String,LinkedList<Integer>> vardims;    //stores dimensions
    HashMap<String,LinkedList<Integer>> pardims;    //stores dimensions

    HashMap<String,Temp> vars_tmp;              //temporary variables
    HashMap<String,Temp> params_tmp;            //temporary parameters

    LinkedList<String> sorted_vars;             //sorted vars for better memory optimization
    HashMap<String,Integer> offset_vars;        //the offset of variables

    LinkedList<String> procced_params;          //same as above for parameters
    HashMap<String,Integer> offset_params;

    LinkedList<String> sorted_temps;            //same as above for temporary variables
    HashMap<String,Integer> offset_temps;

    HashMap<String,String> inheritedVars;       //ingerited variables from mother func

    int temp;                                   //sizes for mem allocation
    int localvarssize;                          
    int tempssize;
    int fullsize;
    int paramssize;

    Func mother;
   

    public Func(String name,String ret,boolean decl){ // if the function does not have a mother function
        //System.out.println("New func name "+name);
        this.name=name;
        this.retType=ret;
        mother=null;
        variables = new HashMap<String,String>();
        parameters = new HashMap<String,String>();
        children = new HashMap<String,Func>(); 
        childrenList=new LinkedList<Func>();
        ptypes = new LinkedList <String>();
        refptypes = new LinkedList<Integer>();
        paramlist = new LinkedList<LinkedList<String>>();
        temps = new LinkedList<Temp>();
        refptypes = new LinkedList <Integer>(); 
        params_tmp = new  HashMap<String,Temp>();
        vars_tmp = new HashMap<String,Temp>();
        vardims= new HashMap<String,LinkedList<Integer>>();
        pardims= new HashMap<String,LinkedList<Integer>>();  
        parameters_order=new LinkedList<String>();    
        declaredfirst = decl;
        tempshash=new HashMap<String,String>();
        temp=0;
        if (declaredfirst)
            definedsecond = false;
        else
            definedsecond = true;

    }


    public Func(String name,String ret,boolean decl,Func mother){// if the function has a mother function
        //System.out.println("New func name "+name);
        this.name=name;
        this.retType=ret;
        this.mother=mother;
        variables = new HashMap<String,String>();
        parameters = new HashMap<String,String>(); 
        children = new HashMap<String,Func>();
        childrenList=new LinkedList<Func>();
        ptypes = new LinkedList <String>();
        refptypes = new LinkedList<Integer>();
        temps = new LinkedList<Temp>();
        paramlist = new LinkedList<LinkedList<String>>();
        refptypes = new LinkedList <Integer>();
        params_tmp = new  HashMap<String,Temp>();
        vars_tmp = new HashMap<String,Temp>();
        vardims= new HashMap<String,LinkedList<Integer>>();
        pardims= new HashMap<String,LinkedList<Integer>>(); 
        parameters_order=new LinkedList<String>(); 
        tempshash=new HashMap<String,String>();
   
        inheritedVars=mother.getVars();
   
        declaredfirst = decl;
        temp=0;
        if (declaredfirst)
            definedsecond = false;
        else
            definedsecond = true;

    }

    public void sortVars(){                 //sort the vars according to type 
        sorted_vars=new LinkedList<String>();
        offset_vars=new HashMap<String,Integer>();
        localvarssize=0;

        for (Map.Entry<String, String> var : variables.entrySet()) {
            String varname = var.getKey();
            String vartype = var.getValue();
            String[] split = vartype.split("\\s+");
            String type=split[0].replaceAll("\\s","");
            if(type.equals("int")&&split.length==1){
                offset_vars.put(varname,localvarssize);
                sorted_vars.add(varname);
                localvarssize+=4;
            }else if(type.equals("int")&&split.length>1){
                         //   System.out.println(var.getKey());

                sorted_vars.add(varname);
                offset_vars.put(varname,localvarssize);

                LinkedList<Integer> dims=getDims(varname);
                int sum=4;
                for(int dim : dims){
                    sum*=dim;
                }
                localvarssize+=sum;

            }
        }
        for (Map.Entry<String, String> var : variables.entrySet()) {
                      //  System.out.println(var.getKey());

            String varname = var.getKey();
            String vartype = var.getValue();
            String[] split = vartype.split("\\s+");
            String type=split[0];
            if(type.equals("char")&&split.length==1){
                offset_vars.put(varname,localvarssize);
                sorted_vars.add(varname);
                localvarssize+=1;
            }else if(type.equals("char")&&split.length>1){
                offset_vars.put(varname,localvarssize);

                sorted_vars.add(varname);
                LinkedList<Integer> dims=getDims(varname);
                int sum=1;
                for(int dim : dims){
                    sum*=dim;
                }
                localvarssize+=sum;

            }
        }

        if(localvarssize%4!=0){
            localvarssize+=localvarssize%4;

        }
    }

    public void procParams(){   //"sort" the parameters and find their total size

        Collections.reverse(parameters_order);

        procced_params=new LinkedList<String>();
        offset_params=new HashMap<String,Integer>();
        paramssize=0;

        for(String parameter:parameters_order){
            offset_params.put(parameter,paramssize);
            paramssize+=4;

        }
       /* for (Map.Entry<String, String> param : parameters.entrySet()) {
            String paramname = param.getKey();
            //String paramtype = param.getValue();
            //String[] split = paramtype.split("\\s+");
            //String type=split[0];
            //if(type.equals("int")&&split.length==1){
                offset_params.put(paramname,paramssize);
                paramssize+=4;
            //}else if(type.equals("int")&&split.length>1){
               

            //}
        ptyp
        }*/
       
    }

    public int getParamssize(){return paramssize;}

    public Integer getOffset(String name){      //offset of a requested var/param/temp 
       // System.out.println(this.name+ " "+name);
        
        if(offset_temps.get(name)!=null)
            return offset_temps.get(name);

        else if(offset_vars.get(name)!=null)
            return offset_vars.get(name);

        else if(offset_params.get(name)!=null)
            return offset_params.get(name);

        else {
            
            return null;    
        }
        

    }

    public Integer getInhOffset(String name){   //offset of an inherited var/param/temp 
      //  System.out.println(this.name+" "+name+"1");
        if(mother.getOffset(name)!=null){
            return  mother.getOffset(name);
        }else{
            return mother.getInhOffset(name);
        }

    }

     public void sortTemps(){    //sort the vars according to type 
        sorted_temps=new LinkedList<String>();
        offset_temps=new HashMap<String,Integer>();
        tempssize=localvarssize;
        //System.out.println("SORTING TEMPS"+temps.size()+ " "+name);
        for(Temp tmp:temps){
            //System.out.println("for INT OR BOOL TEMP $"+tmp.getN() + " "+tmp.getT());
            if(tmp.getT().equals("int")||tmp.getT().equals("int ") || tmp.getT().equals("boolean")){
               // System.out.println("INSERTED");
                sorted_temps.add("$"+tmp.getN());
                offset_temps.put("$"+tmp.getN(),tempssize);
                tempssize+=4;
            }
        }

        for(Temp tmp:temps){
                 //       System.out.println("for CHAR TEMP $"+tmp.getN());

            if(tmp.getT().equals("char")||tmp.getT().equals("char ")){
                sorted_temps.add("$"+tmp.getN());
                offset_temps.put("$"+tmp.getN(),tempssize);
                tempssize+=1;
            }
        }
        tempssize+=tempssize%4;
        fullsize=tempssize;//+localvarssize;

     }

    public int getFuncSize(){return fullsize;} 

    public int getDepth(){return depth;}

    public void setDepth(int d){depth=d;}

    public LinkedList<Integer> getRefptypes(){return refptypes;}

    public String getName(){return name;}

    public boolean getDeclared(){return declaredfirst;}

    public void newParam(String name,String type,boolean ref,LinkedList<Integer> dims){ //insert a new parameter

        //Temp nt = newTemp(type); 
        parameters.put(name,type);
        //params_tmp.put(name,nt);
        ptypes.add(type);
        parameters_order.add(name);
        pardims.put(name,dims);
        if(ref){
            refptypes.add(ptypes.size());
        }


    }
    
    public void newVariable(String name,String type,LinkedList<Integer> dims){ //insert a new variable

        //Temp nt = newTemp(type); 
        variables.put(name,type);
        //vars_tmp.put(name,nt);
        vardims.put(name,dims);

    }

    public void newChild (String name,Func child){  //insert a new child function
        children.put(name,child);
        childrenList.add(child);
            
    }

    public HashMap<String,Func> getChildren(){return children;}

    public HashMap<String,String> getVars(){return variables;}

    public  HashMap<String,String> getParams(){return parameters;};

    public  void newParamList(LinkedList<String> list){paramlist.add(list);}

    public LinkedList<Func> getChildrenList(){return childrenList;}

    public LinkedList<LinkedList<String>> getParamList(){return paramlist;}

    public String getVariableType(String name){return variables.get(name);}

    public String getInheritedVarType(String name){
        return inheritedVars.get(name);
    }

    public String getParameterType(String name){return parameters.get(name);}

    public Func getChild(String name){return children.get(name);}

    public String getRetType(){return retType;}

    public List<String> getPTypes(){return ptypes;}

    public boolean getDefined(){return definedsecond;}

    public void setDefined(){definedsecond=true;}

    public boolean hasVariable(String name){return variables.containsKey(name);}

    public boolean hasParameter(String name){return parameters.containsKey(name);}

    public boolean hasChild(String name){return children.containsKey(name);}

    public String methodParamType(String name){

        if(hasParameter(name))
            return getParameterType(name);
        else
            return null;

    }

    public String methodVarType(String name){
        
        if(hasVariable(name))
            return getVariableType(name);
        else
            return null;

    }

    public Func methodChild(String name){
        
        if(hasChild(name))
            return getChild(name);
        else
            return null;

    }


    public int compareFuncs(Func function,Library lib){ //compare with a given function

        Collections.reverse(ptypes);

        Iterator<String> func1=ptypes.listIterator();
        Iterator<String> func2=function.getPTypes().listIterator();

        while(func1.hasNext()){

            if(func2.hasNext()==false)          
                return 1;
                String paramf1=func1.next();
                String paramf2=func2.next(); 
                  if(!(lib.compatibleTypes(paramf2,paramf1))){
                    return 2;
                }
        }

        if(func2.hasNext())              
            return 1;
        return 0;

    }

    public void removeChild(String name){children.remove(name);}

     public int compareHeaders(Func function){ //compare with a given function header 

        if(!retType.equals(function.getRetType())) 
            return 1;

        if(!paramlist.equals(function.getParamList()))
            return 2;

        return 0;

    } 

    public boolean hasField(String name){ //check if the fucntion has a parameter or variable
        if(hasParameter(name) || hasVariable(name)){
            return true;
        }
        else{

            return false;
        }
    } 

    public String getFieldType1(String name){ //return a parameter or variable type
        if(hasParameter(name))
            return getParameterType(name);
        else if (hasVariable(name))
            return getVariableType(name);

        return null;
    }

    public String getFieldType2(String name){  //return a parameter or variable type (can be inherited)
        if(hasParameter(name))
            return getParameterType(name);
        else if (hasVariable(name))
            return getInheritedVarType(name);
            //return getVariableType(name);

        return null;
    }

    public String getInheritedValue(String name){ //return a parameter or variable type (only inherited)
        if(getInheritedVarType(name)!=null)
            return getInheritedVarType(name);
        if(mother!=null){
            if(mother.hasField(name))
                return mother.getFieldType2(name);
            else{
                return mother.getInheritedValue(name) ;
            }
        }
        return null;    
                
    }

    public String getInheritedVar(String name){ //return a variable type (only inherited)
        if(mother!=null){
            if(mother.hasVariable(name))
                return mother.getVariableType(name);
            else
                return mother.getInheritedVar(name) ;
        }
        return null;    
    }

    public String getInheritedPar(String name){//return a parameter type (only inherited)
        if(mother!=null){
            if(mother.hasVariable(name))
                return mother.getParameterType(name);
            else
                return mother.getInheritedPar(name) ;
        }
        return null;    

    }

    public LinkedList<Integer> getInheritedDims(String name){//get var/param dimensions(only inherited)
        if(mother!=null){
            if(mother.hasField(name))
                return mother.getDims(name);
            else
                return mother.getInheritedDims(name) ;
        }
        return null;    

    }

    public LinkedList<Integer> getDims(String name){//get var/param dimensions(can be inherited)
        if(hasParameter(name))
            return getParamDims(name);
        else if (hasVariable(name))
            return getVarDims(name);

        return null;
    }

    public LinkedList<Integer> getParamDims(String name){return pardims.get(name);}

    public LinkedList<Integer> getVarDims(String name){return vardims.get(name);}



    public Func getFuncSameScope(String name){//get func of the same scope with given name 

        boolean found=false;
        int pos1=0;
        int pos2=0;
        int count=0;
        if (mother==null)
            return null;
        else{
            Iterator<Func> functions=mother.getChildrenList().listIterator();
            while(functions.hasNext()){
                String fname = functions.next().getName();
                if(fname.equals(name)){ // found what we are looking for
                    pos1=count;
                    found=true;
                    break;
                }
                count++;
            }
            count=0;
            if (found == true){
                functions=mother.getChildrenList().listIterator();
                while(functions.hasNext()){
                    String fname = functions.next().getName();
                    if(fname.equals(this.name)){ // found what we are looking for
                        pos2=count;
                        //found=true;
                        break;
                    }
                    count++;
                }
            }else{

                return null;
            }
            if(pos1<pos2)
                return mother.getChild(name);
            else
                return null;
        }

    }
    public Func getMother(){return mother;}

    public Func getAncFunc(String name){ //get ancestor func
        if(mother!=null){
            if(mother.getName().equals(name)) 
                return mother;
            else
                return mother.getAncFunc(name) ;
        }
        return null;    
                
    }

    public Temp newTemp(String type){ // new temporary variable
        
        Temp nr = new Temp(temp,type); 
        //System.out.println("TEMP OF TYPE "+type + " "+temps.size());
        tempshash.put("$"+temp,type);
        temps.add(nr);
        temp++;
        return nr;

    }
    public String getTmpType(String name){return tempshash.get(name);}
   
}