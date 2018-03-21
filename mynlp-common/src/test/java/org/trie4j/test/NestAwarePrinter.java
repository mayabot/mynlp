package org.trie4j.test;

import java.io.PrintWriter;

public class NestAwarePrinter {
	public NestAwarePrinter(){
		this.pw = new PrintWriter(System.out);
	}
	public NestAwarePrinter(PrintWriter pw, int nest){
		this.nest = nest;
		this.pw = pw;
	}

	public void print(String format, Object... args){
		if(!nestPrinted){
			for(int i = 0; i < nest; i++) pw.print(" ");
			nestPrinted = true;
		}
		pw.print(String.format(format, args));
		pw.flush();
	}

	public void println(String format, Object... args){
		if(!nestPrinted){
			for(int i = 0; i < nest; i++) pw.print(" ");
			nestPrinted = true;
		}
		pw.println(String.format(format, args));
		pw.flush();
		nestPrinted = false;
	}

	public int nest(){
		return nest++;
	}

	public int unnest(){
		if(nest == 0) return 0;
		return nest--;
	}

	private int nest;
	private boolean nestPrinted;
	private PrintWriter pw;
}
