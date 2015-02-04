package com.sirckopo.guezzdachezz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class ChessWorkdesk {

	public static String readString(String prompt) {
	  System.out.print(prompt);
	  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String output = null;
	  try {
		  output = br.readLine();
	  } catch (IOException ioe) {
		  System.out.println("IO error");
		  System.exit(1);
	  }
	  return output;
	}
	
	public static String textMoveList(LinkedList<ChessMove> moves) {
		String output = "";
		for (int i = 0; i < moves.size(); i++) {
			output += moves.get(i).getString();
			if (i < moves.size() - 1)
				output += ", "; 
		}
		return output;
	}
	
	public static void main(String[] args) {
		ChessLayout l = new ChessLayout();
		ChessMove m = new ChessMove("e2-e4");
		l.setup();
		System.out.println(l.getFEN());
		System.out.println(m.getString());
		LinkedList<ChessMove> moves = l.moveBuffer(5, 2);
		System.out.println(textMoveList(moves));
	}

}
