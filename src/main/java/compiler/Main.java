package compiler;

import compiler.lexer.Lexer;
import compiler.lexer.LexerException;
import compiler.node.Start;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import compiler.TreeTraversalP1;
import compiler.Library;
import compiler.Quad;
import compiler.QuadstoA;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;

import java.io.PushbackReader;
import java.util.*;

public class Main { 

	public static void main(String args[]) { 
	 	FileInputStream fis = null;
	    InputStreamReader isr = null;

	    try{
	    	//fis = new FileInputStream("./src/test/test1.grace");
	    	fis = new FileInputStream(args[0]);

	    }catch(Exception e){
		  	e.printStackTrace();
		}
	    isr = new InputStreamReader(fis);
	    PushbackReader readerParser = new PushbackReader(isr, 1024);

		Start tree = null;
		try {
	    	Parser p = new Parser(new Lexer(readerParser));
	        tree = p.parse();
	    } catch (LexerException e) {
	        System.err.printf("Lexing error: %s\n", e.getMessage());
	        return;
	    } catch (IOException e) {
	        System.err.printf("I/O error: %s\n", e.getMessage());
	        	e.printStackTrace();
	        	return;
	    } catch (ParserException e) {
	           System.err.printf("Parsing error: %s\n", e.getMessage());
	           return;
	    }
	   	Library lib = new Library();


	   TreeTraversalP1 tt1=new TreeTraversalP1(lib); 

	   tree.apply(tt1);
	   TreeTraversalP2 tt2=new TreeTraversalP2(tt1.getvTable(),lib);

	   tree.apply(tt2);


	   TreeTraversalP3 tt3=new TreeTraversalP3(tt2.getVtable(),lib);
	   tree.apply(tt3);
	   tt3.procFuncs();

	   LinkedList<Quad> quads = new LinkedList<Quad>(tt3.getQuads());

	  for(Quad q:quads){
	   	 q.printQuad();
	  }

	   QuadstoA assemblygen= new QuadstoA(quads,tt3.getVtable(),args[0],lib,tt3.getatext(),tt3.gethashtext());
	   assemblygen.quadProc();
	}

}
