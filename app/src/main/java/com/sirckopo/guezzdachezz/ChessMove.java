package com.sirckopo.guezzdachezz;

public class ChessMove {
	public static final int sNumbers = -1;
	public static final int sPlain = 0;
	public static final int sUpper = 1;
	public static final int sDash = 2;
	public static final int sDefault = sPlain;
	
	ChessMove(String s) {setString(s);}
	ChessMove(int xx, int xy, int x, int y) {setCode(xx, xy, x, y);}
	ChessMove(int xx, int xy, int x, int y, int pr) {setCode(xx, xy, x, y, pr);}
	
	public void setCode(int xx, int xy, int x, int y) {
		setCode(xx, xy, x, y, 0);
	}
	
	public void setCode(int xx, int xy, int x, int y, int pr) {
		if (!ChessLayout.areXYOnBoard(xx, xy) || 
			!ChessLayout.areXYOnBoard(x, y)   ||
			!(pr == 0 || ChessLayout.fKnight <= pr && pr <= ChessLayout.fQueen)
			) {
			throw new IllegalArgumentException("bad code " + xx + xy + x + y + pr);
		}
		xX = xx; xY = xy; X = x; Y = y; promotion = pr;
	}
	
	public int getCode(int index) {
		switch (index) {
		case 0: return xX;
		case 1: return xY;
		case 2:	return X;
		case 3: return Y;
		case 4: return promotion;
		default: throw new IllegalArgumentException();
		}
	}
	
	public int[] getCode() {
        return new int[]{xX, xY, X, Y, promotion};
	}

	public String getString() {return getString(sDefault);}
	
	public String getString(int style) {
		String output = "";
		if (style == sNumbers) {
			output += (char)('0' + xX);
			output += (char)('0' + xY);
			output += (char)('0' + X);
			output += (char)('0' + Y);
			if (promotion != 0)
				output += (char)('0' + promotion);
		} else {
			output += (char)(((style & sUpper) != 0 ? 'A' : 'a') - 1 + xX);
			output += (char)('0' + xY);
			if ((style & sDash) != 0) {
				output += "-";
			}
			output += (char)(((style & sUpper) != 0 ? 'A' : 'a') - 1  + X);
			output += (char)('0' + Y);
			if (promotion != 0)
				output += ChessLayout.tFigures.charAt(promotion);
		}
		return output;
	}
	
	public void setString(String string) {
		String s = string.toLowerCase();
		
		// omg so mush checks
		if (s.length() > 4 && s.charAt(2) == '-') {
				s = s.substring(0, 2) + s.substring(3);
		}
		if (s.length() == 5) {
			if (!ChessLayout.tFigures.toLowerCase().contains(
					String.valueOf(s.charAt(4)))){
				throw new IllegalArgumentException();
			}
			setCode(s.charAt(0) - 'a' + 1, s.charAt(1) - '0', 
					s.charAt(2) - 'a' + 1, s.charAt(3) - '0',
					ChessLayout.tFigures.toLowerCase().indexOf(s.charAt(4)));
		} else if (s.length() == 4 ) {
			setCode(s.charAt(0) - 'a' + 1, s.charAt(1) - '0', 
					s.charAt(2) - 'a' + 1, s.charAt(3) - '0'); 
		} else {
			throw new IllegalArgumentException();
		}
	}

    public boolean isEqual(ChessMove cm) {
        if (cm == null) {
            return false;
        }
        int[] c = cm.getCode();
        return xX == c[0] && xY == c[1] && X == c[2] && Y == c[3] && promotion == c[4];
    }

	public void reset() {xX = 0; xY = 0; X = 0; Y = 0; promotion = 0;}
	
	private int xX = 0, xY = 0, X = 0, Y = 0, promotion = 0;
}
