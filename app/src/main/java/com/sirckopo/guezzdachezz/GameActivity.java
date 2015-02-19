package com.sirckopo.guezzdachezz;


import android.graphics.Color;
import android.graphics.Picture;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import java.util.LinkedList;


public class GameActivity extends ActionBarActivity {

    LinearLayout llBase;
    LinearLayout llBar;
    TableLayout tlChessboard;
    LinkedList<Button> butSquares = new LinkedList<>();
    Button butReset;
    Button butHint;
    Button butMoveIndicator;
    
    TextView tvTester;

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
        //setContentView(R.layout.activity_game);

        updateScreenMetrics();

        //Toast.makeText(GameActivity.this, screenWidth + " " + screenHeight + " " + isLandscape,
        //        Toast.LENGTH_LONG).show();

        llBase = new LinearLayout(this);
        llBase.setOrientation(isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        setContentView(llBase, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        LinearLayout llTest = new LinearLayout(this);
        llTest.setOrientation(!isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        //llTest.setBackgroundColor(Color.argb(255, 255, 0, 0));
        llBase.addView(llTest);

        tlChessboard = new TableLayout(this);
        tlChessboard.setOrientation(TableLayout.VERTICAL);
        tlChessboard.setBackgroundColor(Color.argb(255, 0, 255, 0));
        llBase.addView(tlChessboard, new LayoutParams(
                isLandscape ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT,
                isLandscape ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT));

        for (int j = 8; j > 0; j--) {
            TableRow trRank = new TableRow(this);
            trRank.setOrientation(TableRow.HORIZONTAL);
            tlChessboard.addView(trRank);
            //tlChessboard.addView(trRank, new TableLayout.LayoutParams(
            //        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

            for (int i = 1; i <= 8; i++) {
                Button butSquare = new Button(this);
                butSquare.setTag(i * 10 + j);
                butSquare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        squarePress(v);
                    }
                });
                butSquare.setMinimumWidth(0);
                butSquare.setMinimumHeight(0);
                butSquare.setPadding(0, 0, 0, 0);
                butSquares.add(butSquare);
                trRank.addView(butSquare);
                //trRank.addView(butSquare, new TableRow.LayoutParams(
                //        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

            }
        }


        llBar = new LinearLayout(this);
        llBar.setOrientation(!isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        llBase.addView(llBar);

        butReset = new Button(this);
        butReset.setText("Reset");
        butReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lMain.setup();
                updateSquares();
            }
        });
        llBar.addView(butReset, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1));

        butHint = new Button(this);
        butHint.setText("Hint");
        butHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameActivity.this, screenWidth + " " + screenHeight + "\n" +
                                llBase.getMeasuredWidth() + " " + llBase.getMeasuredHeight() + "\n" +
                                tlChessboard.getMeasuredWidth() + " " + tlChessboard.getMeasuredHeight(),
                        Toast.LENGTH_LONG).show();
            }
        });
        llBar.addView(butHint, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1));

        butMoveIndicator = new Button(this);
        butMoveIndicator.setText("");
        llBar.addView(butMoveIndicator, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 0.1f));

        llBase.post(new Runnable() {
            @Override
            public void run() {
                int butSize = (isLandscape ? tlChessboard.getMeasuredHeight() :
                        tlChessboard.getMeasuredWidth()) / 8;
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

        tvTester = new TextView(this);
        tvTester.setText("Chess engine status is right here. It's temporary, though.");
        llBase.addView(tvTester, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1));
                
                
        // Chess things
        lMain.setup();
        updateSquares();

        //SVGBuilder svgBuilder = new SVGBuilder();
        //svgBuilder.readFromResource(getResources(), R.raw.figure_wp);
        //svgWPawn = svgBuilder.build();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    ChessLayout lMain = new ChessLayout();
    final int STATE_WAIT = 0;
    final int STATE_SQUARE = 1;
    final int STATE_PROMOTION = 2;

    int state = 0;
    int sqx = 0;
    int sqy = 0;
    LinkedList<ChessMove> moveBuffer;

    private void updateSquares() {
        //TODO: implement graphical representation (using SVG or PNG) ?
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
            b.setText(String.valueOf(fig / ChessLayout.fBlack == 1 ?
                    ChessLayout.tFiguresUnicodeB.charAt(fig % ChessLayout.fBlack) :
                    ChessLayout.tFiguresUnicodeW.charAt(fig % ChessLayout.fBlack)));
        }
        // butMoveIndicator.setBackgroundColor(lMain.getMove() ?
        //        Color.argb(196, 0, 0, 0) : Color.argb(195, 255, 255, 255));
        butMoveIndicator.setText(String.valueOf((lMain.getMove() ? ChessLayout.tFiguresUnicodeB :
                ChessLayout.tFiguresUnicodeW).charAt(ChessLayout.fKing)));
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
                /**/String texst = "Waiting for a move, one of " + moveBuffer.size();
                for (ChessMove cm: moveBuffer) {
                    texst += " " + cm.getString();
                }
                tvTester.setText(texst);
                break;
            case STATE_SQUARE:
                ChessMove desiredMove;
                if (sq % 10 == (lMain.getMove() ? 8 : 1) &&
                        lMain.getBoardFigure(sqx / 10, sqx % 10) == ChessLayout.fPawn) {
                    // TODO: work it out with promotion
                    desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10,
                            ChessLayout.fQueen);
                } else
                desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10);
                boolean isPossible = false;
                for (ChessMove cm: moveBuffer) {
                    if (desiredMove.isEqual(cm)) {
                        isPossible = true;
                        break;
                    }
                }
                if (!isPossible) {
                    tvTester.setText("No such move. " + desiredMove.getString());
                    moveReset();
                    updateSquares();
                    if (lMain.getBoard(sq / 10, sq % 10) != 0) {
                        state = STATE_WAIT;
                        squarePress(v);
                    }
                    return;
                }
                if (lMain.doMove(desiredMove)) {
                    /**/updateSquares();
                    tvTester.setText("Move made! " + desiredMove.getString());
                    if (lMain.isCheck(lMain.getMove())) {
                        /**/updateSquares();
                        tvTester.setText(tvTester.getText() + " Check!");
                        if (lMain.isCheckmate(lMain.getMove())) {
                            /**/updateSquares();
                            tvTester.setText(tvTester.getText() + " And mate!");
                        }
                    }
                } else {
                    //TODO: (probably) 'king is still attacked' notification
                    tvTester.setText("The king is still attacked! " + desiredMove.getString());
                    //Toast.makeText(GameActivity.this, "The king is still under attack!",
                    //        Toast.LENGTH_LONG).show();
                }
                updateSquares();
                moveReset();
                break;
            case STATE_PROMOTION:
                // TODO: yes, right here. promotion code.
                break;
        }

    }

}
