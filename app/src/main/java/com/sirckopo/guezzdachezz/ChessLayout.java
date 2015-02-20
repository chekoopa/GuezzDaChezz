package com.sirckopo.guezzdachezz;

import java.util.LinkedList;

/**
 * Chess layout representation with move generator.
 * @author sirckopo
 */
public class ChessLayout {
	/**
	 * The board's size
	 */
	public final static int size = 8;
	/**
	 * Black color (a figure constant)
	 */
	public final static int fBlack = 8;
	/**
	 * An empty cell (a figure constant)
	 */
	public final static int fEmpty  = 0;
	/**
	 * A pawn (a figure constant)
	 */
	public final static int fPawn   = 1;
	/**
	 * A knight (a figure constant)
	 */
	public final static int fKnight = 2;
	/**
	 * A bishop (a figure constant)
	 */
	public final static int fBishop = 3;
	/**
	 * A rook (a figure constant)
	 */
	public final static int fRook   = 4;
	/**
	 * A queen (a figure constant)
	 */
	public final static int fQueen  = 5;
	/**
	 * A king (figure constant)
	 */
	public final static int fKing   = 6;
	/**
	 * Queen side castling constant
	 */
	public final static int cQueenSide = 1;
	/**
	 * King side castling constant
	 */
	public final static int cKingSide  = 2;
	/**
	 * Figure chars constant (to use with charAt)
	 */
	public static final String tFigures = " pNBRQK";
	/**
	 * Unicode white figure chars constant (to use with charAt)
	 */
	public static final String tFiguresUnicodeW = " ♙♘♗♖♕♔";
	/**
	 * Unicode black figure chars constant (to use with charAt)
	 */
	public static final String tFiguresUnicodeB = " ♟♞♝♜♛♚";

	/**
	 * Knight move offsets array constant
	 */
	public static final short[][] knightMove = {{-2,-1},{-2,1}, {-1,-2},{-1,2},
                                                { 1,-2},{ 1,2}, { 2,-1},{ 2,1}};

	/**
	 * Checks whether a coordinate pair is within a board.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return Check result
	 */
	public static boolean areXYOnBoard(int x, int y) {
		return (0 < x && x <= size && 0 < y && y <= size);
	}
	
	/**
	 * Sets board's cell to defined value.
	 * @param x a X coordinate
	 * @param y a Y coordinate
	 * @param value a desired cell value
	 * @throws IllegalArgumentException if got wrong input
	 */
	public void setBoard(int x, int y, int value) {
		if (!areXYOnBoard(x, y) || !((0 <= value && value <= fKing) || 
			(fPawn + fBlack <= value && value <= fKing + fBlack))) {
			throw new IllegalArgumentException(); 
		}
		board[x - 1][y - 1] = value;
	}
	
	public int getBoard(int x, int y) {
		if (!areXYOnBoard(x, y)) {
			return 0;
			//throw new IllegalArgumentException();
		}
		return board[x - 1][y - 1];
	}
	
	public boolean getMove() {
		return move;
	}
	
	public void setMove(boolean value) {
		move = value;
	}
	
	public void changeMove() {
		move = !move;
	}
	
	public String getEnPassantSquare() {
		String output = "";
		output += (char)('a' - 1 + enpassant[0]);
		output += (char)('0' + enpassant[1]);
		return output;
	}
	
	public boolean getQueenSideCastling(boolean side) {
		return castling[side ? 1 : 0] % 2 == 1;
	}

	public boolean getKingSideCastling(boolean side) {
		return castling[side ? 1 : 0] > 1;
	}

	public void setQueenSideCastling(boolean side, boolean value) {
		castling[side ? 1 : 0] = 
				(short) ((getKingSideCastling(side) ? cKingSide : 0) + 
				         (value ? cQueenSide : 0));
	}

	public void setKingSideCastling(boolean side, boolean value) {
		castling[side ? 1 : 0] = 
				(short) ((getQueenSideCastling(side) ? cQueenSide : 0) + 
				         (value ? cKingSide : 0));
	}

	public boolean loadFEN(String fen) {
	    boolean done = false;
	    int phase = 0;
	    int x = 1, y = 8;
	    for (int c = 0; c < fen.length(); c++) {
	        switch (phase) {
	            case 0: // board
	            	switch (fen.charAt(c)) {
		                case 'P': setBoard(x, y, fPawn); x++; break;
		                case 'N': setBoard(x, y, fKnight); x++; break;
		                case 'B': setBoard(x, y, fBishop); x++; break;
		                case 'R': setBoard(x, y, fRook); x++; break;
		                case 'Q': setBoard(x, y, fQueen); x++; break;
		                case 'K': setBoard(x, y, fKing); x++; break;
		                case 'p': setBoard(x, y, fPawn + fBlack); x++; break;
		                case 'n': setBoard(x, y, fKnight + fBlack); x++; break;
		                case 'b': setBoard(x, y, fBishop + fBlack); x++; break;
		                case 'r': setBoard(x, y, fRook + fBlack); x++; break;
		                case 'q': setBoard(x, y, fQueen + fBlack); x++; break;
		                case 'k': setBoard(x, y, fKing + fBlack); x++; break;
		                case '1': x += 1; break;
		                case '2': x += 2; break;
		                case '3': x += 3; break;
		                case '4': x += 4; break;
		                case '5': x += 5; break;
		                case '6': x += 6; break;
		                case '7': x += 7; break;
		                case '8': x += 8; break;
		                case '/': y--; x = 1; break; // next rank
		                case ' ': phase++; break; // stop
		                default: return false; // wrong char
		            }
	                break;
	            case 1: // move flag;
	                if (fen.charAt(c) == 'w') {
	                    move = false;
	                } else if (fen.charAt(c) == 'b') {
	                    move = true;
	                } else {
	                    return false;
	                }
	                c++; phase++;
	                break;
	            case 2: // castling
	                if (fen.charAt(c) == '-') {
	                    c++; phase++;
	                } else if (fen.charAt(c) == ' ') {
	                    phase++;
	                } else if (fen.charAt(c) == 'K') {
	                    setKingSideCastling(false, true);
	                } else if (fen.charAt(c) == 'Q') {
	                	setQueenSideCastling(false, true);
	                } else if (fen.charAt(c) == 'k') {
	                	setKingSideCastling(true, true);
	                } else if (fen.charAt(c) == 'q') {
	                	setQueenSideCastling(true, true);
	                }
	                break;
	            case 3: // en passant
	                if (fen.charAt(c) == '-') {
	                    c++; phase++;
	                } else {
	                	enpassant[0] = fen.charAt(c) - 96;
	                	enpassant[1] = fen.charAt(c+1) - '0';
	                    c+=2; phase++;
	                }
	                break;
	            case 4: // halfmove clock -- skipping
	                if (fen.charAt(c) == ' ') {
	                    phase++;
	                }
	                break;
	            case 5: // move counter -- skipping
	                if (fen.charAt(c) == ' ' || c == fen.length() - 1) { 
	                    phase++; done = true;
	                }
	                break;
	            case 6: // problem type
	                if (fen.charAt(c) == 'h') {
	                    coop = true;
	                } else if (fen.charAt(c) == '#') {
	                    // placeholder to mate problem type, but...
	                    // we need to read the depth, so
	                    depth = Integer.parseInt(fen.substring(c + 1));
	                }
	                // studies are "+" (to win) and "=" (to draw) without depth
	                break;
	        }
	    }
	    return done;
	}
	
	public String getFEN() {
	    String output = "";

	    // board
	    char chsFigure[] = {' ','p','n','b','r','q','k'};
	    for (int y = size; y > 0; y--) {
	        int empties = 0;
	        for (int x = 1; x <= size; x++) {
	            if (getBoard(x, y) != 0) {
	                if (empties != 0) {
	                    output += (char) (empties + 48);
	                    empties = 0;
	                }
	                output += (char)(chsFigure[getBoard(x, y) % fBlack] +
	                           32 * (getBoard(x, y) / fBlack - 1));
	            } else {
	                empties++;
	            }
	        }
	        if (empties != 0) {
	            output += (char) (empties + 48);
	        }
	        output += (y > 1 ? "/" : " ");
	    }

	    // move flag
	    output += (move ? "b" : "w");
	    output += " ";

	    // castling
	    boolean hadCastling = false;
        if (getKingSideCastling(false)) {
            output += "K";
            hadCastling = true;
        }
        if (getQueenSideCastling(false)) {
            output += "Q";
            hadCastling = true;
        }
        if (getKingSideCastling(true)) {
            output += "k";
            hadCastling = true;
        }
        if (getQueenSideCastling(true)) {
            output += "q";
            hadCastling = true;
        }
	    
	    if (!hadCastling) {
	        output += "-";
	    }
	    output += " ";

	    // en passant
	    if (enpassant[0] != 0) {
	        output += getEnPassantSquare();
	    } else {
	        output += "-";
	    }
	    output += " ";

	    // halfmove clock and move number just are not implemented, so...
	    output += "0 1";

	    // problem type looks like [h is for help]#<number of moves>
	    if (depth != 0) {
	        output += " ";
	        output += (coop ? "h" : "");
	        output += "#";
	        output += String.valueOf(depth);
	    }

	    return output;
	}
	
	public void reset() {
		board = new int[size][size];
		move = false;
		castling = new int[2];
		enpassant = new int[2];
		depth = 0;
		coop = false;
	}
	
	public void setup() {
		reset();
		loadFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}
	
	public int getBoardFigure(int x, int y) {
		return getBoard(x, y) % fBlack;
	}
	
	private void _do_move(int xX, int xY, int X, int Y, int promote) {
		
		switch (getBoardFigure(xX, xY)) {
		case fKing:
			castling[move ? 1 : 0] = 0;
			if (Math.abs(X - xX) == 2) {
				if (X == 7) {
					setBoard(X + 1, Y, fEmpty);
					setBoard(X - 1, Y, fRook + (move ? fBlack : 0));
				} else if (X == 3) {
					setBoard(X - 2, Y, fEmpty);
					setBoard(X + 1, Y, fRook + (move ? fBlack : 0));
		        }
			}
			break;
		case fRook:
			if (xX == 1) {
				setQueenSideCastling(move, true);
			} else if (xX == 8) {
				setKingSideCastling(move, true);
			}
			break;
		case fPawn:
			if (Math.abs(Y - xY) == 2) {
				enpassant[0] = X; enpassant[1] = (move ? 6 : 3);
			} else if (enpassant[0] == X && enpassant[1] == Y) {
				setBoard(X, Y + (move ? 1 : -1), fEmpty);
			}

		}

        //TODO: castling bit changes on rook capture

		if (enpassant[1] != (move ? 6 : 3)) {
			enpassant = new int[2];
		}
		
		setBoard(X, Y, getBoard(xX, xY));
		setBoard(xX, xY, fEmpty);
		
		if (promote != 0 && getBoardFigure(X, Y) == fPawn &&
				(Y == 1 || Y == 8)) {
			setBoard(X, Y, promote);
		}
		
		move = !move;
	}
	
	public boolean isLegit() {
		// pawn on last ranks check
	    for (int i = 1; i <= size; i++) {
	    	if (getBoardFigure(i, 1) == fPawn ||
	    		getBoardFigure(i, size) == fPawn) {
	    		return false;
	    	}
	    }

	    int figs[][] = new int[7][2];

	    for (short j = 1; j <= size; j++) for (short i = 1; i <= size; i++)
	        if (getBoard(i, j) != 0)
	            figs[getBoardFigure(i, j)][getBoard(i, j) / fBlack]++;

	    // something wrong on kings
	    if (figs[6][0] != 1 || figs[6][1] != 1) 
	        return false;

	    // too much figures
	    if (figs[1][0] + figs[2][0] + figs[3][0] + figs[4][0] + figs[5][0] > 15)
	        return false;
	    if (figs[1][1] + figs[2][1] + figs[3][1] + figs[4][1] + figs[5][1] > 15)
	        return false;

	    // too much spare figures
	    if ((figs[5][0] > 1 ? figs[5][0] - 1 : 0) +
	        (figs[4][0] > 2 ? figs[4][0] - 2 : 0) +
	        (figs[3][0] > 2 ? figs[3][0] - 2 : 0) +
	        (figs[2][0] > 2 ? figs[2][0] - 2 : 0) > 8 - figs[1][0])
	        return false;
	    if ((figs[5][1] > 1 ? figs[5][1] - 1 : 0) +
	        (figs[4][1] > 2 ? figs[4][1] - 2 : 0) +
	        (figs[3][1] > 2 ? figs[3][1] - 2 : 0) +
	        (figs[2][1] > 2 ? figs[2][1] - 2 : 0) > 8 - figs[1][1])
	        return false;

	    // self check
        return !isCheck(!move);

    }
	
	public boolean isCheck(boolean fmove) {
		int x = 0, y = 0;
		for (int i = 1; i <= size && x == 0; i++)
			for (int j = 1; j <= size && x == 0; j++)
				if (getBoard(i, j) == (fKing + (fmove ? fBlack : 0))) {
	                    x = i; y = j;
				}
		return isChecked(x, y, fmove);
	}
	
	public boolean isPlaceable(int x, int y, boolean fmove) {
        if (!areXYOnBoard(x, y)) {
            return false;
        }
		if (getBoard(x, y) == fEmpty) // automatic wrong argument throw
			return true;
		if (getBoardFigure(x, y) == fKing)
			return false;
		if (getBoard(x, y) / fBlack == (fmove ? 1 : 0)) 
			return false;
		// TODO?: implement getBoardSide(x, y)
	    return true;
	}
	
	public boolean isChecked(int x, int y, boolean fmove) {
		if (!areXYOnBoard(x, y)) {
			throw new IllegalArgumentException("Wrong square " + x + " " + y);
		}
		for (short i = -1; i <= 1; i++) 
			for (short j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				try {
					if (getBoard(x + i, y + j) == (fmove ? 0 : fBlack) + fKing)
						return true;
				} catch (IllegalArgumentException e) {}
			}
		return isCheckedNoKing(x, y, fmove);
	}
	
	public boolean isCheckedNoKing(int x, int y, boolean fmove) {
		if (!areXYOnBoard(x, y)) {
			throw new IllegalArgumentException(); 
		}
		// pawn
		if (areXYOnBoard(x + 1, y + (fmove ? 1 : -1)))
			if (getBoard(x + 1, y + (fmove ? 1 : -1)) == 
			     fPawn + (fmove ? 0 : fBlack))
				return true;
		if (areXYOnBoard(x - 1, y + (fmove ? 1 : -1)))
			if (getBoard(x - 1, y + (fmove ? 1 : -1)) == 
			     fPawn + (fmove ? 0 : fBlack))
				return true;
		return isReachableNoPawn(x, y, !fmove);
	}
	
	public boolean isReachable (int x, int y, boolean fmove) {
		return isReachableEmpty(x, y, fmove) || 
				(getBoard(x, y) / fBlack == (fmove ? 0 : 1) &&
				 isChecked(x, y, !fmove));
	}
	
	public boolean isReachableEmpty (int x, int y, boolean fmove) {
		if (!areXYOnBoard(x, y) && getBoard(x, y) != fEmpty) {
			throw new IllegalArgumentException(); 
		}
		// pawn
		if (areXYOnBoard(x + 1, y + (fmove ? -1 : 1)))
			if (getBoard(x + 1, y + (fmove ? -1 : 1)) == 
			     fPawn + (fmove ? fBlack : 0))
				return true;

	    if (areXYOnBoard(x, y + (fmove ? -1 : 1)))
	        if (getBoard(x, y + (fmove ? -1 : 1)) == 
	             fPawn + (fmove ? fBlack : 0))
	            return true;
	    if (y == (fmove ? 4 : 5))
	        if (getBoard(x, y + (fmove ? -2 : 2)) == 
				 fPawn + (fmove ? fBlack : 0))
	            return true;
	    return isReachableNoPawn(x, y, fmove);
	}

	public boolean isReachableNoPawn(int x, int y, boolean fmove) {
		if (!areXYOnBoard(x, y)) {
			return false;
		}
		short i = 0;
		for(i=0; i<8; i++)
			if (areXYOnBoard(x+knightMove[i][0],y+knightMove[i][1]))
				if (getBoard(x+knightMove[i][0], y+knightMove[i][1]) ==
						(fmove ? fBlack : 0) + fKnight) {return true;}
	   for(i=1; getBoard(x+i,y) == fEmpty; i++)
	      {if(!areXYOnBoard(x + i, y)) {break;}}
	   if (areXYOnBoard(x+i,y))
	   {
	      if(getBoard(x + i, y)==(fmove ? fBlack : 0) + fRook)  {return true;}
	      if(getBoard(x + i, y)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   for(i=-1; getBoard(x + i, y)==fEmpty; i--)
	      {if(!areXYOnBoard(x+i,y)){break;}}
	   if (areXYOnBoard(x+i,y))
	   {
	      if(getBoard(x + i, y)==(fmove ? fBlack : 0) + fRook)  {return true;}
	      if(getBoard(x + i, y)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	
	   for(i=1; getBoard(x, y + i)==fEmpty; i++)
	      {if(!areXYOnBoard(x,y+i)){break;}}
	   if (areXYOnBoard(x,y+i))
	   {
	      if(getBoard(x, y + i)==(fmove ? fBlack : 0) + fRook)  {return true;}
	      if(getBoard(x, y + i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   for(i=-1; getBoard(x, y + i)==fEmpty; i--)
	      {if(!areXYOnBoard(x,y+i)){break;}}
	   if (areXYOnBoard(x,y+i))
	   {
	      if(getBoard(x, y + i)==(fmove ? fBlack : 0) + fRook)  {return true;}
	      if(getBoard(x, y + i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	
	   for(i=1; getBoard(x+i, y+i)==fEmpty; i++)
	      {if(!areXYOnBoard(x+i,y+i)){break;}}
	   if (areXYOnBoard(x+i,y+i))
	   {
	      if(getBoard(x+i, y+i)==(fmove ? fBlack : 0) + fBishop)  {return true;}
	      if(getBoard(x+i, y+i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   for(i=-1; getBoard(x+i, y+i)==fEmpty; i--)
	      {if(!areXYOnBoard(x+i,y+i)){break;}}
	   if (areXYOnBoard(x+i,y+i))
	   {
	      if(getBoard(x+i, y+i)==(fmove ? fBlack : 0) + fBishop)  {return true;}
	      if(getBoard(x+i, y+i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   for(i=1; getBoard(x+i, y-i)==fEmpty; i++)
	      {if(!areXYOnBoard(x+i,y-i)){break;}}
	   if (areXYOnBoard(x+i,y-i))
	   {
	      if(getBoard(x+i, y-i)==(fmove ? fBlack : 0) + fBishop)  {return true;}
	      if(getBoard(x+i, y-i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   for(i=-1; getBoard(x+i, y-i)==fEmpty; i--)
	      {if(!areXYOnBoard(x+i,y-i)){break;}}
	   if (areXYOnBoard(x+i,y-i))
	   {
	      if(getBoard(x+i, y-i)==(fmove ? fBlack : 0) + fBishop) {return true;}
	      if(getBoard(x+i, y-i)==(fmove ? fBlack : 0) + fQueen) {return true;}
	   }
	   return false;
	}

	public boolean isCheckmate(boolean fmove) {
        int[][] xboard = new int[size][size];
        for (int i = 0; i < size; i++) {
            xboard[i] = board[i].clone();
        }
        boolean xmove = move;
        int[] xcastling = castling.clone();
        int[] xenpassant = enpassant.clone();
		if (!isCheck(fmove))
			return false;
		for (int x = 1; x <= size; x++) for (int y = 1; y <= size; y++) {
			int f = getBoard(x, y);
			if (f != fEmpty && f / fBlack == (getMove() ? 1 : 0)) {
				LinkedList<ChessMove> mbuf = moveBuffer(x, y);
				for (ChessMove cm : mbuf)
					if (doMove(cm)) {
                        board = xboard;
                        move = xmove;
                        castling = xcastling;
                        enpassant = xenpassant;
                        return false;
                    }

			}
		}
		return true;
	}
	
	public boolean isMoveCorrect(ChessMove m) {
		// it's temporary maybe? i really hate this workaround. :c
		LinkedList<ChessMove> moves = moveBuffer(m.getCode(0), m.getCode(1));
        for (ChessMove cm: moves)
            if (m.isEqual(cm))
                return true;
		return false;
    }
	
	public boolean doMove(ChessMove m) {
		if (!isMoveCorrect(m)) {
			//throw new IllegalArgumentException("Incorrect move " +
			//		                           m.getString());
            //NOTE: let's try to make it less killing
            return false;
		}

        int[][] xboard = new int[size][size];
        for (int i = 0; i < size; i++) {
            xboard[i] = board[i].clone();
        }
		boolean xmove = move;
		int[] xcastling = castling.clone();
		int[] xenpassant = enpassant.clone();
		
		int[] mc = m.getCode();
		
	    _do_move(mc[0], mc[1], mc[2], mc[3], mc[4]);
	    
	    if (isCheck(!move)) {
	    	board = xboard; move = xmove; 
	    	castling = xcastling; enpassant = xenpassant; 
	    	return false;
	    }
   			        
	    return true;
	}
	
	public LinkedList<ChessMove> moveBuffer(int x, int y) {
		return moveBufferFigure(x, y, getBoard(x, y));
	}
	
	public LinkedList<ChessMove> moveBufferFigure(int x, int y, int figure) { 
		LinkedList<ChessMove> mbuf = new LinkedList<>();
		if (!areXYOnBoard(x, y))
			throw new IllegalArgumentException(); 
		if (figure == fEmpty || figure / fBlack != (move ? 1 : 0))
			return mbuf; 
		int i = 0;

   		switch (figure % fBlack) {
   		case fPawn:
   			if (areXYOnBoard(x, y + (move ? -1 : 1)))
   				if(getBoard(x, y + (move ? -1 : 1)) == fEmpty) {
   					mbuf.push(new ChessMove(x, y, x, y + (move ? -1 : 1)));
   				}
			if (y == (move ? 7 : 2) && getBoard(x, y + (move ? -1 : 1)) ==
				 fEmpty && getBoard(x, y + (move ? -2 : 2)) == fEmpty) {
				mbuf.push(new ChessMove(x, y, x, y + (move ? -2 : 2)));
			}
            // TODO: make en-passant work
			if (isPlaceable(x + 1, y + (move ? -1 : 1), move))
				if (getBoard(x + 1, y + (move ? -1 : 1)) != fEmpty ||
				    (x + 1 == enpassant[0] && y + (move ? -1 : 1) == enpassant[1])) {
					mbuf.push(new ChessMove(x, y, x + 1, y + (move ? -1 : 1)));
				}
			if (isPlaceable(x - 1, y + (move ? -1 : 1), move))
				if (getBoard(x - 1, y + (move ? -1 : 1)) != fEmpty ||
				    (x - 1 == enpassant[0] && y + (move ? -1 : 1) == enpassant[1])) {
					mbuf.push(new ChessMove(x, y, x - 1, y + (move ? -1 : 1)));
				}
			break;
   		case fKnight:
   			for (i = 0; i < 8; i++) {
   				if (isPlaceable(x + knightMove[i][0], y + knightMove[i][1], move)) {
   					mbuf.push(new ChessMove(x, y, x + knightMove[i][0],
   							y + knightMove[i][1]));
   				}
   			}
   			break;
   		case fQueen: //a little optimization: queen = rook + bishop
   		case fBishop:
   			for(i = 1; isPlaceable(x + i, y + i, move); i++) {
   				mbuf.push(new ChessMove(x, y, x + i, y + i));
   				if((getBoard(x+i, y+i) != fEmpty)&&
   					getBoard(x+i, y+i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = -1; isPlaceable(x + i, y + i, move); i--) {
   				mbuf.push(new ChessMove(x, y, x + i, y + i));
   				if((getBoard(x+i, y+i) != fEmpty)&&
   					getBoard(x+i, y+i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = 1; isPlaceable(x + i, y - i, move); i++) {
   				mbuf.push(new ChessMove(x, y, x + i, y - i));
   				if((getBoard(x+i, y-i) != fEmpty)&&
   					getBoard(x+i, y-i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = -1; isPlaceable(x + i, y - i, move); i--) {
   				mbuf.push(new ChessMove(x, y, x + i, y - i));
   				if((getBoard(x+i, y-i) != fEmpty)&&
   					getBoard(x+i, y-i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			if(figure % fBlack == fBishop) {break;}

   		case fRook:
   			for(i = 1; isPlaceable(x + i, y, move); i++) {
   				mbuf.push(new ChessMove(x, y, x + i, y));
   				if((getBoard(x+i, y) != fEmpty) &&
   					getBoard(x+i, y)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = -1; isPlaceable(x + i, y, move); i--) {
   				mbuf.push(new ChessMove(x, y, x + i, y));
   				if((getBoard(x+i, y) != fEmpty) &&
   					getBoard(x+i, y)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = 1; isPlaceable(x, y + i, move); i++) {
   				mbuf.push(new ChessMove(x, y, x, y + i));
   				if((getBoard(x, y+i) != fEmpty) &&
   					getBoard(x, y+i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			for(i = -1; isPlaceable(x, y + i, move); i--) {
   				mbuf.push(new ChessMove(x, y, x, y + i));
   				if((getBoard(x, y+i) != fEmpty) &&
   					getBoard(x, y+i)/fBlack == (move ? 0 : 1)) {break;}
   			}
   			break;

   		case fKing:
   			for(i=-1;i<=1;i++) for(short j=-1;j<=1;j++)
   				if (!(i == 0 && j == 0) && isPlaceable(x+i,y+j,move) &&
   						!isChecked(x+i, y+j, move)) {
   					mbuf.push(new ChessMove(x, y, x+i, y+j));
   				}
   			// castling
   			if (castling[move ? 1 : 0] != 0 && !isChecked(x, y, move) &&
   					x == 5 && y == (move ? 8 : 1)) {
   				if (getQueenSideCastling(move) &&
   					 getBoard(x-1, y) == fEmpty && !isChecked(x-1, y, move) &&
   					 getBoard(x-2, y) == fEmpty && !isChecked(x-2, y, move) &&
   					 getBoard(x-3, y) == fEmpty) {
   					mbuf.push(new ChessMove(x, y, x - 2, y));
   				}
   				if (getKingSideCastling(move) &&
   					 getBoard(x+1, y) == fEmpty && !isChecked(x+1, y, move) &&
      				 getBoard(x+2, y) == fEmpty && !isChecked(x+2, y, move)) {
   					mbuf.push(new ChessMove(x, y, x + 2, y));
   				}
   			}
   			break;
   		}
		return mbuf;
	}
	
	public String getTextBoard() {
		String o = "";
		for (int y = size; y > 0; y++) {
			for (int x = 1; x <= size; x++) {
				int f = getBoard(x, y);
				o += (f / fBlack == 1 ?
						tFigures.toLowerCase().charAt(f % fBlack) :
						tFigures.toLowerCase().charAt(f % fBlack));
			}
			o += "\n";
		}
		return o;
	}
	
	public String getTextCondition() {
		return (getMove() ? "Black" : "White") + " " + (depth > 0 ? 
				"starts and " + (coop ? "self" : "") + "mates in " +
				String.valueOf(depth): "moves");
	}
	
	public String getTextLayout() {
		return getTextBoard() + "\n" + getTextCondition();
	}
	
	// private data ...
	private int[][] board = new int[size][size];
	private boolean move = false;
	private int[] castling = new int[2];
	private int[] enpassant = new int[2];
	private int depth = 0;
	private boolean coop = false;
}
