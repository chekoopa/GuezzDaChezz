package com.sirckopo.guezzdachezz;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;


public class GameActivity extends ActionBarActivity {

    RelativeLayout llBase;
    TableLayout tlChessboard;
    LinkedList<Button> butSquares = new LinkedList<>();
    
    MenuItem miMoveIndicator;
    MenuItem miHint;
    MenuItem miLevelIndicator;
    MenuItem miReset;
    MenuItem miFreeplay;

    private int screenWidth;
    private int screenHeight;
    private boolean isLandscape;

    private void updateScreenMetrics() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        isLandscape = (screenWidth > screenHeight);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all the dynamic layout magic is right here
        makeLayout();

        try {
            layoutStorage.createDataBase();
            layoutStorage.overrideDataBase();
            layoutStorage.openDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        // Chess things
        Intent intent = getIntent();
        problemSet = intent.getStringExtra("set");
        currentId = intent.getIntExtra("id", 0);

        if (problemSet == null) {
            baseFEN = (intent.getStringExtra("fen") == null ? ChessLayout.startLayout :
                    intent.getStringExtra("fen"));
        } else {
            getProblem();
        }

        if (savedInstanceState == null) {
            lMain.loadFEN(baseFEN);
        } else {
            lMain.loadFEN(savedInstanceState.getString("board"));
        }
        llBase.post(new Runnable() {
            @Override
            public void run() {
                updateSquares();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("board", lMain.getFEN());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);
        miMoveIndicator = menu.findItem(R.id.action_king);
        miHint = menu.findItem(R.id.action_hint);
        miLevelIndicator = menu.findItem(R.id.action_level);
        miReset = menu.findItem(R.id.action_reset);
        miFreeplay = menu.findItem(R.id.action_freeplay);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_hint:
                makeHint();
                return true;
            case R.id.action_reset:
                resetLayout();
                return true;
            case R.id.action_freeplay:
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("fen", baseFEN);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeLayout() {
        updateScreenMetrics();

        llBase = new RelativeLayout(this);
        setContentView(llBase, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        tlChessboard = new TableLayout(this);
        tlChessboard.setOrientation(TableLayout.VERTICAL);
        tlChessboard.setBackgroundColor(Color.argb(255, 0, 255, 0));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                isLandscape ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT,
                isLandscape ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        llBase.addView(tlChessboard, params);

        for (int j = 8; j > 0; j--) {
            TableRow trRank = new TableRow(this);
            trRank.setOrientation(TableRow.HORIZONTAL);
            tlChessboard.addView(trRank);

            for (int i = 1; i <= 8; i++) {
                Button butSquare = new Button(this);
                butSquare.setTag(i * 10 + j);
                butSquare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        squarePress(v);
                    }
                });
                butSquare.setMinimumWidth(1);
                butSquare.setMinimumHeight(1);
                butSquare.setPadding(0, 0, 0, 0);
                butSquares.add(butSquare);
                trRank.addView(butSquare);
            }
        }

        llBase.post(new Runnable() {
            @Override
            public void run() {
                int butSize = (isLandscape ? tlChessboard.getMeasuredHeight() :
                        tlChessboard.getMeasuredWidth()) / 8 + 1;
                for (Button b : butSquares) {
                    b.setWidth(butSize);
                    b.setHeight(butSize);
                    b.setTextSize(TypedValue.COMPLEX_UNIT_PX, butSize * 4 / 5);
                    int code = (Integer)b.getTag();
                    b.setBackgroundColor(((code / 10 + code % 10) % 2 == 0) ?
                            Color.argb(128, 0, 0, 0) :
                            Color.argb(128, 255, 255, 255));
                }
            }
        });

        //llBase.addView(tvTester, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT, 1));
    }

    LayoutStorage layoutStorage = new LayoutStorage(this);

    ChessLayout lMain = new ChessLayout();

    String baseFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 #1";
    String problemSet = "";
    int currentId = 0;

    ChessMove[][] solutions = {};

    final int STATE_WAIT = 0;
    final int STATE_SQUARE = 1;
    final int STATE_PROMOTION = 2;

    int state = 0;
    int sqx = 0;
    int sqy = 0;
    LinkedList<ChessMove> moveBuffer;

    private void getProblem() {
        // just to transform a current problem into a plain layout
        solutions = null;

        String[] problem = layoutStorage.getProblem(problemSet, currentId);
        if (problem == null) {
            // shall we get away?
            //TODO: fancy final grats screen
            Toast.makeText(getApplicationContext(), "That's all, folks! Feel free to " +
                    "freeplay the last problem", Toast.LENGTH_LONG).show();
            currentId = 0;
            resetLayout();
            // we shall, but for while we have this
            return;
        }
        baseFEN = problem[0];
        String[] sol = problem[1].split(" ");
        solutions = new ChessMove[sol.length][2];
        for (int i = 0; i < sol.length; i++) {
            solutions[i][0] = new ChessMove(sol[i]);
        }

        resetLayout();
    }

    private void resetLayout() {
        lMain.loadFEN(baseFEN);
        moveReset();
        updateSquares();
    }

    private void makeHint() {
        if (solutions == null || solutions.length == 0)
            return;
        int i = (int) Math.floor(Math.random() * solutions.length);
        int fig = lMain.getBoardFigure(solutions[i][0].getCode(0), solutions[i][0].getCode(1));
        String hint = "Well, you shall try a ";
        switch (fig) {
            case ChessLayout.fPawn: hint += "pawn"; break;
            case ChessLayout.fKnight: hint += "knight"; break;
            case ChessLayout.fBishop: hint += "bishop"; break;
            case ChessLayout.fRook: hint += "rook"; break;
            case ChessLayout.fQueen: hint += "queen"; break;
            case ChessLayout.fKing: hint += "king"; break;
        }
        hint += ". Is it enough for you?";
        Toast.makeText(getApplicationContext(), hint, Toast.LENGTH_LONG).show();
    }

    private void updateSquares() {
        //TODO: implement fancier graphical representation (using SVG or PNG) ?
        for (Button b: butSquares) {
            int sq = (Integer) b.getTag();
            int fig = lMain.getBoard(sq / 10, sq % 10);
            if (fig == 0) {
                b.setText("");
                continue;
            }
            //plain style
            //b.setText((fig / ChessLayout.fBlack == 1 ? "b" : "w") +
            //        ChessLayout.tFigures.charAt(fig % ChessLayout.fBlack));
            b.setText(ChessLayout.getUnicodeCharString(fig));
        }
        if (miMoveIndicator != null)
            miMoveIndicator.setTitle(ChessLayout.getUnicodeCharString(ChessLayout.fKing +
                           (lMain.getMove() ? ChessLayout.fBlack : 0)) +
                           (lMain.getMove() ? getString(R.string.chess_black) :
                                              getString(R.string.chess_white)));
        if (miHint != null)
            miHint.setVisible(solutions != null && solutions.length != 0);
        if (miLevelIndicator != null)
            miLevelIndicator.setTitle(currentId != 0 ? String.valueOf(currentId) :
                                          getString(R.string.status_freeplay));
        if (miReset != null)
            miReset.setVisible(solutions == null || solutions.length == 0);
        if (miFreeplay != null)
            miFreeplay.setVisible(solutions != null && solutions.length != 0);
    }

    private void moveReset() {
        state = 0;
        sqx = 0;
        sqy = 0;
        moveBuffer = null;
        for (Button b: butSquares) {
            int code = (Integer) b.getTag();
            b.setBackgroundColor(((code / 10 + code % 10) % 2 == 0) ?
                    Color.argb(128, 0, 0, 0) :
                    Color.argb(128, 255, 255, 255));
        }
    }

    private void highlightMoveBuffer() {
        for (ChessMove cm : moveBuffer) {
            tlChessboard.findViewWithTag(cm.getCode(2) * 10 +
                    cm.getCode(3)).setBackgroundColor((
                    (cm.getCode(2) + cm.getCode(3)) % 2 == 0) ?
                    Color.argb(192, 0, 0, 0) :
                    Color.argb(192, 255, 255, 255));
        }
    }

    private void squarePress(View v) {
        if (lMain.isCheckmate(lMain.getMove())) {
            //TODO: game over nudge
            Toast.makeText(getApplicationContext(), "Uh-uh-uh, the game is already over.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int sq = (Integer) v.getTag();
        switch (state) {
            case STATE_WAIT:
                int fig = lMain.getBoard(sq / 10, sq % 10);
                if ( fig == 0 || fig / ChessLayout.fBlack !=
                        (lMain.getMove()? 1 : 0)) {
                    return;
                }
                moveBuffer = lMain.moveBuffer(sq / 10, sq % 10);
                if (moveBuffer.isEmpty()) {
                    moveBuffer = null;
                    return;
                }
                sqx = sq;
                highlightMoveBuffer();
                state = STATE_SQUARE;
                ///**/String texst = "Waiting for a move, one of " + moveBuffer.size();
                //for (ChessMove cm: moveBuffer) {
                //    texst += " " + cm.getString();
                //}
                //tvTester.setText(texst);
                break;
            case STATE_SQUARE:
                //TODO: unite promotion and plain move codes
                if (sq % 10 == (lMain.getMove() ? 1 : 8) &&
                     lMain.getBoardFigure(sqx / 10, sqx % 10) == ChessLayout.fPawn) {
                    ChessMove desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10);
                    if (!isInMoveBuffer(desiredMove)) {
                        moveReset();
                        updateSquares();
                        if (lMain.getBoard(sq / 10, sq % 10) != 0) {
                            state = STATE_WAIT;
                            squarePress(v);
                        }
                        return;
                    }
                    state = STATE_PROMOTION;
                    showPromotionScreen();
                    sqy = sq;
                    return;
                }
                ChessMove desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10);
                if (!isInMoveBuffer(desiredMove)) {
                    moveReset();
                    updateSquares();
                    if (lMain.getBoard(sq / 10, sq % 10) != 0) {
                        state = STATE_WAIT;
                        squarePress(v);
                    }
                    return;
                }
                if (lMain.doMove(desiredMove)) {
                    warnMoveResult(desiredMove);
                } else warnKingIsStillAttacked(desiredMove);
                updateSquares();
                moveReset();
                break;
            case STATE_PROMOTION:
                int newfig = 0;
                switch (sq) {
                    case 35: newfig = ChessLayout.fKnight; break;
                    case 45: newfig = ChessLayout.fBishop; break;
                    case 55: newfig = ChessLayout.fRook; break;
                    case 65: newfig = ChessLayout.fQueen; break;
                }
                if (newfig != 0) {
                    desiredMove = new ChessMove(sqx / 10, sqx % 10, sqy / 10, sqy % 10);
                    if (lMain.doMove(desiredMove)) {
                        lMain.setBoard(sqy / 10, sqy % 10, newfig +
                                        (lMain.getMove() ? 0 : ChessLayout.fBlack));
                        warnMoveResult(desiredMove);
                    } else warnKingIsStillAttacked(desiredMove);
                }
                updateSquares();
                moveReset();
                break;
        }

    }

    private void warnKingIsStillAttacked(ChessMove cm) {
        //TODO: 'king is still attacked' notification (a nudge?)
        Toast.makeText(getApplicationContext(), "The king is still under attack!",
                Toast.LENGTH_SHORT).show();
    }

    private void warnMoveResult(ChessMove cm) {
        //tvTester.setText("Move made! " + cm.getString());
        if (solutions == null || solutions.length == 0) { // just check for a check/mate
            if (lMain.isCheck(lMain.getMove())) {
                //TODO: fancier nudge for a check/mate?
                if (lMain.isCheckmate(lMain.getMove()))
                    Toast.makeText(getApplicationContext(), "Checkmate!!!",
                            Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Check!",
                            Toast.LENGTH_SHORT).show();
            }
            return;
        }

        for (ChessMove[] solution : solutions) {
            if (solution[0].isEqual(cm)) {
                //TODO: fancier "solved!" greeting?
                Toast.makeText(getApplicationContext(), "Right move! Good job!",
                        Toast.LENGTH_LONG).show();
                delayLevelUp();
                return;
            }
        }
        Toast.makeText(getApplicationContext(), "Wrong! Think again.",
                Toast.LENGTH_SHORT).show();
        // TODO: get enemy side to make a defensive move if on wrong try (or a little nudge)
        delayReset();
    }

    private void showPromotionScreen() {
        ((TextView) tlChessboard.findViewWithTag(35)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fKnight +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(45)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fBishop +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(55)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fRook +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(65)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fQueen +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        tlChessboard.findViewWithTag(35).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(45).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(55).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(65).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
    }

    private boolean isInMoveBuffer(ChessMove desm) {
        for (ChessMove cm: moveBuffer)
            if (desm.isEqual(cm))
                return true;
        return false;
    }

    private void delayLevelUp() {
        llBase.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentId += 1;
                getProblem();
            }
        }, 500);
    }

    private void delayReset() {
        llBase.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetLayout();
            }
        }, 500);
    }
}
